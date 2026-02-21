package com.github.scribejava.core.oauth;

import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.httpclient.HttpClientConfig;
import com.github.scribejava.core.httpclient.HttpClientProvider;
import com.github.scribejava.core.httpclient.jdk.JDKHttpClient;
import com.github.scribejava.core.httpclient.jdk.JDKHttpClientConfig;
import com.github.scribejava.core.model.OAuthAsyncRequestCallback;
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

public abstract class OAuthService implements Closeable {

    private final String apiKey;
    private final String apiSecret;
    private final String callback;
    private final String userAgent;
    private final HttpClient httpClient;
    private final OutputStream debugStream;
    private final List<OAuthRequestInterceptor> interceptors = new ArrayList<>(); // ADDED

    public OAuthService(String apiKey, String apiSecret, String callback, OutputStream debugStream,
                        String userAgent, HttpClientConfig httpClientConfig, HttpClient httpClient) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.callback = callback;
        this.debugStream = debugStream;
        this.userAgent = userAgent;

        if (httpClientConfig == null && httpClient == null) {
            this.httpClient = new JDKHttpClient(JDKHttpClientConfig.defaultConfig());
        } else {
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

    public void addInterceptor(OAuthRequestInterceptor interceptor) { // ADDED
        interceptors.add(interceptor);
    }

    @Override
    public void close() throws IOException {
        httpClient.close();
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

    public abstract String getVersion();

    public CompletableFuture<Response> executeAsync(OAuthRequest request) {
        return execute(request, null);
    }

    public CompletableFuture<Response> execute(OAuthRequest request, OAuthAsyncRequestCallback<Response> callback) {
        return execute(request, callback, null);
    }

    public <R> CompletableFuture<R> execute(OAuthRequest request, OAuthAsyncRequestCallback<R> callback,
                                            OAuthRequest.ResponseConverter<R> converter) {

        interceptors.forEach(interceptor -> interceptor.intercept(request)); // ADDED: Execute interceptors

        final File filePayload = request.getFilePayload();
        if (filePayload != null) {
            return httpClient.executeAsync(userAgent, request.getHeaders(), request.getVerb(), request.getCompleteUrl(),
                    filePayload, callback, converter);
        } else if (request.getStringPayload() != null) {
            return httpClient.executeAsync(userAgent, request.getHeaders(), request.getVerb(), request.getCompleteUrl(),
                    request.getStringPayload(), callback, converter);
        } else if (request.getMultipartPayload() != null) {
            return httpClient.executeAsync(userAgent, request.getHeaders(), request.getVerb(), request.getCompleteUrl(),
                    request.getMultipartPayload(), callback, converter);
        } else {
            return httpClient.executeAsync(userAgent, request.getHeaders(), request.getVerb(), request.getCompleteUrl(),
                    request.getByteArrayPayload(), callback, converter);
        }
    }

    public Response execute(OAuthRequest request) throws InterruptedException, ExecutionException, IOException {
        interceptors.forEach(interceptor -> interceptor.intercept(request)); // ADDED: Execute interceptors

        final File filePayload = request.getFilePayload();
        if (filePayload != null) {
            return httpClient.execute(userAgent, request.getHeaders(), request.getVerb(), request.getCompleteUrl(),
                    filePayload);
        } else if (request.getStringPayload() != null) {
            return httpClient.execute(userAgent, request.getHeaders(), request.getVerb(), request.getCompleteUrl(),
                    request.getStringPayload());
        } else if (request.getMultipartPayload() != null) {
            return httpClient.execute(userAgent, request.getHeaders(), request.getVerb(), request.getCompleteUrl(),
                    request.getMultipartPayload());
        } else {
            return httpClient.execute(userAgent, request.getHeaders(), request.getVerb(), request.getCompleteUrl(),
                    request.getByteArrayPayload());
        }
    }

    public void log(String message) {
        if (debugStream != null) {
            log(message, (Object[]) null);
        }
    }

    public void log(String messagePattern, Object... params) {
        final String message = params == null || params.length == 0 ? messagePattern
                : String.format(messagePattern, params);
        final String messageWithNewline = message + '\n';
        try {
            debugStream.write(messageWithNewline.getBytes(StandardCharsets.UTF_8));
        } catch (IOException | RuntimeException e) {
            throw new RuntimeException("there were problems while writting to the debug stream", e);
        }
    }

    protected boolean isDebug() {
        return debugStream != null;
    }
}
