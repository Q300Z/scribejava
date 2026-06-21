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
package com.github.scribejava.oidc;

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.httpclient.HttpClientConfig;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthAsyncRequestCallback;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.oauth2.grant.OAuth20Grant;
import com.github.scribejava.core.utils.JsonUtils;
import com.github.scribejava.oidc.model.OidcNonce;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/** Service OIDC natif. */
public class OidcService extends OAuth20Service {

  private IdTokenValidator idTokenValidator;
  private OidcSessionStateStore sessionStateStore = new DefaultOidcSessionStateStore();

  public OidcService(
      DefaultApi20 api,
      String apiKey,
      String apiSecret,
      String callback,
      String scope,
      String responseType,
      OutputStream debugStream,
      String userAgent,
      HttpClientConfig httpClientConfig,
      HttpClient httpClient) {
    super(
        api,
        apiKey,
        apiSecret,
        callback,
        scope,
        responseType,
        debugStream,
        userAgent,
        httpClientConfig,
        httpClient);
  }

  @Override
  protected CompletableFuture<OAuth2AccessToken> sendAccessTokenRequestAsync(
      OAuthRequest request, OAuthAsyncRequestCallback<OAuth2AccessToken> callback) {
    if ((getApiSecret() == null || getApiSecret().isEmpty())
        && !request
            .getHeaders()
            .containsKey(com.github.scribejava.core.model.OAuthConstants.HEADER)) {
      final boolean hasClientId =
          request.getBodyParams().getParams().stream().anyMatch(p -> "client_id".equals(p.getKey()))
              || request.getQueryStringParams().getParams().stream()
                  .anyMatch(p -> "client_id".equals(p.getKey()));
      if (!hasClientId) {
        request.addParameter(
            com.github.scribejava.core.model.OAuthConstants.CLIENT_ID, getApiKey());
      }
    }
    return super.sendAccessTokenRequestAsync(request, callback)
        .thenApply(
            token -> {
              validateIdToken(token);
              return token;
            });
  }

  private void validateIdToken(OAuth2AccessToken token) {
    String idTokenRaw = null;
    boolean isJson = false;
    try {
      final Map<String, Object> response = JsonUtils.parse(token.getRawResponse());
      isJson = true;
      idTokenRaw = (String) response.get("id_token");
    } catch (Exception e) {
      // ignore, not a JSON response
    }
    if (isJson && idTokenRaw == null) {
      return;
    }
    if (idTokenRaw == null) {
      idTokenRaw = token.getRawResponse();
    }

    if (idTokenValidator != null) {
      idTokenValidator.validate(idTokenRaw, null, 0);
    } else {
      if (idTokenRaw != null) {
        new IdToken(idTokenRaw);
      }
    }
  }

  /**
   * @param token token
   * @param nonce nonce (optionnel)
   * @return IdToken validé
   */
  public IdToken validateIdToken(OAuth2AccessToken token, OidcNonce nonce) {
    String idTokenRaw = null;
    try {
      final Map<String, Object> response = JsonUtils.parse(token.getRawResponse());
      idTokenRaw = (String) response.get("id_token");
    } catch (Exception e) {
      // ignore, not a JSON response
    }
    if (idTokenRaw == null) {
      idTokenRaw = token.getRawResponse();
    }

    if (idTokenValidator != null) {
      return idTokenValidator.validate(idTokenRaw, nonce, 0);
    }
    return idTokenRaw != null ? new IdToken(idTokenRaw) : null;
  }

  /**
   * @return idTokenValidator
   */
  public IdTokenValidator getIdTokenValidator() {
    return idTokenValidator;
  }

  /**
   * @param idTokenValidator idTokenValidator
   */
  public void setIdTokenValidator(IdTokenValidator idTokenValidator) {
    this.idTokenValidator = idTokenValidator;
  }

  /**
   * @param token access token
   * @return future
   */
  public CompletableFuture<StandardClaims> getUserInfoAsync(OAuth2AccessToken token) {
    return CompletableFuture.completedFuture(new StandardClaims(java.util.Collections.emptyMap()));
  }

  /**
   * Generates the RP-Initiated Logout URL for this service.
   *
   * @param idTokenHint the ID token hint (ID Token issued during authentication)
   * @param postLogoutRedirectUri the URI to redirect the user to after logout
   * @param state the optional state value to include in the redirect URI
   * @param clientId the client identifier
   * @return the logout URL, or {@code null} if the API or metadata is not configured/available
   */
  public String getLogoutUrl(
      String idTokenHint, String postLogoutRedirectUri, String state, String clientId) {
    final DefaultApi20 api = getApi();
    if (api instanceof DefaultOidcApi20) {
      final OidcProviderMetadata metadata = ((DefaultOidcApi20) api).getMetadata();
      if (metadata != null) {
        return OidcSessionHelper.getLogoutUrl(
            metadata.getEndSessionEndpoint(), idTokenHint, postLogoutRedirectUri, state, clientId);
      }
    }
    return null;
  }

  // Helper methods to automatically initialize state, generate auth URLs and request tokens

  /**
   * Initializes and saves a new OIDC session state (including state, nonce, and PKCE code
   * verifier).
   *
   * @return the newly created {@link OidcSessionState}
   */
  public OidcSessionState initSessionState() {
    final String state = java.util.UUID.randomUUID().toString();
    final OidcNonce nonce = OidcNonce.generate();
    final String codeVerifier =
        com.github.scribejava.core.pkce.PKCEService.defaultInstance()
            .generatePKCE()
            .getCodeVerifier();
    final OidcSessionState sessionState = new OidcSessionState(state, nonce, codeVerifier);
    if (sessionStateStore != null) {
      sessionStateStore.save(sessionState);
    }
    return sessionState;
  }

