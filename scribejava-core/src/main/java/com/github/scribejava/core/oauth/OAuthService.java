/*
 * The MIT License
 *
 * Copyright (c) 2010 Pablo Fernandez
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.scribejava.core.oauth;

import com.github.scribejava.core.exceptions.OAuthNetworkException;
import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.httpclient.HttpClientConfig;
import com.github.scribejava.core.httpclient.HttpClientProvider;
import com.github.scribejava.core.httpclient.jdk.JDKHttpClient;
import com.github.scribejava.core.httpclient.jdk.JDKHttpClientConfig;
import com.github.scribejava.core.model.OAuthAsyncRequestCallback;
import com.github.scribejava.core.model.OAuthLogger;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/** Classe de base abstraite pour tous les services OAuth. */
public abstract class OAuthService implements Closeable {

  private final String apiKey;
  private final String apiSecret;
  private final String callback;
  private final String userAgent;
  private final HttpClient httpClient;
  private final boolean ownHttpClient;
  private final OutputStream debugStream;
  private final List<OAuthRequestInterceptor> interceptors = new ArrayList<>();
  private OAuthLogger logger;
  private OAuthRetryPolicy retryPolicy;
  private RateLimitListener rateLimitListener;

  public OAuthService(
      String apiKey,
      String apiSecret,
      String callback,
      OutputStream debugStream,
      String userAgent,
      HttpClientConfig httpClientConfig,
      HttpClient httpClient) {
    this.apiKey = apiKey;
    this.apiSecret = apiSecret;
    this.callback = callback;
    this.debugStream = debugStream;
    this.userAgent = userAgent;

    if (httpClientConfig == null && httpClient == null) {
      this.httpClient = new JDKHttpClient(JDKHttpClientConfig.defaultConfig());
      this.ownHttpClient = true;
    } else {
      this.ownHttpClient = httpClient == null;
      this.httpClient = httpClient == null ? getClient(httpClientConfig) : httpClient;
    }
  }

  private static HttpClient getClient(HttpClientConfig config) {
    for (HttpClientProvider provider : ServiceLoader.load(HttpClientProvider.class)) {
      final HttpClient client = provider.createClient(config);
      if (client != null) {
        return client;
      }
    }
    return null;
  }

  public void setLogger(OAuthLogger logger) {
    this.logger = logger;
  }

  public void setRetryPolicy(OAuthRetryPolicy retryPolicy) {
    this.retryPolicy = retryPolicy;
  }

  public void setRateLimitListener(RateLimitListener rateLimitListener) {
    this.rateLimitListener = rateLimitListener;
  }

  @Override
  public void close() throws IOException {
    if (ownHttpClient && httpClient != null) {
      httpClient.close();
    }
  }

  public String getApiKey() {
    return apiKey;
  }

  public String getApiSecret() {
    return apiSecret;
  }

  public String getCallback() {
    return callback;
  }

  /**
   * Exécute une requête OAuth de manière asynchrone avec un rappel.
   *
   * @param request La requête à exécuter.
   * @param callback Le rappel à invoquer une fois la réponse reçue.
   * @return Un {@link CompletableFuture}.
   */
  public CompletableFuture<Response> execute(
      OAuthRequest request, OAuthAsyncRequestCallback<Response> callback) {
    return execute(request, callback, null);
  }

  /**
   * Exécute une requête OAuth de manière asynchrone avec un rappel et un convertisseur de réponse.
   *
   * @param <R> Le type de l'objet converti.
   * @param request La requête.
   * @param callback Le rappel.
   * @param converter Le convertisseur de réponse.
   * @return Un {@link CompletableFuture}.
   */
  public <R> CompletableFuture<R> execute(
      OAuthRequest request,
      OAuthAsyncRequestCallback<R> callback,
      OAuthRequest.ResponseConverter<R> converter) {

    interceptors.forEach(interceptor -> interceptor.intercept(request));
    if (logger != null) {
      logger.logRequest(request);
    }

    final CompletableFuture<R> future = executeInternalAsync(request, callback, converter);

    return future.thenApply(
        result -> {
          if (result instanceof Response) {
            final Response resp = (Response) result;
            resp.setRequest(request);
            checkRateLimits(resp);
            if (logger != null) {
              logger.logResponse(resp);
            }
          }
          return result;
        });
  }

