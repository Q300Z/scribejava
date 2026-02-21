package com.github.scribejava.httpclient.armeria;

import com.github.scribejava.core.httpclient.AbstractAsyncOnlyHttpClient;
import com.github.scribejava.core.httpclient.multipart.MultipartPayload;
import com.github.scribejava.core.httpclient.multipart.MultipartUtils;
import com.github.scribejava.core.model.*;
import com.github.scribejava.core.model.Response;
import com.linecorp.armeria.client.WebClient;
import com.linecorp.armeria.common.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * An implementation of {@link AbstractAsyncOnlyHttpClient} based on
 * <a href="https://line.github.io/armeria/">Armeria HTTP client</a>.
 */
public class ArmeriaHttpClient extends AbstractAsyncOnlyHttpClient {

    private final ArmeriaWebClientBuilder clientBuilder;
    private final Map<String, WebClient> httpClients = new HashMap<>();
    private final ReentrantReadWriteLock httpClientsLock = new ReentrantReadWriteLock();

    public ArmeriaHttpClient() {
        this(ArmeriaHttpClientConfig.defaultConfig());
    }

    public ArmeriaHttpClient(final ArmeriaHttpClientConfig config) {
        clientBuilder = config.createClientBuilder();
    }

    private static String getEndPoint(final URI uri) {
        return requireNonNull(uri.getScheme(), "scheme") + "://" + requireNonNull(uri.getAuthority(), "authority");
    }

    private static String getServicePath(final URI uri) {
        final StringBuilder builder = new StringBuilder()
                .append(requireNonNull(uri.getPath(), "path"));
        final String query = uri.getQuery();
        if (query != null) {
            builder.append('?').append(query);
        }
        final String fragment = uri.getFragment();
        if (fragment != null) {
            builder.append('#').append(fragment);
        }
        return builder.toString();
    }

    private static HttpMethod getHttpMethod(final Verb httpVerb) {
        switch (httpVerb) {
            case GET:
                return HttpMethod.GET;
            case POST:
                return HttpMethod.POST;
            case PUT:
                return HttpMethod.PUT;
            case DELETE:
                return HttpMethod.DELETE;
            case HEAD:
                return HttpMethod.HEAD;
            case OPTIONS:
                return HttpMethod.OPTIONS;
            case TRACE:
                return HttpMethod.TRACE;
            case PATCH:
                return HttpMethod.PATCH;
            default:
                throw new IllegalArgumentException("message build error: unsupported HTTP method: " + httpVerb.name());
        }
    }