  /**
   * Generates the authorization URL using parameters from the provided OIDC session state.
   *
   * @param sessionState the OIDC session state to use for generating the authorization URL
   * @return the authorization URL string
   */
  public String getAuthorizationUrl(OidcSessionState sessionState) {
    final com.github.scribejava.core.pkce.PKCE pkce = new com.github.scribejava.core.pkce.PKCE();
    pkce.setCodeVerifier(sessionState.getCodeVerifier());
    try {
      pkce.setCodeChallenge(
          pkce.getCodeChallengeMethod().transform2CodeChallenge(sessionState.getCodeVerifier()));
    } catch (Exception e) {
      pkce.setCodeChallengeMethod(com.github.scribejava.core.pkce.PKCECodeChallengeMethod.PLAIN);
      pkce.setCodeChallenge(sessionState.getCodeVerifier());
    }
    return createAuthorizationUrlBuilder()
        .state(sessionState.getState())
        .nonce(sessionState.getNonce().getValue())
        .pkce(pkce)
        .build();
  }

  /**
   * {@inheritDoc}
   *
   * @param grant the OAuth 2.0 grant
   * @return the retrieved {@link OAuth2AccessToken}
   * @throws IOException if network or serialization error occurs
   * @throws InterruptedException if current thread is interrupted
   * @throws java.util.concurrent.ExecutionException if execution exception occurs
   */
  @Override
  public OAuth2AccessToken getAccessToken(OAuth20Grant grant)
      throws IOException, InterruptedException, java.util.concurrent.ExecutionException {
    return super.getAccessToken(grant);
  }

  /**
   * Retrieves the access token using authorization code and loads session state for verification.
   *
   * @param code the authorization code
   * @param stateVal the state parameter value
   * @return the retrieved {@link OAuth2AccessToken}
   * @throws IOException if network or serialization error occurs
   * @throws InterruptedException if current thread is interrupted
   * @throws java.util.concurrent.ExecutionException if execution exception occurs
   */
  public OAuth2AccessToken getAccessToken(String code, String stateVal)
      throws IOException, InterruptedException, java.util.concurrent.ExecutionException {
    final OidcSessionState sessionState =
        sessionStateStore != null ? sessionStateStore.load(stateVal) : null;
    return getAccessToken(code, sessionState);
  }

  /**
   * Retrieves the access token using authorization code and performs ID Token validation using
   * session state.
   *
   * @param code the authorization code
   * @param sessionState the {@link OidcSessionState} containing nonce and code verifier, or {@code
   *     null}
   * @return the retrieved {@link OAuth2AccessToken}
   * @throws IOException if network or serialization error occurs
   * @throws InterruptedException if current thread is interrupted
   * @throws java.util.concurrent.ExecutionException if execution exception occurs
   */
  public OAuth2AccessToken getAccessToken(String code, OidcSessionState sessionState)
      throws IOException, InterruptedException, java.util.concurrent.ExecutionException {
    final com.github.scribejava.core.oauth2.grant.AuthorizationCodeGrant grant =
        new com.github.scribejava.core.oauth2.grant.AuthorizationCodeGrant(code);
    if (sessionState != null && sessionState.getCodeVerifier() != null) {
      grant.setPkceCodeVerifier(sessionState.getCodeVerifier());
    }
    final OAuth2AccessToken token = getAccessToken(grant);
    if (sessionState != null) {
      validateIdToken(token, sessionState.getNonce());
      if (sessionStateStore != null && sessionState.getState() != null) {
        sessionStateStore.remove(sessionState.getState());
      }
    } else {
      validateIdToken(token);
    }
    return token;
  }

  /**
   * Asynchronously retrieves the access token using authorization code and loads session state for
   * verification.
   *
   * @param code the authorization code
   * @param stateVal the state parameter value
   * @return a {@link CompletableFuture} for the {@link OAuth2AccessToken}
   */
  public CompletableFuture<OAuth2AccessToken> getAccessTokenAsync(String code, String stateVal) {
    final OidcSessionState sessionState =
        sessionStateStore != null ? sessionStateStore.load(stateVal) : null;
    return getAccessTokenAsync(code, sessionState);
  }

  /**
   * Asynchronously retrieves the access token using authorization code and performs ID Token
   * validation.
   *
   * @param code the authorization code
   * @param sessionState the {@link OidcSessionState} containing nonce and code verifier, or {@code
   *     null}
   * @return a {@link CompletableFuture} for the {@link OAuth2AccessToken}
   */
  public CompletableFuture<OAuth2AccessToken> getAccessTokenAsync(
      String code, OidcSessionState sessionState) {
    final com.github.scribejava.core.oauth2.grant.AuthorizationCodeGrant grant =
        new com.github.scribejava.core.oauth2.grant.AuthorizationCodeGrant(code);
    if (sessionState != null && sessionState.getCodeVerifier() != null) {
      grant.setPkceCodeVerifier(sessionState.getCodeVerifier());
    }
    return getAccessTokenAsync(grant)
        .thenApply(
            token -> {
              if (sessionState != null) {
                validateIdToken(token, sessionState.getNonce());
                if (sessionStateStore != null && sessionState.getState() != null) {
                  sessionStateStore.remove(sessionState.getState());
                }
              } else {
                validateIdToken(token);
              }
              return token;
            });
  }

  /**
   * Gets the session state store.
   *
   * @return the {@link OidcSessionStateStore}
   */
  public OidcSessionStateStore getSessionStateStore() {
    return sessionStateStore;
  }

  /**
   * Sets the session state store.
   *
   * @param sessionStateStore the {@link OidcSessionStateStore} to use
   */
  public void setSessionStateStore(OidcSessionStateStore sessionStateStore) {
    this.sessionStateStore = sessionStateStore;
  }
}
