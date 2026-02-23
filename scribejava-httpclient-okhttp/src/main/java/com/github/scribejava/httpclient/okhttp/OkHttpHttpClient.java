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
package com.github.scribejava.httpclient.okhttp;

import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.httpclient.multipart.BodyPartPayload;
import com.github.scribejava.core.httpclient.multipart.ByteArrayBodyPartPayload;
import com.github.scribejava.core.httpclient.multipart.MultipartPayload;
import com.github.scribejava.core.model.*;
import com.github.scribejava.core.model.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import okhttp3.*;
import okhttp3.internal.http.HttpMethod;

/** Implémentation du client HTTP utilisant la bibliothèque OkHttp. */
public class OkHttpHttpClient implements HttpClient {

  private static final MediaType DEFAULT_CONTENT_TYPE_MEDIA_TYPE =
      MediaType.parse(DEFAULT_CONTENT_TYPE);

  private final OkHttpClient client;

  /** Constructeur par défaut utilisant la configuration standard. */
  public OkHttpHttpClient() {
    this(OkHttpHttpClientConfig.defaultConfig());
  }

  /**
   * Constructeur avec configuration spécifique.
   *
   * @param config La configuration OkHttp.
   */
  public OkHttpHttpClient(final OkHttpHttpClientConfig config) {
    final OkHttpClient.Builder clientBuilder = config.getClientBuilder();
    client = clientBuilder == null ? new OkHttpClient() : clientBuilder.build();
  }

  /**
   * Constructeur avec une instance OkHttpClient préexistante.
   *
   * @param client L'instance OkHttp.
   */
  public OkHttpHttpClient(final OkHttpClient client) {
    this.client = client;
  }

  static Response convertResponse(final okhttp3.Response okHttpResponse) {
    final Headers headers = okHttpResponse.headers();
    final Map<String, String> headersMap = new HashMap<>();
    for (final String headerName : headers.names()) {
      headersMap.put(headerName, headers.get(headerName));
    }

    final ResponseBody body = okHttpResponse.body();
    final InputStream bodyStream = body == null ? null : body.byteStream();
    return new Response(okHttpResponse.code(), okHttpResponse.message(), headersMap, bodyStream);
  }

  @Override
  public void close() throws IOException {
    client.dispatcher().executorService().shutdown();
    client.connectionPool().evictAll();
    final Cache cache = client.cache();
    if (cache != null) {
      cache.close();
    }
  }

  @Override
  public <T> CompletableFuture<T> executeAsync(
      final String userAgent,
      final Map<String, String> headers,
      final Verb httpVerb,
      final String completeUrl,
      final byte[] bodyContents,
      final OAuthAsyncRequestCallback<T> callback,
      final OAuthRequest.ResponseConverter<T> converter) {

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
      final String userAgent,
      final Map<String, String> headers,
      final Verb httpVerb,
      final String completeUrl,
      final MultipartPayload bodyContents,
      final OAuthAsyncRequestCallback<T> callback,
      final OAuthRequest.ResponseConverter<T> converter) {

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
      final String userAgent,
      final Map<String, String> headers,
      final Verb httpVerb,
      final String completeUrl,
      final String bodyContents,
      final OAuthAsyncRequestCallback<T> callback,
      final OAuthRequest.ResponseConverter<T> converter) {

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
      final String userAgent,
      final Map<String, String> headers,
      final Verb httpVerb,
      final String completeUrl,
      final File bodyContents,
      final OAuthAsyncRequestCallback<T> callback,
      final OAuthRequest.ResponseConverter<T> converter) {

    return doExecuteAsync(
        userAgent,
        headers,
        httpVerb,
        completeUrl,
        BodyType.FILE,
        bodyContents,
        callback,
        converter);
  }

  private <T> CompletableFuture<T> doExecuteAsync(
      final String userAgent,
      final Map<String, String> headers,
      final Verb httpVerb,
      final String completeUrl,
      final BodyType bodyType,
      final Object bodyContents,
      final OAuthAsyncRequestCallback<T> callback,
      final OAuthRequest.ResponseConverter<T> converter) {

    final CompletableFuture<T> future = new CompletableFuture<>();
    final Call call = createCall(userAgent, headers, httpVerb, completeUrl, bodyType, bodyContents);

    future.whenComplete(
        (t, throwable) -> {
          if (future.isCancelled()) {
            call.cancel();
          }
        });

    call.enqueue(
        new Callback() {
          @Override
          public void onFailure(final Call call, final IOException e) {
            if (callback != null) {
              callback.onThrowable(e);
            }
            future.completeExceptionally(e);
          }

          @Override
          public void onResponse(final Call call, final okhttp3.Response response)
              throws IOException {
            try {
              final Response resp = convertResponse(response);
              @SuppressWarnings("unchecked")
              final T t = converter == null ? (T) resp : converter.convert(resp);
              if (callback != null) {
                callback.onCompleted(t);
              }
              future.complete(t);
            } catch (final IOException | RuntimeException e) {
              if (callback != null) {
                callback.onThrowable(e);
              }
              future.completeExceptionally(e);
            }
          }
        });
    return future;
  }

