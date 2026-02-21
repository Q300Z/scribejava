package com.github.scribejava.core.builder;

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.httpclient.HttpClientConfig;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.oauth.OAuthService;
import com.github.scribejava.core.utils.Preconditions;

import java.io.OutputStream;

/**
 * Implementation of the Builder pattern, with a fluent interface that creates a {@link OAuthService}
 */
public class ServiceBuilder implements ServiceBuilderOAuth20 {

    private String callback;
    private String apiKey;
    private String apiSecret;
    private String scope;
    private OutputStream debugStream;
    private String responseType = "code";
    private String userAgent;

    private HttpClientConfig httpClientConfig;
    private HttpClient httpClient;

    private String discoveryIssuer;
    private com.github.scribejava.core.oauth.DiscoveryService discoveryService;

    public ServiceBuilder(String apiKey) {
        apiKey(apiKey);
    }

    public ServiceBuilder discoverFromIssuer(String issuer, com.github.scribejava.core.oauth.DiscoveryService service) {
        this.discoveryIssuer = issuer;
        this.discoveryService = service;
        return this;
    }

    @Override
    public ServiceBuilder callback(String callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public final ServiceBuilder apiKey(String apiKey) {
        Preconditions.checkEmptyString(apiKey, "Invalid Api key");
        this.apiKey = apiKey;
        return this;
    }

    @Override
    public ServiceBuilder apiSecret(String apiSecret) {
        Preconditions.checkEmptyString(apiSecret, "Invalid Api secret");
        this.apiSecret = apiSecret;
        return this;
    }

    @Override
    public ServiceBuilder apiSecretIsEmptyStringUnsafe() {
        apiSecret = "";
        return this;
    }

    private ServiceBuilder setScope(String scope) {
        Preconditions.checkEmptyString(scope, "Invalid OAuth scope");
        this.scope = scope;
        return this;
    }

    @Override
    public ServiceBuilderOAuth20 defaultScope(String defaultScope) {
        return setScope(defaultScope);
    }

    @Override
    public ServiceBuilderOAuth20 defaultScope(ScopeBuilder scopeBuilder) {
        return setScope(scopeBuilder.build());
    }

    @Override
    public ServiceBuilder debugStream(OutputStream debugStream) {
        Preconditions.checkNotNull(debugStream, "debug stream can't be null");
        this.debugStream = debugStream;
        return this;
    }

    @Override
    public ServiceBuilderOAuth20 responseType(String responseType) {
        Preconditions.checkEmptyString(responseType, "Invalid OAuth responseType");
        this.responseType = responseType;
        return this;
    }

    @Override
    public ServiceBuilder httpClientConfig(HttpClientConfig httpClientConfig) {
        Preconditions.checkNotNull(httpClientConfig, "httpClientConfig can't be null");
        this.httpClientConfig = httpClientConfig;
        return this;
    }

    @Override
    public ServiceBuilder httpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    @Override
    public ServiceBuilder userAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    @Override
    public ServiceBuilder debug() {
        return debugStream(System.out);
    }

    @Override
    public OAuth20Service build(final DefaultApi20 api) {
        DefaultApi20 apiToUse = api;
        if (discoveryIssuer != null && discoveryService != null) {
            try {
                final com.github.scribejava.core.oauth.DiscoveredEndpoints endpoints = discoveryService
                        .discoverAsync(discoveryIssuer).get();
                apiToUse = new DefaultApi20() {
                    @Override
                    public String getAccessTokenEndpoint() {
                        return endpoints.getTokenEndpoint();
                    }

                    @Override
                    public String getAuthorizationBaseUrl() {
                        return endpoints.getAuthorizationEndpoint();
                    }
                };
            } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
                throw new com.github.scribejava.core.exceptions.OAuthException("Failed to discover endpoints", e);
            }
        }
        return apiToUse.createService(apiKey, apiSecret, callback, scope, responseType, debugStream, userAgent,
                httpClientConfig, httpClient);
    }
}
