package com.github.scribejava.httpclient.okhttp;

import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.httpclient.multipart.MultipartPayload;
import com.github.scribejava.core.model.OAuthAsyncRequestCallback;
import com.github.scribejava.core.model.OAuthConstants;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.internal.http.HttpMethod;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import com.github.scribejava.core.model.Response;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import okhttp3.Cache;
import okhttp3.Headers;
import okhttp3.ResponseBody;

public class OkHttpHttpClient implements HttpClient {

    private static final MediaType DEFAULT_CONTENT_TYPE_MEDIA_TYPE = MediaType.parse(DEFAULT_CONTENT_TYPE);

    private final OkHttpClient client;

    public OkHttpHttpClient() {
        this(OkHttpHttpClientConfig.defaultConfig());
    }

    public OkHttpHttpClient(final OkHttpHttpClientConfig config) {
        final OkHttpClient.Builder clientBuilder = config.getClientBuilder();
        client = clientBuilder == null ? new OkHttpClient() : clientBuilder.build();
    }

    public OkHttpHttpClient(final OkHttpClient client) {
        this.client = client;
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
    public <T> CompletableFuture<T> executeAsync(final String userAgent, final Map<String, String> headers,
            final Verb httpVerb, final String completeUrl, final byte[] bodyContents,
            final OAuthAsyncRequestCallback<T> callback, final OAuthRequest.ResponseConverter<T> converter) {

        return doExecuteAsync(userAgent, headers, httpVerb, completeUrl, BodyType.BYTE_ARRAY, bodyContents, callback,
                converter);
    }

    @Override
    public <T> CompletableFuture<T> executeAsync(final String userAgent, final Map<String, String> headers,
            final Verb httpVerb, final String completeUrl, final MultipartPayload bodyContents,
            final OAuthAsyncRequestCallback<T> callback, final OAuthRequest.ResponseConverter<T> converter) {

        throw new UnsupportedOperationException("OKHttpClient does not support Multipart payload for the moment");
    }

    @Override
    public <T> CompletableFuture<T> executeAsync(final String userAgent, final Map<String, String> headers,
            final Verb httpVerb, final String completeUrl, final String bodyContents,
            final OAuthAsyncRequestCallback<T> callback, final OAuthRequest.ResponseConverter<T> converter) {

        return doExecuteAsync(userAgent, headers, httpVerb, completeUrl, BodyType.STRING, bodyContents, callback,
                converter);
    }

    @Override
    public <T> CompletableFuture<T> executeAsync(final String userAgent, final Map<String, String> headers,
            final Verb httpVerb, final String completeUrl, final File bodyContents,
            final OAuthAsyncRequestCallback<T> callback, final OAuthRequest.ResponseConverter<T> converter) {

        return doExecuteAsync(userAgent, headers, httpVerb, completeUrl, BodyType.FILE, bodyContents, callback,
                converter);
    }

    private <T> CompletableFuture<T> doExecuteAsync(final String userAgent, final Map<String, String> headers,
            final Verb httpVerb, final String completeUrl, final BodyType bodyType, final Object bodyContents,
            final OAuthAsyncRequestCallback<T> callback, final OAuthRequest.ResponseConverter<T> converter) {

        final CompletableFuture<T> future = new CompletableFuture<>();
        final Call call = createCall(userAgent, headers, httpVerb, completeUrl, bodyType, bodyContents);

        future.whenComplete((t, throwable) -> {
            if (future.isCancelled()) {
                call.cancel();
            }
        });

        call.enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                if (callback != null) {
                    callback.onThrowable(e);
                }
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(final Call call, final okhttp3.Response response) throws IOException {
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
    public Response execute(final String userAgent, final Map<String, String> headers, final Verb httpVerb,
            final String completeUrl, final byte[] bodyContents)
            throws InterruptedException, ExecutionException, IOException {

        return doExecute(userAgent, headers, httpVerb, completeUrl, BodyType.BYTE_ARRAY, bodyContents);
    }

    @Override
    public Response execute(final String userAgent, final Map<String, String> headers, final Verb httpVerb,
            final String completeUrl, final MultipartPayload bodyContents)
            throws InterruptedException, ExecutionException, IOException {

        throw new UnsupportedOperationException("OKHttpClient does not support Multipart payload for the moment");
    }

    @Override
    public Response execute(final String userAgent, final Map<String, String> headers, final Verb httpVerb,
            final String completeUrl, final String bodyContents)
            throws InterruptedException, ExecutionException, IOException {

        return doExecute(userAgent, headers, httpVerb, completeUrl, BodyType.STRING, bodyContents);
    }

    @Override
    public Response execute(final String userAgent, final Map<String, String> headers, final Verb httpVerb,
            final String completeUrl, final File bodyContents)
            throws InterruptedException, ExecutionException, IOException {

        return doExecute(userAgent, headers, httpVerb, completeUrl, BodyType.FILE, bodyContents);
    }

    private Response doExecute(final String userAgent, final Map<String, String> headers, final Verb httpVerb,
            final String completeUrl, final BodyType bodyType, final Object bodyContents) throws IOException {
        final Call call = createCall(userAgent, headers, httpVerb, completeUrl, bodyType, bodyContents);
        return convertResponse(call.execute());
    }

    private Call createCall(final String userAgent, final Map<String, String> headers, final Verb httpVerb,
            final String completeUrl, final BodyType bodyType, final Object bodyContents) {
        final Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(completeUrl);

        final String method = httpVerb.name();

        final RequestBody body;
        if (bodyContents != null && HttpMethod.permitsRequestBody(method)) {
            final MediaType mediaType = headers.containsKey(CONTENT_TYPE) ? MediaType.parse(headers.get(CONTENT_TYPE))
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
        };

        abstract RequestBody createBody(MediaType mediaType, Object bodyContents);
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

}
