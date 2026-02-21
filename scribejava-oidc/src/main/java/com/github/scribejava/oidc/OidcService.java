package com.github.scribejava.oidc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.httpclient.HttpClientConfig;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthConstants;
import com.github.scribejava.core.model.ParameterList;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.nimbusds.openid.connect.sdk.Nonce;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Specialized OAuth20Service for OpenID Connect.
 */
public class OidcService extends OAuth20Service {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final IdTokenValidator idTokenValidator;

    public OidcService(final DefaultApi20 api, final String apiKey, final String apiSecret, final String callback,
                       final String defaultScope, final String responseType, final OutputStream debugStream,
                       final String userAgent, final HttpClientConfig httpClientConfig, final HttpClient httpClient,
                       final IdTokenValidator idTokenValidator) {
        super(api, apiKey, apiSecret, callback, defaultScope, responseType, debugStream, userAgent, httpClientConfig,
                httpClient);
        this.idTokenValidator = idTokenValidator;
    }

    public IdToken validateIdToken(final OAuth2AccessToken accessToken, final Nonce expectedNonce) {
        try {
            final JsonNode node = OBJECT_MAPPER.readTree(accessToken.getRawResponse());
            final JsonNode idTokenNode = node.get("id_token");
            if (idTokenNode == null || idTokenNode.isNull()) {
                throw new IllegalArgumentException("Response does not contain an id_token");
            }
            return idTokenValidator.validate(idTokenNode.asText(), expectedNonce, 0);
        } catch (final IOException e) {
            throw new IllegalArgumentException("Failed to parse access token response as JSON", e);
        }
    }

    public String getRpInitiatedLogoutUrl(final String endSessionEndpoint, final String idTokenHint,
                                          final String postLogoutRedirectUri, final String state) {
        final ParameterList parameters = new ParameterList();
        if (idTokenHint != null) {
            parameters.add("id_token_hint", idTokenHint);
        }
        if (postLogoutRedirectUri != null) {
            parameters.add("post_logout_redirect_uri", postLogoutRedirectUri);
        }
        if (state != null) {
            parameters.add(OAuthConstants.STATE, state);
        }
        return parameters.appendTo(endSessionEndpoint);
    }

    public CompletableFuture<Void> revokeTokenAsync(final String revocationEndpoint, final String token) {
        final com.github.scribejava.core.model.OAuthRequest request = new com.github.scribejava.core.model.OAuthRequest(
                com.github.scribejava.core.model.Verb.POST, revocationEndpoint);
        request.addBodyParameter("token", token);
        getApi().getClientAuthentication().addClientAuthentication(request, getApiKey(), getApiSecret());

        return execute(request, null, response -> {
            try (com.github.scribejava.core.model.Response resp = response) {
                if (resp.getCode() != 200) {
                    throw new com.github.scribejava.core.exceptions.OAuthException("Token revocation failed. Status: "
                            + resp.getCode() + ", Body: " + resp.getBody());
                }
                return null;
            }
        });
    }

    public String getAuthorizationUrlFormPost(final String state, final Map<String, String> additionalParams) {
        final Map<String, String> params = additionalParams == null ? new HashMap<>() : new HashMap<>(additionalParams);
        params.put("response_mode", "form_post");
        return createAuthorizationUrlBuilder()
                .state(state)
                .additionalParams(params)
                .build();
    }

    public void validateIssuerResponse(final String issuerResponse, final String expectedIssuer) {
        if (issuerResponse != null && !issuerResponse.equals(expectedIssuer)) {
            throw new com.github.scribejava.core.exceptions.OAuthException(
                    "Issuer mismatch in authorization response. Expected: " + expectedIssuer + ", Got: "
                            + issuerResponse);
        }
    }

    public CompletableFuture<StandardClaims> getUserInfoAsync(final OAuth2AccessToken accessToken) {
        String userInfoEndpoint = null;
        if (getApi() instanceof DefaultOidcApi20) {
            userInfoEndpoint = ((DefaultOidcApi20) getApi()).getUserinfoEndpoint();
        }

        if (userInfoEndpoint == null || userInfoEndpoint.isEmpty()) {
            throw new com.github.scribejava.core.exceptions.OAuthException("UserInfo endpoint is not defined.");
        }

        final com.github.scribejava.core.model.OAuthRequest request = new com.github.scribejava.core.model.OAuthRequest(
                com.github.scribejava.core.model.Verb.GET, userInfoEndpoint);
        signRequest(accessToken, request);

        return execute(request, null, response -> {
            try (com.github.scribejava.core.model.Response resp = response) {
                if (resp.getCode() != 200) {
                    throw new com.github.scribejava.core.exceptions.OAuthException("UserInfo request failed. Status: "
                            + resp.getCode() + ", Body: " + resp.getBody());
                }
                return UserInfoJsonExtractor.instance().extract(resp);
            }
        });
    }
}
