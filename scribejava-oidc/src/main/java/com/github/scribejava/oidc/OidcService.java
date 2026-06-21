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
   * Génère l'URL de déconnexion RP-Initiated Logout pour ce service.
   *
   * @param idTokenHint Le jeton d'identité (ID Token) émis lors de l'authentification.
   * @param postLogoutRedirectUri L'URL vers laquelle rediriger après déconnexion.
   * @param state L'état facultatif à joindre à la redirection.
   * @param clientId L'identifiant du client.
   * @return L'URL de déconnexion, ou null si l'API ou le point de terminaison n'est pas configuré.
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

  @Override
  public OAuth2AccessToken getAccessToken(OAuth20Grant grant)
      throws IOException, InterruptedException, java.util.concurrent.ExecutionException {
    return super.getAccessToken(grant);
  }

  public OAuth2AccessToken getAccessToken(String code, String stateVal)
      throws IOException, InterruptedException, java.util.concurrent.ExecutionException {
    final OidcSessionState sessionState =
        sessionStateStore != null ? sessionStateStore.load(stateVal) : null;
    return getAccessToken(code, sessionState);
  }

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

  public CompletableFuture<OAuth2AccessToken> getAccessTokenAsync(String code, String stateVal) {
    final OidcSessionState sessionState =
        sessionStateStore != null ? sessionStateStore.load(stateVal) : null;
    return getAccessTokenAsync(code, sessionState);
  }

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

  public OidcSessionStateStore getSessionStateStore() {
    return sessionStateStore;
  }

  public void setSessionStateStore(OidcSessionStateStore sessionStateStore) {
    this.sessionStateStore = sessionStateStore;
  }
}
