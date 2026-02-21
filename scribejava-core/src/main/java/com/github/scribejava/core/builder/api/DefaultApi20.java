package com.github.scribejava.core.builder.api;

import com.github.scribejava.core.dpop.DPoPProofCreator;
import com.github.scribejava.core.extractors.DeviceAuthorizationJsonExtractor;
import com.github.scribejava.core.extractors.OAuth2AccessTokenJsonExtractor;
import com.github.scribejava.core.extractors.TokenExtractor;
import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.httpclient.HttpClientConfig;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthConstants;
import com.github.scribejava.core.model.ParameterList;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.oauth2.bearersignature.BearerSignature;
import com.github.scribejava.core.oauth2.bearersignature.BearerSignatureAuthorizationRequestHeaderField;
import com.github.scribejava.core.oauth2.clientauthentication.ClientAuthentication;
import com.github.scribejava.core.oauth2.clientauthentication.HttpBasicAuthenticationScheme;

import java.io.OutputStream;
import java.util.Map;

/**
 * Default implementation of the OAuth protocol, version 2.0
 */
public abstract class DefaultApi20 {

    /**
     * Returns the access token extractor.
     *
     * @return access token extractor
     */
    public TokenExtractor<OAuth2AccessToken> getAccessTokenExtractor() {
        return OAuth2AccessTokenJsonExtractor.instance();
    }

    /**
     * Returns the verb for the access token endpoint (defaults to POST)
     *
     * @return access token endpoint verb
     */
    public Verb getAccessTokenVerb() {
        return Verb.POST;
    }

    /**
     * Returns the URL that receives the access token requests.
     *
     * @return access token URL
     */
    public abstract String getAccessTokenEndpoint();

    public String getRefreshTokenEndpoint() {
        return getAccessTokenEndpoint();
    }

    /**
     * As stated in RFC 7009 OAuth 2.0 Token Revocation
     *
     * @return endpoint, which allows clients to notify the authorization server that a previously obtained refresh or
     * access token is no longer needed.
     * @see <a href="https://tools.ietf.org/html/rfc7009">RFC 7009</a>
     */
    public String getRevokeTokenEndpoint() {
        throw new UnsupportedOperationException(
                "This API doesn't support revoking tokens or we have no info about this");
    }

    /**
     * As stated in RFC 9126 OAuth 2.0 Pushed Authorization Requests
     *
     * @return endpoint, which allows clients to push the authorization request parameters to the authorization server.
     * @see <a href="https://tools.ietf.org/html/rfc9126">RFC 9126</a>
     */
    public String getPushedAuthorizationRequestEndpoint() {
        return null;
    }

    public abstract String getAuthorizationBaseUrl();

    /**
     * Returns the URL where you should redirect your users to authenticate your application.
     *
     * @param responseType responseType
     * @param apiKey apiKey
     * @param callback callback
     * @param scope scope
     * @param state state
     * @param additionalParams additionalParams
     * @return authorization URL
     */
    public String getAuthorizationUrl(String responseType, String apiKey, String callback, String scope, String state,
                                      Map<String, String> additionalParams) {
        final ParameterList parameters = new ParameterList(additionalParams);
        parameters.add(OAuthConstants.RESPONSE_TYPE, responseType);
        parameters.add(OAuthConstants.CLIENT_ID, apiKey);

        if (callback != null) {
            parameters.add(OAuthConstants.REDIRECT_URI, callback);
        }

        if (scope != null) {
            parameters.add(OAuthConstants.SCOPE, scope);
        }

        if (state != null) {
            parameters.add(OAuthConstants.STATE, state);
        }

        return parameters.appendTo(getAuthorizationBaseUrl());
    }

    /**
     * @param apiKey apiKey
     * @param apiSecret apiSecret
     * @param callback callback
     * @param defaultScope defaultScope
     * @param responseType responseType
     * @param debugStream debugStream
     * @param userAgent userAgent
     * @param httpClientConfig httpClientConfig
     * @param httpClient httpClient
     * @return OAuth20Service
     */
    public OAuth20Service createService(String apiKey, String apiSecret, String callback, String defaultScope,
                                        String responseType, OutputStream debugStream, String userAgent, HttpClientConfig httpClientConfig,
                                        HttpClient httpClient) {
        return new OAuth20Service(this, apiKey, apiSecret, callback, defaultScope, responseType, debugStream, userAgent,
                httpClientConfig, httpClient);
    }

    /**
     * @param apiKey apiKey
     * @param apiSecret apiSecret
     * @param callback callback
     * @param defaultScope defaultScope
     * @param responseType responseType
     * @param debugStream debugStream
     * @param userAgent userAgent
     * @param httpClientConfig httpClientConfig
     * @param httpClient httpClient
     * @param dpopProofCreator dpopProofCreator
     * @return OAuth20Service
     */
    public OAuth20Service createService(String apiKey, String apiSecret, String callback, String defaultScope,
                                        String responseType, OutputStream debugStream, String userAgent, HttpClientConfig httpClientConfig,
                                        HttpClient httpClient, DPoPProofCreator dpopProofCreator) {
        return new OAuth20Service(this, apiKey, apiSecret, callback, defaultScope, responseType, debugStream, userAgent,
                httpClientConfig, httpClient, dpopProofCreator);
    }

    public BearerSignature getBearerSignature() {
        return BearerSignatureAuthorizationRequestHeaderField.instance();
    }

    public ClientAuthentication getClientAuthentication() {
        return HttpBasicAuthenticationScheme.instance();
    }

    /**
     * RFC 8628 OAuth 2.0 Device Authorization Grant
     *
     * @return the device authorization endpoint
     * @see <a href="https://tools.ietf.org/html/rfc8628">RFC 8628</a>
     */
    public String getDeviceAuthorizationEndpoint() {
        throw new UnsupportedOperationException(
                "This API doesn't support Device Authorization Grant or we have no info about this");
    }

    public DeviceAuthorizationJsonExtractor getDeviceAuthorizationExtractor() {
        return DeviceAuthorizationJsonExtractor.instance();
    }
}
