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

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.dpop.DPoPProofCreator;
import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.httpclient.HttpClientConfig;
import com.github.scribejava.core.model.*;
import com.github.scribejava.core.oauth2.grant.AuthorizationCodeGrant;
import com.github.scribejava.core.oauth2.grant.ClientCredentialsGrant;
import com.github.scribejava.core.oauth2.grant.OAuth20Grant;
import com.github.scribejava.core.oauth2.grant.PasswordGrant;
import com.github.scribejava.core.pkce.PKCE;
import com.github.scribejava.core.revoke.TokenTypeHint;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Service principal pour OAuth 2.0 (Refactorisé SOLID).
 */
public class OAuth20Service extends OAuthService
        implements OAuth20AsyncOperations, OAuth20Operations {

    private final DefaultApi20 api;
    private final String responseType;
    private final String defaultScope;
    private final OAuth20RequestSigner requestSigner;
    private final OAuth20RevocationHandler revocationHandler;
    private final OAuth20DeviceFlowHandler deviceFlowHandler;
    private final OAuth20PushedAuthHandler pushedAuthHandler;
    private final List<AuthorizationRequestInterceptor> authorizationRequestInterceptors;
    private AuthorizationRequestConverter authorizationRequestConverter = params -> params;

    /**
     * @param api              api
     * @param apiKey           apiKey
     * @param apiSecret        apiSecret
     * @param callback         callback
     * @param defaultScope     defaultScope
     * @param responseType     responseType
     * @param debugStream      debugStream
     * @param userAgent        userAgent
     * @param httpClientConfig httpClientConfig
     * @param httpClient       httpClient
     * @param dpopProofCreator dpopProofCreator
     */
    public OAuth20Service(
            DefaultApi20 api,
            String apiKey,
            String apiSecret,
            String callback,
            String defaultScope,
            String responseType,
            OutputStream debugStream,
            String userAgent,
            HttpClientConfig httpClientConfig,
            HttpClient httpClient,
            DPoPProofCreator dpopProofCreator) {
        super(apiKey, apiSecret, callback, debugStream, userAgent, httpClientConfig, httpClient);
        this.api = api;
        this.defaultScope = defaultScope;
        this.responseType = responseType;
        this.requestSigner = new OAuth20RequestSigner(api, dpopProofCreator);
        this.revocationHandler = new OAuth20RevocationHandler(this);
        this.deviceFlowHandler = new OAuth20DeviceFlowHandler(this);
        this.pushedAuthHandler = new OAuth20PushedAuthHandler(this);
        this.authorizationRequestInterceptors = new ArrayList<>();
    }

    /**
     * @param api              api
     * @param apiKey           apiKey
     * @param apiSecret        apiSecret
     * @param callback         callback
     * @param defaultScope     defaultScope
     * @param responseType     responseType
     * @param debugStream      debugStream
     * @param userAgent        userAgent
     * @param httpClientConfig httpClientConfig
     * @param httpClient       httpClient
     */
    public OAuth20Service(
            DefaultApi20 api,
            String apiKey,
            String apiSecret,
            String callback,
            String defaultScope,
            String responseType,
            OutputStream debugStream,
            String userAgent,
            HttpClientConfig httpClientConfig,
            HttpClient httpClient) {
        this(
                api,
                apiKey,
                apiSecret,
                callback,
                defaultScope,
                responseType,
                debugStream,
                userAgent,
                httpClientConfig,
                httpClient,
                null);
    }

    /**
     * @param interceptor interceptor
     */
    public void addAuthorizationRequestInterceptor(AuthorizationRequestInterceptor interceptor) {
        authorizationRequestInterceptors.add(interceptor);
    }

    /**
     * @return builder
     */
    public AuthorizationUrlBuilder createAuthorizationUrlBuilder() {
        return new AuthorizationUrlBuilder(this);
    }

    /**
     * @param redirectLocation redirectLocation
     * @return authorization
     */
    public OAuth2Authorization extractAuthorization(String redirectLocation) {
        final OAuth2Authorization authorization = new OAuth2Authorization();
        final int questionMarkIndex = redirectLocation.indexOf('?');
        String query =
                questionMarkIndex == -1
                        ? redirectLocation
                        : redirectLocation.substring(questionMarkIndex + 1);
        final int fragmentIndex = query.indexOf('#');
        if (fragmentIndex != -1) {
            query = query.substring(0, fragmentIndex);
        }
        for (String param : query.split("&")) {
            final String[] pair = param.split("=");
            if (pair.length > 0) {
                try {
                    final String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8.name());
                    final String value =
                            pair.length > 1 ? URLDecoder.decode(pair[1], StandardCharsets.UTF_8.name()) : "";
                    if (OAuthConstants.CODE.equals(key)) {
                        authorization.setCode(value);
                    } else if (OAuthConstants.STATE.equals(key)) {
                        authorization.setState(value);
                    }
                } catch (IOException e) {
                    // Ignored
                }
            }
        }
        return authorization;
    }

    /**
     * @return api
     */
    public DefaultApi20 getApi() {
        return api;
    }

    @Override
    public OAuth2AccessToken getAccessToken(OAuth20Grant grant)
            throws IOException, InterruptedException, ExecutionException {
        try {
            return getAccessTokenAsync(grant).get();
        } catch (ExecutionException e) {
            handleExecutionException(e);
            return null;
        }
    }

    /**
     * @param code code
     * @return token
     * @throws IOException          IOException
     * @throws InterruptedException InterruptedException
     * @throws ExecutionException   ExecutionException
     * @deprecated use {@link #getAccessToken(OAuth20Grant)}
     */
    @Deprecated
    public OAuth2AccessToken getAccessToken(String code)
            throws IOException, InterruptedException, ExecutionException {
        return getAccessToken(new AuthorizationCodeGrant(code));
    }

    /**
     * @param params params
     * @return token
     * @throws IOException          IOException
     * @throws InterruptedException InterruptedException
     * @throws ExecutionException   ExecutionException
     * @deprecated use {@link #getAccessToken(OAuth20Grant)}
     */
    @Deprecated
    public OAuth2AccessToken getAccessToken(AccessTokenRequestParams params)
            throws IOException, InterruptedException, ExecutionException {
        return getAccessToken(new AuthorizationCodeGrant(params.getCode()));
    }

    /**
     * @param code     code
     * @param callback callback
     * @return future
     * @deprecated use {@link #getAccessTokenAsync(OAuth20Grant, OAuthAsyncRequestCallback)}
     */
    @Deprecated
    public CompletableFuture<OAuth2AccessToken> getAccessToken(
            String code, OAuthAsyncRequestCallback<OAuth2AccessToken> callback) {
        return getAccessTokenAsync(new AuthorizationCodeGrant(code), callback);
    }

    @Override
    public CompletableFuture<OAuth2AccessToken> getAccessTokenAsync(OAuth20Grant grant) {
        return getAccessTokenAsync(grant, null);
    }

    @Override
    public CompletableFuture<OAuth2AccessToken> getAccessTokenAsync(
            OAuth20Grant grant, OAuthAsyncRequestCallback<OAuth2AccessToken> callback) {
        return sendAccessTokenRequestAsync(grant.createRequest(this), callback);
    }

    /**
     * @param code code
     * @return future
     * @deprecated use {@link #getAccessTokenAsync(OAuth20Grant)}
     */
    @Deprecated
    public CompletableFuture<OAuth2AccessToken> getAccessTokenAsync(String code) {
        return getAccessTokenAsync(new AuthorizationCodeGrant(code));
    }

    /**
     * @param code     code
     * @param callback callback
     * @return future
     * @deprecated use {@link #getAccessTokenAsync(OAuth20Grant, OAuthAsyncRequestCallback)}
     */
    @Deprecated
    public CompletableFuture<OAuth2AccessToken> getAccessTokenAsync(
            String code, OAuthAsyncRequestCallback<OAuth2AccessToken> callback) {
        return getAccessTokenAsync(new AuthorizationCodeGrant(code), callback);
    }

    /**
     * @return future
     * @deprecated use {@link #getAccessTokenAsync(OAuth20Grant)}
     */
    @Deprecated
    public CompletableFuture<OAuth2AccessToken> getAccessTokenClientCredentialsGrantAsync() {
        return getAccessTokenAsync(new ClientCredentialsGrant());
    }

    /**
     * @param username username
     * @param password password
     * @return future
     * @deprecated use {@link #getAccessTokenAsync(OAuth20Grant)}
     */
    @Deprecated
    public CompletableFuture<OAuth2AccessToken> getAccessTokenPasswordGrantAsync(
            String username, String password) {
        return getAccessTokenAsync(new PasswordGrant(username, password));
    }

    /**
     * @param username username
     * @param password password
     * @return token
     * @throws IOException          IOException
     * @throws InterruptedException InterruptedException
     * @throws ExecutionException   ExecutionException
     * @deprecated use {@link #getAccessToken(OAuth20Grant)}
     */
    @Deprecated
    public OAuth2AccessToken getAccessTokenPasswordGrant(String username, String password)
            throws IOException, InterruptedException, ExecutionException {
        return getAccessToken(new PasswordGrant(username, password));
    }

    /**
     * @return token
     * @throws IOException          IOException
     * @throws InterruptedException InterruptedException
     * @throws ExecutionException   ExecutionException
     * @deprecated use {@link #getAccessToken(OAuth20Grant)}
     */
    @Deprecated
    public OAuth2AccessToken getAccessTokenClientCredentialsGrant()
            throws IOException, InterruptedException, ExecutionException {
        return getAccessToken(new ClientCredentialsGrant());
    }

    /**
     * @return list
     */
    public List<AuthorizationRequestInterceptor> getAuthorizationRequestInterceptors() {
        return authorizationRequestInterceptors;
    }

    /**
     * @return converter
     */
    public AuthorizationRequestConverter getAuthorizationRequestConverter() {
        return authorizationRequestConverter;
    }

    /**
     * @param converter converter
     */
    public void setAuthorizationRequestConverter(AuthorizationRequestConverter converter) {
        this.authorizationRequestConverter = converter;
    }

    /**
     * @return url
     */
    public String getAuthorizationUrl() {
        return createAuthorizationUrlBuilder().build();
    }

    /**
     * @param pkce pkce
     * @return url
     */
    public String getAuthorizationUrl(PKCE pkce) {
        return createAuthorizationUrlBuilder().pkce(pkce).build();
    }

    /**
     * @return defaultScope
     */
    public String getDefaultScope() {
        return defaultScope;
    }

    /**
     * @return codes
     * @throws InterruptedException InterruptedException
     * @throws ExecutionException   ExecutionException
     * @throws IOException          IOException
     */
    public DeviceAuthorization getDeviceAuthorizationCodes()
            throws InterruptedException, ExecutionException, IOException {
        return deviceFlowHandler.getDeviceAuthorizationCodes(null);
    }

    /**
     * @return future
     */
    public CompletableFuture<DeviceAuthorization> getDeviceAuthorizationCodesAsync() {
        return getDeviceAuthorizationCodesAsync(null);
    }

    /**
     * @param callback callback
     * @return future
     */
    public CompletableFuture<DeviceAuthorization> getDeviceAuthorizationCodesAsync(
            OAuthAsyncRequestCallback<DeviceAuthorization> callback) {
        return CompletableFuture.supplyAsync(
                () -> {
                    try {
                        return getDeviceAuthorizationCodes();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * @return responseType
     */
    public String getResponseType() {
        return responseType;
    }

    @Override
    public String getVersion() {
        return "2.0";
    }

    /**
     * @param deviceAuthorization deviceAuthorization
     * @return token
     * @throws InterruptedException InterruptedException
     * @throws ExecutionException   ExecutionException
     * @throws IOException          IOException
     */
    public OAuth2AccessToken pollAccessTokenDeviceAuthorizationGrant(
            DeviceAuthorization deviceAuthorization)
            throws InterruptedException, ExecutionException, IOException {
        return deviceFlowHandler.pollAccessTokenDeviceAuthorizationGrant(deviceAuthorization);
    }

    /**
     * @param responseType     responseType
     * @param apiKey           apiKey
     * @param callback         callback
     * @param scope            scope
     * @param state            state
     * @param additionalParams additionalParams
     * @return future
     */
    public CompletableFuture<PushedAuthorizationResponse> pushAuthorizationRequestAsync(
            String responseType,
            String apiKey,
            String callback,
            String scope,
            String state,
            Map<String, String> additionalParams) {
        return pushedAuthHandler.pushAuthorizationRequestAsync(
                responseType, apiKey, callback, scope, state, additionalParams, null);
    }

    @Override
    public OAuth2AccessToken refreshAccessToken(String refreshToken)
            throws IOException, InterruptedException, ExecutionException {
        try {
            return refreshAccessTokenAsync(refreshToken).get();
        } catch (ExecutionException e) {
            handleExecutionException(e);
            return null;
        }
    }

    /**
     * @param refreshToken refreshToken
     * @param callback     callback
     * @return future
     * @deprecated use {@link #refreshAccessTokenAsync(String, String, OAuthAsyncRequestCallback)}
     */
    @Deprecated
    public CompletableFuture<OAuth2AccessToken> refreshAccessToken(
            String refreshToken, OAuthAsyncRequestCallback<OAuth2AccessToken> callback) {
        return refreshAccessTokenAsync(refreshToken, null, callback);
    }

    @Override
    public CompletableFuture<OAuth2AccessToken> refreshAccessTokenAsync(String refreshToken) {
        return refreshAccessTokenAsync(refreshToken, (String) null);
    }

    /**
     * @param refreshToken refreshToken
     * @param scope        scope
     * @return future
     */
    public CompletableFuture<OAuth2AccessToken> refreshAccessTokenAsync(
            String refreshToken, String scope) {
        return refreshAccessTokenAsync(refreshToken, scope, null);
    }

    /**
     * @param refreshToken refreshToken
     * @param scope        scope
     * @param callback     callback
     * @return future
     */
    public CompletableFuture<OAuth2AccessToken> refreshAccessTokenAsync(
            String refreshToken, String scope, OAuthAsyncRequestCallback<OAuth2AccessToken> callback) {
        return sendAccessTokenRequestAsync(createRefreshTokenRequest(refreshToken, scope), callback);
    }

    /**
     * @param token         token
     * @param tokenTypeHint tokenTypeHint
     * @throws IOException          IOException
     * @throws InterruptedException InterruptedException
     * @throws ExecutionException   ExecutionException
     */
    public void revokeToken(String token, TokenTypeHint tokenTypeHint)
            throws IOException, InterruptedException, ExecutionException {
        try {
            revokeTokenAsync(token, tokenTypeHint).get();
        } catch (ExecutionException e) {
            handleExecutionException(e);
        }
    }

    /**
     * @param token token
     * @throws IOException          IOException
     * @throws InterruptedException InterruptedException
     * @throws ExecutionException   ExecutionException
     * @deprecated use {@link #revokeTokenAsync(String, TokenTypeHint)}
     */
    @Deprecated
    public void revokeToken(String token)
            throws IOException, InterruptedException, ExecutionException {
        revokeToken(token, null);
    }

    @Override
    public CompletableFuture<Void> revokeTokenAsync(String token, TokenTypeHint tokenTypeHint) {
        return revocationHandler.revokeTokenAsync(token, tokenTypeHint);
    }

    /**
     * @param token token
     * @return future
     * @deprecated use {@link #revokeTokenAsync(String, TokenTypeHint)}
     */
    @Deprecated
    public CompletableFuture<Void> revokeTokenAsync(String token) {
        return revokeTokenAsync(token, null);
    }

    /**
     * @param accessToken accessToken
     * @param request     request
     */
    public void signRequest(String accessToken, OAuthRequest request) {
        requestSigner.signRequest(accessToken, request);
    }

    /**
     * @param accessToken accessToken
     * @param request     request
     */
    public void signRequest(OAuth2AccessToken accessToken, OAuthRequest request) {
        requestSigner.signRequest(accessToken, request);
    }

    /**
     * @param request  request
     * @param callback callback
     * @return future
     */
    protected CompletableFuture<OAuth2AccessToken> sendAccessTokenRequestAsync(
            OAuthRequest request, OAuthAsyncRequestCallback<OAuth2AccessToken> callback) {
        return execute(
                request,
                callback,
                response -> {
                    return api.getAccessTokenExtractor().extract(response);
                });
    }

    /**
     * @param refreshToken refreshToken
     * @param scope        scope
     * @return request
     */
    protected OAuthRequest createRefreshTokenRequest(String refreshToken, String scope) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new IllegalArgumentException("The refreshToken cannot be null or empty");
        }
        final OAuthRequest request =
                new OAuthRequest(api.getAccessTokenVerb(), api.getRefreshTokenEndpoint());
        api.getClientAuthentication().addClientAuthentication(request, getApiKey(), getApiSecret());
        final String effectiveScope = scope != null ? scope : defaultScope;
        if (effectiveScope != null) {
            request.addParameter(OAuthConstants.SCOPE, effectiveScope);
        }
        request.addParameter(OAuthConstants.REFRESH_TOKEN, refreshToken);
        request.addParameter(OAuthConstants.GRANT_TYPE, OAuthConstants.REFRESH_TOKEN);
        return request;
    }

    private void handleExecutionException(ExecutionException e)
            throws IOException, ExecutionException {
        final Throwable cause = e.getCause();
        if (cause instanceof IOException) {
            throw (IOException) cause;
        }
        if (cause instanceof RuntimeException) {
            throw (RuntimeException) cause;
        }
        throw e;
    }

    /**
     * @param token token
     * @param hint  hint
     * @return request
     */
    protected OAuthRequest createRevokeTokenRequest(String token, TokenTypeHint hint) {
        return revocationHandler.createRevokeTokenRequest(token, hint);
    }

    /**
     * @param username username
     * @param password password
     * @param scope    scope
     * @return request
     */
    protected OAuthRequest createAccessTokenPasswordGrantRequest(
            String username, String password, String scope) {
        return new PasswordGrant(username, password, scope).createRequest(this);
    }

    /**
     * @param scope scope
     * @return request
     */
    protected OAuthRequest createAccessTokenClientCredentialsGrantRequest(String scope) {
        return new ClientCredentialsGrant(scope).createRequest(this);
    }
}