  @Override
  public Response execute(
      final String userAgent,
      final Map<String, String> headers,
      final Verb httpVerb,
      final String completeUrl,
      final byte[] bodyContents)
      throws InterruptedException, ExecutionException, IOException {

    return doExecute(userAgent, headers, httpVerb, completeUrl, BodyType.BYTE_ARRAY, bodyContents);
  }

  @Override
  public Response execute(
      final String userAgent,
      final Map<String, String> headers,
      final Verb httpVerb,
      final String completeUrl,
      final MultipartPayload bodyContents)
      throws InterruptedException, ExecutionException, IOException {

    return doExecute(userAgent, headers, httpVerb, completeUrl, BodyType.MULTIPART, bodyContents);
  }

  @Override
  public Response execute(
      final String userAgent,
      final Map<String, String> headers,
      final Verb httpVerb,
      final String completeUrl,
      final String bodyContents)
      throws InterruptedException, ExecutionException, IOException {

    return doExecute(userAgent, headers, httpVerb, completeUrl, BodyType.STRING, bodyContents);
  }

  @Override
  public Response execute(
      final String userAgent,
      final Map<String, String> headers,
      final Verb httpVerb,
      final String completeUrl,
      final File bodyContents)
      throws InterruptedException, ExecutionException, IOException {

    return doExecute(userAgent, headers, httpVerb, completeUrl, BodyType.FILE, bodyContents);
  }

  private Response doExecute(
      final String userAgent,
      final Map<String, String> headers,
      final Verb httpVerb,
      final String completeUrl,
      final BodyType bodyType,
      final Object bodyContents)
      throws IOException {
    final Call call = createCall(userAgent, headers, httpVerb, completeUrl, bodyType, bodyContents);
    return convertResponse(call.execute());
  }

  private Call createCall(
      final String userAgent,
      final Map<String, String> headers,
      final Verb httpVerb,
      final String completeUrl,
      final BodyType bodyType,
      final Object bodyContents) {
    final Request.Builder requestBuilder = new Request.Builder();
    requestBuilder.url(completeUrl);

    final String method = httpVerb.name();

    final RequestBody body;
    if (bodyContents != null && HttpMethod.permitsRequestBody(method)) {
      final MediaType mediaType =
          headers.containsKey(CONTENT_TYPE)
              ? MediaType.parse(headers.get(CONTENT_TYPE))
              : DEFAULT_CONTENT_TYPE_MEDIA_TYPE;

      body = bodyType.createBody(mediaType, bodyContents);
    } else {
      body = null;
    }

    requestBuilder.method(method, body);

    for (final Map.Entry<String, String> header : headers.entrySet()) {
      requestBuilder.addHeader(header.getKey(), header.getValue());
    }

    if (userAgent != null) {
      requestBuilder.header(OAuthConstants.USER_AGENT_HEADER_NAME, userAgent);
    }

    return client.newCall(requestBuilder.build());
  }

  private enum BodyType {
    BYTE_ARRAY {
      @Override
      RequestBody createBody(final MediaType mediaType, final Object bodyContents) {
        return RequestBody.create((byte[]) bodyContents, mediaType);
      }
    },
    STRING {
      @Override
      RequestBody createBody(final MediaType mediaType, final Object bodyContents) {
        return RequestBody.create((String) bodyContents, mediaType);
      }
    },
    FILE {
      @Override
      RequestBody createBody(final MediaType mediaType, final Object bodyContents) {
        return RequestBody.create((File) bodyContents, mediaType);
      }
    },
    MULTIPART {
      @Override
      RequestBody createBody(final MediaType mediaType, final Object bodyContents) {
        final MultipartPayload multipartPayload = (MultipartPayload) bodyContents;
        final MultipartBody.Builder builder =
            new MultipartBody.Builder(multipartPayload.getBoundary());

        final String contentType = multipartPayload.getHeaders().get(CONTENT_TYPE);
        if (contentType != null) {
          builder.setType(MediaType.parse(contentType));
        }

        for (final BodyPartPayload part : multipartPayload.getBodyParts()) {
          final Headers.Builder headersBuilder = new Headers.Builder();
          final Map<String, String> partHeaders = part.getHeaders();
          String partContentType = null;
          if (partHeaders != null) {
            for (final Map.Entry<String, String> entry : partHeaders.entrySet()) {
              if (CONTENT_TYPE.equalsIgnoreCase(entry.getKey())) {
                partContentType = entry.getValue();
              } else {
                headersBuilder.add(entry.getKey(), entry.getValue());
              }
            }
          }

          final RequestBody partBody;
          if (part instanceof ByteArrayBodyPartPayload) {
            final ByteArrayBodyPartPayload byteArrayPart = (ByteArrayBodyPartPayload) part;
            partBody =
                RequestBody.create(
                    byteArrayPart.getPayload(),
                    partContentType == null ? null : MediaType.parse(partContentType),
                    byteArrayPart.getOff(),
                    byteArrayPart.getLen());
          } else if (part instanceof MultipartPayload) {
            partBody = createBody(null, part);
          } else {
            throw new IllegalArgumentException(
                "Unknown body part type: " + part.getClass().getName());
          }
          builder.addPart(headersBuilder.build(), partBody);
        }
        return builder.build();
      }
    };

    abstract RequestBody createBody(MediaType mediaType, Object bodyContents);
  }
}
