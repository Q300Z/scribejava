package com.github.scribejava.oidc;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.httpclient.HttpClientConfig;
import com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet;
import com.nimbusds.openid.connect.sdk.Nonce;

import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;

/**
 * Specialized OAuth20Service for OpenID Connect.
 * Automatically handles ID Token validation.
 */
public class OidcService extends OAuth20Service {

    private final IdTokenValidator idTokenValidator;

    public OidcService(DefaultApi20 api, String apiKey, String apiSecret, String callback, String defaultScope,
                       String responseType, OutputStream debugStream, String userAgent, HttpClientConfig httpClientConfig,
                       HttpClient httpClient, IdTokenValidator idTokenValidator) {
        super(api, apiKey, apiSecret, callback, defaultScope, responseType, debugStream, userAgent, httpClientConfig, httpClient);
        this.idTokenValidator = idTokenValidator;
    }

    /**
     * Validates the ID Token from an OAuth2AccessToken response.
     *
     * @param accessToken The access token containing an id_token claim.
     * @param expectedNonce The nonce used in the authorization request.
     * @return The claims from the validated ID Token.
     */
    public IDTokenClaimsSet validateIdToken(OAuth2AccessToken accessToken, Nonce expectedNonce) {
        String idToken = (String) accessToken.getRawResponseAsJson().get("id_token");
        if (idToken == null) {
            throw new IllegalArgumentException("Response does not contain an id_token");
        }
        return idTokenValidator.validate(idToken, expectedNonce, 0);
    }

    // You can override getAccessToken methods to automatically validate if a nonce is provided.
}