  /**
   * Exécute une requête OAuth de manière synchrone.
   *
   * @param request La requête à exécuter.
   * @return La réponse reçue.
   * @throws InterruptedException si le thread est interrompu.
   * @throws ExecutionException si l'exécution échoue.
   * @throws IOException en cas d'erreur réseau.
   */
  public Response execute(OAuthRequest request)
      throws InterruptedException, ExecutionException, IOException {
    interceptors.forEach(interceptor -> interceptor.intercept(request));
    if (logger != null) {
      logger.logRequest(request);
    }

    int attempts = 0;
    Response response = null;
    final int maxAttempts = retryPolicy != null ? retryPolicy.getMaxAttempts() : 1;

    while (attempts < maxAttempts) {
      attempts++;
      try {
        response = executeOnce(request);
        if (response != null) {
          checkRateLimits(response);
        }
        if (retryPolicy == null
            || response == null
            || !retryPolicy.shouldRetry(response)
            || attempts >= maxAttempts) {
          break;
        }
        Thread.sleep(retryPolicy.getDelayMs());
      } catch (IOException e) {
        if (retryPolicy == null || attempts >= maxAttempts) {
          throw new OAuthNetworkException(e);
        }
        Thread.sleep(retryPolicy.getDelayMs());
      }
    }

    if (logger != null && response != null) {
      response.setRequest(request);
      logger.logResponse(response);
    }
    return response;
  }

  private void checkRateLimits(Response response) {
    if (rateLimitListener == null) {
      return;
    }
    final String remaining = response.getHeader("X-RateLimit-Remaining");
    final String reset = response.getHeader("X-RateLimit-Reset");
    if (remaining != null && reset != null) {
      try {
        rateLimitListener.onRateLimit(Integer.parseInt(remaining), Long.parseLong(reset), response);
      } catch (NumberFormatException e) {
        // Ignored
      }
    }
  }

  private <R> CompletableFuture<R> executeInternalAsync(
      OAuthRequest request,
      OAuthAsyncRequestCallback<R> callback,
      OAuthRequest.ResponseConverter<R> converter) {
    final File filePayload = request.getFilePayload();
    if (filePayload != null) {
      return httpClient.executeAsync(
          userAgent,
          request.getHeaders(),
          request.getVerb(),
          request.getCompleteUrl(),
          filePayload,
          callback,
          converter);
    } else if (request.getStringPayload() != null) {
      return httpClient.executeAsync(
          userAgent,
          request.getHeaders(),
          request.getVerb(),
          request.getCompleteUrl(),
          request.getStringPayload(),
          callback,
          converter);
    } else if (request.getMultipartPayload() != null) {
      return httpClient.executeAsync(
          userAgent,
          request.getHeaders(),
          request.getVerb(),
          request.getCompleteUrl(),
          request.getMultipartPayload(),
          callback,
          converter);
    } else {
      return httpClient.executeAsync(
          userAgent,
          request.getHeaders(),
          request.getVerb(),
          request.getCompleteUrl(),
          request.getByteArrayPayload(),
          callback,
          converter);
    }
  }

  private Response executeOnce(OAuthRequest request)
      throws InterruptedException, ExecutionException, IOException {
    final File filePayload = request.getFilePayload();
    if (filePayload != null) {
      return httpClient.execute(
          userAgent,
          request.getHeaders(),
          request.getVerb(),
          request.getCompleteUrl(),
          filePayload);
    } else if (request.getStringPayload() != null) {
      return httpClient.execute(
          userAgent,
          request.getHeaders(),
          request.getVerb(),
          request.getCompleteUrl(),
          request.getStringPayload());
    } else if (request.getMultipartPayload() != null) {
      return httpClient.execute(
          userAgent,
          request.getHeaders(),
          request.getVerb(),
          request.getCompleteUrl(),
          request.getMultipartPayload());
    } else {
      return httpClient.execute(
          userAgent,
          request.getHeaders(),
          request.getVerb(),
          request.getCompleteUrl(),
          request.getByteArrayPayload());
    }
  }

  public void log(String message) {
    if (debugStream != null) {
      log(message, (Object[]) null);
    }
  }

  public void log(String messagePattern, Object... params) {
    final String message =
        params == null || params.length == 0
            ? messagePattern
            : String.format(messagePattern, params);
    try {
      if (debugStream != null) {
        debugStream.write((message + '\n').getBytes(StandardCharsets.UTF_8));
      }
    } catch (IOException e) {
      throw new RuntimeException("Problems writing to debug stream", e);
    }
  }

  /**
   * Indique si le mode débogage est activé.
   *
   * @return true si activé, false sinon.
   */
  protected boolean isDebug() {
    return debugStream != null;
  }
}