    @Override
    public void close() {
        final Lock writeLock = httpClientsLock.writeLock();
        writeLock.lock();
        try {
            httpClients.clear();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public <T> CompletableFuture<T> executeAsync(final String userAgent, final Map<String, String> headers,
                                                 final Verb httpVerb, final String completeUrl, final byte[] bodyContents,
                                                 final OAuthAsyncRequestCallback<T> callback, final OAuthRequest.ResponseConverter<T> converter) {
        return doExecuteAsync(userAgent, headers, httpVerb, completeUrl, new BytesBody(bodyContents), callback,
                converter);
    }

    @Override
    public <T> CompletableFuture<T> executeAsync(final String userAgent, final Map<String, String> headers,
                                                 final Verb httpVerb, final String completeUrl, final MultipartPayload bodyContents,
                                                 final OAuthAsyncRequestCallback<T> callback, final OAuthRequest.ResponseConverter<T> converter) {
        return doExecuteAsync(userAgent, headers, httpVerb, completeUrl, new MultipartBody(bodyContents), callback,
                converter);
    }

    @Override
    public <T> CompletableFuture<T> executeAsync(final String userAgent, final Map<String, String> headers,
                                                 final Verb httpVerb, final String completeUrl, final String bodyContents,
                                                 final OAuthAsyncRequestCallback<T> callback, final OAuthRequest.ResponseConverter<T> converter) {
        return doExecuteAsync(userAgent, headers, httpVerb, completeUrl, new StringBody(bodyContents), callback,
                converter);
    }

    @Override
    public <T> CompletableFuture<T> executeAsync(final String userAgent, final Map<String, String> headers,
                                                 final Verb httpVerb, final String completeUrl, final File bodyContents,
                                                 final OAuthAsyncRequestCallback<T> callback, final OAuthRequest.ResponseConverter<T> converter) {
        return doExecuteAsync(userAgent, headers, httpVerb, completeUrl, new FileBody(bodyContents), callback,
                converter);
    }

    private <T> CompletableFuture<T> doExecuteAsync(final String userAgent, final Map<String, String> headers,
                                                    final Verb httpVerb, final String completeUrl, final Supplier<HttpData> contentSupplier,
                                                    final OAuthAsyncRequestCallback<T> callback, final OAuthRequest.ResponseConverter<T> converter) {

        final URI uri = URI.create(completeUrl);
        final String path = getServicePath(uri);
        final WebClient client = getClient(uri);

        final RequestHeadersBuilder headersBuilder = RequestHeaders.of(getHttpMethod(httpVerb), path).toBuilder();
        headersBuilder.add(headers.entrySet());
        if (userAgent != null) {
            headersBuilder.add(OAuthConstants.USER_AGENT_HEADER_NAME, userAgent);
        }

        final HttpResponse response;
        if (httpVerb.isPermitBody()) {
            final HttpData contents = contentSupplier.get();
            if (httpVerb.isRequiresBody() && contents == null) {
                throw new IllegalArgumentException("Contents missing for request method " + httpVerb.name());
            }

            if (headersBuilder.contentType() == null) {
                headersBuilder.contentType(MediaType.FORM_DATA);
            }

            if (contents != null) {
                response = client.execute(headersBuilder.build(), contents);
            } else {
                response = client.execute(headersBuilder.build());
            }
        } else {
            response = client.execute(headersBuilder.build());
        }

        final CompletableFuture<AggregatedHttpResponse> aggregateFuture = response.aggregate();
        final CompletableFuture<T> resultFuture = aggregateFuture
                .thenApply(aggregatedResponse -> whenResponseComplete(callback, converter, aggregatedResponse))
                .exceptionally(throwable -> completeExceptionally(callback, throwable));

        resultFuture.whenComplete((t, throwable) -> {
            if (resultFuture.isCancelled()) {
                aggregateFuture.cancel(true);
            }
        });

        return resultFuture;
    }

    private WebClient getClient(final URI uri) {
        final String endpoint = getEndPoint(uri);

        WebClient client;
        final Lock readLock = httpClientsLock.readLock();
        readLock.lock();
        try {
            client = httpClients.get(endpoint);
        } finally {
            readLock.unlock();
        }

        if (client != null) {
            return client;
        }

        client = clientBuilder.newWebClient(
                requireNonNull(uri.getScheme(), "scheme"),
                requireNonNull(uri.getAuthority(), "authority"));

        final Lock writeLock = httpClientsLock.writeLock();
        writeLock.lock();
        try {
            if (!httpClients.containsKey(endpoint)) {
                httpClients.put(endpoint, client);
                return client;
            } else {
                return httpClients.get(endpoint);
            }
        } finally {
            writeLock.unlock();
        }
    }

    private Response convertResponse(final AggregatedHttpResponse aggregatedResponse) {
        final Map<String, String> headersMap = new HashMap<>();
        aggregatedResponse.headers().forEach((header, value) -> headersMap.put(header.toString(), value));

        final HttpStatus status = aggregatedResponse.status();
        final InputStream inputStream = aggregatedResponse.content().toInputStream();

        return new Response(status.code(), status.reasonPhrase(), headersMap, inputStream);
    }

    private <T> T whenResponseComplete(final OAuthAsyncRequestCallback<T> callback,
                                       final OAuthRequest.ResponseConverter<T> converter, final AggregatedHttpResponse aggregatedResponse) {
        final Response response = convertResponse(aggregatedResponse);
        try {
            @SuppressWarnings("unchecked") final T t = converter == null ? (T) response : converter.convert(response);
            if (callback != null) {
                callback.onCompleted(t);
            }
            return t;
        } catch (final IOException | RuntimeException e) {
            return completeExceptionally(callback, e);
        }
    }

    private <T> T completeExceptionally(final OAuthAsyncRequestCallback<T> callback, final Throwable throwable) {
        if (callback != null) {
            callback.onThrowable(throwable);
        }
        if (throwable instanceof RuntimeException) {
            throw (RuntimeException) throwable;
        }
        throw new RuntimeException(throwable);
    }

    private static class BytesBody implements Supplier<HttpData> {
        private final byte[] bodyContents;

        BytesBody(final byte[] bodyContents) {
            this.bodyContents = bodyContents;
        }

        @Override
        public HttpData get() {
            return (bodyContents != null) ? HttpData.wrap(bodyContents) : null;
        }
    }

    private static class StringBody implements Supplier<HttpData> {
        private final String bodyContents;

        StringBody(final String bodyContents) {
            this.bodyContents = bodyContents;
        }

        @Override
        public HttpData get() {
            return (bodyContents != null) ? HttpData.ofUtf8(bodyContents) : null;
        }
    }

    private static class FileBody implements Supplier<HttpData> {
        private final File bodyContents;

        FileBody(final File bodyContents) {
            this.bodyContents = bodyContents;
        }

        @Override
        public HttpData get() {
            try {
                return (bodyContents != null) ? HttpData.wrap(Files.readAllBytes(bodyContents.toPath())) : null;
            } catch (final IOException ioE) {
                throw new RuntimeException(ioE);
            }
        }
    }

    private static class MultipartBody implements Supplier<HttpData> {
        private final MultipartPayload bodyContents;

        MultipartBody(final MultipartPayload bodyContents) {
            this.bodyContents = bodyContents;
        }

        @Override
        public HttpData get() {
            try {
                return (bodyContents != null) ? HttpData.wrap(MultipartUtils.getPayload(bodyContents).toByteArray())
                        : null;
            } catch (final IOException ioE) {
                throw new RuntimeException(ioE);
            }
        }
    }
}
