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
package com.github.scribejava.core.httpclient.jdk;

import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.httpclient.multipart.MultipartPayload;
import com.github.scribejava.core.httpclient.multipart.MultipartUtils;
import com.github.scribejava.core.model.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class JDKHttpClient implements HttpClient {

  private final JDKHttpClientConfig config;

  public JDKHttpClient() {
    this(JDKHttpClientConfig.defaultConfig());
  }

  public JDKHttpClient(JDKHttpClientConfig clientConfig) {
    config = clientConfig;
  }

  private static Map<String, String> parseHeaders(HttpURLConnection conn) {
    return conn.getHeaderFields().entrySet().stream()
        .filter(entry -> entry.getKey() != null)
        .collect(
            Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().get(0),
                (existing, replacement) -> existing // Default to first value
                ));
  }

  private static void addHeaders(
      HttpURLConnection connection, Map<String, String> headers, String userAgent) {
    for (Map.Entry<String, String> header : headers.entrySet()) {
      connection.setRequestProperty(header.getKey(), header.getValue());
    }

    if (userAgent != null) {
      connection.setRequestProperty(OAuthConstants.USER_AGENT_HEADER_NAME, userAgent);
    }
  }

  private static void addBody(HttpURLConnection connection, byte[] content, boolean requiresBody)
      throws IOException {
    final int contentLength = content.length;
    if (requiresBody || contentLength > 0) {
      try (OutputStream outputStream =
          prepareConnectionForBodyAndGetOutputStream(connection, contentLength)) {
        if (contentLength > 0) {
          outputStream.write(content);
        }
      }
    }
  }

  private static void addBody(
      HttpURLConnection connection, MultipartPayload multipartPayload, boolean requiresBody)
      throws IOException {

    for (Map.Entry<String, String> header : multipartPayload.getHeaders().entrySet()) {
      connection.setRequestProperty(header.getKey(), header.getValue());
    }

    if (requiresBody) {
      final ByteArrayOutputStream os = MultipartUtils.getPayload(multipartPayload);
      final int contentLength = os.size();
      try (OutputStream outputStream =
          prepareConnectionForBodyAndGetOutputStream(connection, contentLength)) {
        if (contentLength > 0) {
          os.writeTo(outputStream);
        }
      }
    }
  }

  private static OutputStream prepareConnectionForBodyAndGetOutputStream(
      HttpURLConnection connection, int contentLength) throws IOException {

    connection.setRequestProperty(CONTENT_LENGTH, String.valueOf(contentLength));
    if (connection.getRequestProperty(CONTENT_TYPE) == null) {
      connection.setRequestProperty(CONTENT_TYPE, DEFAULT_CONTENT_TYPE);
    }
    connection.setDoOutput(true);
    return connection.getOutputStream();
  }

  @Override
  public void close() {}

  @Override
  public <T> CompletableFuture<T> executeAsync(
      String userAgent,
      Map<String, String> headers,
      Verb httpVerb,
      String completeUrl,
      byte[] bodyContents,
      OAuthAsyncRequestCallback<T> callback,
      OAuthRequest.ResponseConverter<T> converter) {

    return doExecuteAsync(
        userAgent,
        headers,
        httpVerb,
        completeUrl,
        BodyType.BYTE_ARRAY,
        bodyContents,
        callback,
        converter);
  }

  @Override
  public <T> CompletableFuture<T> executeAsync(
      String userAgent,
      Map<String, String> headers,
      Verb httpVerb,
      String completeUrl,
      MultipartPayload bodyContents,
      OAuthAsyncRequestCallback<T> callback,
      OAuthRequest.ResponseConverter<T> converter) {

    return doExecuteAsync(
        userAgent,
        headers,
        httpVerb,
        completeUrl,
        BodyType.MULTIPART,
        bodyContents,
        callback,
        converter);
  }

  @Override
  public <T> CompletableFuture<T> executeAsync(
      String userAgent,
      Map<String, String> headers,
      Verb httpVerb,
      String completeUrl,
      String bodyContents,
      OAuthAsyncRequestCallback<T> callback,
      OAuthRequest.ResponseConverter<T> converter) {

    return doExecuteAsync(
        userAgent,
        headers,
        httpVerb,
        completeUrl,
        BodyType.STRING,
        bodyContents,
        callback,
        converter);
  }

  @Override
  public <T> CompletableFuture<T> executeAsync(
      String userAgent,
      Map<String, String> headers,
      Verb httpVerb,
      String completeUrl,
      File bodyContents,
      OAuthAsyncRequestCallback<T> callback,
      OAuthRequest.ResponseConverter<T> converter) {
    throw new UnsupportedOperationException(
        "JDKHttpClient does not support File payload for the moment");
  }

  private <T> CompletableFuture<T> doExecuteAsync(
      String userAgent,
      Map<String, String> headers,
      Verb httpVerb,
      String completeUrl,
      BodyType bodyType,
      Object bodyContents,
      OAuthAsyncRequestCallback<T> callback,
      OAuthRequest.ResponseConverter<T> converter) {
    final CompletableFuture<T> future = new CompletableFuture<>();
    try {
      final Response response =
          doExecute(userAgent, headers, httpVerb, completeUrl, bodyType, bodyContents);
      @SuppressWarnings("unchecked")
      final T t = converter == null ? (T) response : converter.convert(response);
      if (callback != null) {
        callback.onCompleted(t);
      }
      future.complete(t);
    } catch (IOException | RuntimeException e) {
      if (callback != null) {
        callback.onThrowable(e);
      }
      future.completeExceptionally(e);
    }
    return future;
  }

  @Override
  public Response execute(
      String userAgent,
      Map<String, String> headers,
      Verb httpVerb,
      String completeUrl,
      byte[] bodyContents)
      throws InterruptedException, ExecutionException, IOException {
    return doExecute(userAgent, headers, httpVerb, completeUrl, BodyType.BYTE_ARRAY, bodyContents);
  }

  @Override
  public Response execute(
      String userAgent,
      Map<String, String> headers,
      Verb httpVerb,
      String completeUrl,
      MultipartPayload multipartPayloads)
      throws InterruptedException, ExecutionException, IOException {
    return doExecute(
        userAgent, headers, httpVerb, completeUrl, BodyType.MULTIPART, multipartPayloads);
  }

  @Override
  public Response execute(
      String userAgent,
      Map<String, String> headers,
      Verb httpVerb,
      String completeUrl,
      String bodyContents)
      throws InterruptedException, ExecutionException, IOException {
    return doExecute(userAgent, headers, httpVerb, completeUrl, BodyType.STRING, bodyContents);
  }

  @Override
  public Response execute(
      String userAgent,
      Map<String, String> headers,
      Verb httpVerb,
      String completeUrl,
      File bodyContents)
      throws InterruptedException, ExecutionException, IOException {
    throw new UnsupportedOperationException(
        "JDKHttpClient does not support File payload for the moment");
  }

  private Response doExecute(
      String userAgent,
      Map<String, String> headers,
      Verb httpVerb,
      String completeUrl,
      BodyType bodyType,
      Object bodyContents)
      throws IOException {
    final URL url = new URL(completeUrl);
    final HttpURLConnection connection;
    if (config.getProxy() == null) {
      connection = (HttpURLConnection) url.openConnection();
    } else {
      connection = (HttpURLConnection) url.openConnection(config.getProxy());
    }
    connection.setInstanceFollowRedirects(config.isFollowRedirects());
    if (connection instanceof javax.net.ssl.HttpsURLConnection) {
      if (config.getSslSocketFactory() != null) {
        ((javax.net.ssl.HttpsURLConnection) connection)
            .setSSLSocketFactory(config.getSslSocketFactory());
      }
      if (config.getHostnameVerifier() != null) {
        ((javax.net.ssl.HttpsURLConnection) connection)
            .setHostnameVerifier(config.getHostnameVerifier());
      }
    }

    if (httpVerb == Verb.PATCH) {
      connection.setRequestMethod(Verb.POST.name());
      connection.setRequestProperty("X-HTTP-Method-Override", Verb.PATCH.name());
    } else {
      connection.setRequestMethod(httpVerb.name());
    }
    if (config.getConnectTimeout() != null) {
      connection.setConnectTimeout(config.getConnectTimeout());
    }
    if (config.getReadTimeout() != null) {
      connection.setReadTimeout(config.getReadTimeout());
    }
    addHeaders(connection, headers, userAgent);
    if (httpVerb.isPermitBody()) {
      bodyType.setBody(connection, bodyContents, httpVerb.isRequiresBody());
    }

    try {
      connection.connect();
      final int responseCode = connection.getResponseCode();
      return new Response(
          responseCode,
          connection.getResponseMessage(),
          parseHeaders(connection),
          responseCode >= 200 && responseCode < 400
              ? connection.getInputStream()
              : connection.getErrorStream());
    } catch (UnknownHostException e) {
      connection.disconnect();
      throw new OAuthException("The IP address of a host could not be determined.", e);
    } catch (IOException e) {
      connection.disconnect();
      throw e;
    }
  }

  private enum BodyType {
    BYTE_ARRAY {
      @Override
      void setBody(HttpURLConnection connection, Object bodyContents, boolean requiresBody)
          throws IOException {
        addBody(connection, (byte[]) bodyContents, requiresBody);
      }
    },
    MULTIPART {
      @Override
      void setBody(HttpURLConnection connection, Object bodyContents, boolean requiresBody)
          throws IOException {
        addBody(connection, (MultipartPayload) bodyContents, requiresBody);
      }
    },
    STRING {
      @Override
      void setBody(HttpURLConnection connection, Object bodyContents, boolean requiresBody)
          throws IOException {
        addBody(connection, ((String) bodyContents).getBytes(), requiresBody);
      }
    };

    abstract void setBody(HttpURLConnection connection, Object bodyContents, boolean requiresBody)
        throws IOException;
  }
}
