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
import com.github.scribejava.core.pkce.PKCE;
import com.github.scribejava.core.revoke.TokenTypeHint;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Service principal pour le protocole OAuth 2.0.
 *
 * <p>Cette classe gère le flux d'autorisation, l'obtention et le renouvellement des jetons, ainsi
 * que la signature des requêtes vers les ressources protégées. Elle supporte nativement plusieurs
 * extensions de sécurité :
 *
 * <ul>
 *   <li><b>PKCE (RFC 7636):</b> Protection contre l'interception de code d'autorisation.
 *   <li><b>DPoP (RFC 9449):</b> Liaison cryptographique des jetons au client.
 *   <li><b>PAR (RFC 9126):</b> Envoi sécurisé des paramètres d'autorisation côté serveur.
 *   <li><b>Revocation (RFC 7009):</b> Invalidation des jetons d'accès et de renouvellement.
 * </ul>
 *
 * @see <a href="https://tools.ietf.org/html/rfc6749">RFC 6749 (The OAuth 2.0 Authorization
 *     Framework)</a>
 */
public class OAuth20Service extends OAuthService {

  private static final String VERSION = "2.0";
  private final DefaultApi20 api;
  private final String responseType;
  private final String defaultScope;
  private final OAuth20RequestSigner requestSigner;
  private final OAuth20RevocationHandler revocationHandler;
  private final OAuth20DeviceFlowHandler deviceFlowHandler;
  private final OAuth20PushedAuthHandler pushedAuthHandler;
  private AuthorizationRequestConverter authorizationRequestConverter = params -> params;

  /**
   * Constructeur simple.
   *
   * @param api L'instance de l'API OAuth 2.0.
   * @param apiKey La clé API du client (Client ID).
   * @param apiSecret Le secret API du client (Client Secret).
   * @param callback L'URL de rappel (Redirect URI).
   * @param defaultScope La portée par défaut.
   * @param responseType Le type de réponse attendu (ex: "code").
   * @param debugStream Flux pour les logs de débogage.
   * @param userAgent Chaîne User-Agent pour les requêtes HTTP.
   * @param httpClientConfig Configuration du client HTTP.
   * @param httpClient L'implémentation du client HTTP.
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
   * Constructeur supportant DPoP.
   *
   * @param api L'instance de l'API OAuth 2.0.
   * @param apiKey La clé API du client.
   * @param apiSecret Le secret API du client.
   * @param callback L'URL de rappel.
   * @param defaultScope La portée par défaut.
   * @param responseType Le type de réponse.
   * @param debugStream Flux pour les logs de débogage.
   * @param userAgent Chaîne User-Agent.
   * @param httpClientConfig Configuration du client HTTP.
   * @param httpClient L'implémentation du client HTTP.
   * @param dpopProofCreator Créateur de preuves DPoP (RFC 9449).
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
    this.responseType = responseType;
    this.api = api;
    this.defaultScope = defaultScope;
    this.requestSigner = new OAuth20RequestSigner(api, dpopProofCreator);
    this.revocationHandler = new OAuth20RevocationHandler(this);
    this.deviceFlowHandler = new OAuth20DeviceFlowHandler(this);
    this.pushedAuthHandler = new OAuth20PushedAuthHandler(this);
  }

  /**
   * Définit le convertisseur de requête d'autorisation.
   *
   * @param authorizationRequestConverter Le convertisseur à utiliser.
   */
  public void setAuthorizationRequestConverter(
      AuthorizationRequestConverter authorizationRequestConverter) {
    this.authorizationRequestConverter = authorizationRequestConverter;
  }

  /**
   * Retourne le convertisseur de requête d'autorisation actuel.
   *
   * @return Le convertisseur utilisé par ce service.
   */
  public AuthorizationRequestConverter getAuthorizationRequestConverter() {
    return authorizationRequestConverter;
  }

  // ===== common OAuth methods =====

  /** {@inheritDoc} */
  @Override
  public String getVersion() {
    return VERSION;
  }

  /**
   * Signe une requête en y ajoutant le jeton d'accès.
   *
   * <p>Cette méthode gère automatiquement l'ajout du jeton selon le schéma configuré (ex: Bearer)
   * et génère une preuve DPoP si un créateur de preuves est configuré.
   *
   * @param accessToken Le jeton d'accès sous forme de chaîne.
   * @param request La requête à signer.
   * @see <a href="https://tools.ietf.org/html/rfc6750">RFC 6750 (Bearer Token)</a>
   * @see <a href="https://tools.ietf.org/html/rfc9449">RFC 9449 (DPoP)</a>
   */
  public void signRequest(String accessToken, OAuthRequest request) {
    requestSigner.signRequest(accessToken, request);
  }

  /**
   * Signe une requête en y ajoutant le jeton d'accès complexe.
   *
   * @param accessToken L'objet jeton d'accès OAuth 2.0.
   * @param request La requête à signer.
   */
  public void signRequest(OAuth2AccessToken accessToken, OAuthRequest request) {
    requestSigner.signRequest(accessToken, request);
  }

  /**
   * Retourne l'URL vers laquelle vous devez rediriger vos utilisateurs pour authentifier votre
   * application.
   *
   * @return L'URL d'autorisation complète.
   * @see <a href="https://tools.ietf.org/html/rfc6749#section-4.1.1">RFC 6749, Section 4.1.1
   *     (Authorization Request)</a>
   */
  public String getAuthorizationUrl() {
    return createAuthorizationUrlBuilder().build();
  }

  /**
   * Retourne l'URL d'autorisation avec un paramètre d'état (state).
   *
   * @param state Valeur opaque utilisée pour maintenir l'état entre la requête et le rappel (CSRF).
   * @return L'URL d'autorisation.
   */
  public String getAuthorizationUrl(String state) {
    return createAuthorizationUrlBuilder().state(state).build();
  }

  /**
   * Retourne l'URL d'autorisation avec des paramètres supplémentaires.
   *
   * @param additionalParams Paramètres GET additionnels à ajouter à l'URL.
   * @return L'URL d'autorisation.
   */
  public String getAuthorizationUrl(Map<String, String> additionalParams) {
    return createAuthorizationUrlBuilder().additionalParams(additionalParams).build();
  }

  /**
   * Retourne l'URL d'autorisation configurée pour PKCE.
   *
   * @param pkce L'objet contenant le code_challenge et sa méthode.
   * @return L'URL d'autorisation.
   * @see <a href="https://tools.ietf.org/html/rfc7636">RFC 7636 (PKCE)</a>
   */
  public String getAuthorizationUrl(PKCE pkce) {
    return createAuthorizationUrlBuilder().pkce(pkce).build();
  }

  /**
   * Crée un constructeur d'URL d'autorisation fluide.
   *
   * @return Une nouvelle instance de {@link AuthorizationUrlBuilder}.
   */
  public AuthorizationUrlBuilder createAuthorizationUrlBuilder() {
    return new AuthorizationUrlBuilder(this);
  }

  /**
   * Retourne l'instance de l'API associée à ce service.
   *
   * @return L'instance {@link DefaultApi20}.
   */
  public DefaultApi20 getApi() {
    return api;
  }

  /**
   * Extrait les informations d'autorisation à partir de l'URL de redirection.
   *
   * @param redirectLocation L'URL complète de redirection reçue du serveur d'autorisation.
   * @return Un objet {@link OAuth2Authorization} contenant le code et l'état.
   */
  public OAuth2Authorization extractAuthorization(String redirectLocation) {
    final OAuth2Authorization authorization = new OAuth2Authorization();
    int end = redirectLocation.indexOf('#');
    if (end == -1) {
      end = redirectLocation.length();
    }
    for (String param :
        redirectLocation.substring(redirectLocation.indexOf('?') + 1, end).split("&")) {
      final String[] keyValue = param.split("=");
      if (keyValue.length == 2) {
        try {
          switch (keyValue[0]) {
            case "code":
              authorization.setCode(URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8.name()));
              break;
            case "state":
              authorization.setState(URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8.name()));
              break;
            default: // just ignore any other param;
          }
        } catch (IOException | RuntimeException ueE) {
          throw new IllegalStateException("Unexpected encoding exception with UTF-8", ueE);
        }
      }
    }
    return authorization;
  }

  /**
   * Retourne le type de réponse configuré pour ce service.
   *
   * @return Le type de réponse (ex: "code").
   */
  public String getResponseType() {
    return responseType;
  }

  /**
   * Retourne la portée (scope) par défaut configurée pour ce service.
   *
   * @return La portée par défaut.
   */
  public String getDefaultScope() {
    return defaultScope;
  }

  protected void logRequestWithParams(String requestDescription, OAuthRequest request) {
    if (isDebug()) {
      log(
          "created "
              + requestDescription
              + " request with body params [%s], query string params [%s]",
          request.getBodyParams().asFormUrlEncodedString(),
          request.getQueryStringParams().asFormUrlEncodedString());
    }
  }

  // ===== common AccessToken request methods =====
  // protected to facilitate mocking
  protected OAuth2AccessToken sendAccessTokenRequestSync(OAuthRequest request)
      throws IOException, InterruptedException, ExecutionException {
    if (isDebug()) {
      log("send request for access token synchronously to %s", request.getCompleteUrl());
    }
    try (Response response = execute(request)) {
      if (isDebug()) {
        log("response status code: %s", response.getCode());
        log("response body: %s", response.getBody());
      }

      return api.getAccessTokenExtractor().extract(response);
    }
  }

  // protected to facilitate mocking
  protected CompletableFuture<OAuth2AccessToken> sendAccessTokenRequestAsync(OAuthRequest request) {
    return sendAccessTokenRequestAsync(request, null);
  }

  // protected to facilitate mocking
  protected CompletableFuture<OAuth2AccessToken> sendAccessTokenRequestAsync(
      OAuthRequest request, OAuthAsyncRequestCallback<OAuth2AccessToken> callback) {
    if (isDebug()) {
      log("send request for access token asynchronously to %s", request.getCompleteUrl());
    }

    return execute(
        request,
        callback,
        new OAuthRequest.ResponseConverter<OAuth2AccessToken>() {
          @Override
          public OAuth2AccessToken convert(Response response) throws IOException {
            log("received response for access token");
            if (isDebug()) {
              log("response status code: %s", response.getCode());
              log("response body: %s", response.getBody());
            }
            final OAuth2AccessToken token = api.getAccessTokenExtractor().extract(response);
            response.close();
            return token;
          }
        });
  }

  // ===== get AccessToken authorisation code flow methods =====
  protected OAuthRequest createAccessTokenRequest(AccessTokenRequestParams params) {
    final OAuthRequest request =
        new OAuthRequest(api.getAccessTokenVerb(), api.getAccessTokenEndpoint());

    api.getClientAuthentication().addClientAuthentication(request, getApiKey(), getApiSecret());

    request.addParameter(OAuthConstants.CODE, params.getCode());
    final String callback = getCallback();
    if (callback != null) {
      request.addParameter(OAuthConstants.REDIRECT_URI, callback);
    }
    final String scope = params.getScope();
    if (scope != null) {
      request.addParameter(OAuthConstants.SCOPE, scope);
    } else if (defaultScope != null) {
      request.addParameter(OAuthConstants.SCOPE, defaultScope);
    }
    request.addParameter(OAuthConstants.GRANT_TYPE, OAuthConstants.AUTHORIZATION_CODE);

    final String pkceCodeVerifier = params.getPkceCodeVerifier();
    if (pkceCodeVerifier != null) {
      request.addParameter(PKCE.PKCE_CODE_VERIFIER_PARAM, pkceCodeVerifier);
    }

    final Map<String, String> extraParameters = params.getExtraParameters();
    if (extraParameters != null && !extraParameters.isEmpty()) {
      for (Map.Entry<String, String> extraParameter : extraParameters.entrySet()) {
        request.addParameter(extraParameter.getKey(), extraParameter.getValue());
      }
    }

    logRequestWithParams("access token", request);
    return request;
  }

  /**
   * @param code code
   * @return OAuth2AccessToken
   * @throws IOException IOException
   * @throws InterruptedException InterruptedException
   * @throws ExecutionException ExecutionException
   * @deprecated use {@link #getAccessToken(com.github.scribejava.core.oauth2.grant.OAuth20Grant)}
   */
  @Deprecated
  public OAuth2AccessToken getAccessToken(String code)
      throws IOException, InterruptedException, ExecutionException {
    return getAccessToken(AccessTokenRequestParams.create(code));
  }

  /**
   * @param params params
   * @return OAuth2AccessToken
   * @throws IOException IOException
   * @throws InterruptedException InterruptedException
   * @throws ExecutionException ExecutionException
   * @deprecated use {@link #getAccessToken(com.github.scribejava.core.oauth2.grant.OAuth20Grant)}
   */
  @Deprecated
  public OAuth2AccessToken getAccessToken(AccessTokenRequestParams params)
      throws IOException, InterruptedException, ExecutionException {
    return sendAccessTokenRequestSync(createAccessTokenRequest(params));
  }

  /**
   * Méthode unifiée pour obtenir un jeton d'accès en utilisant n'importe quelle stratégie de
   * concession (grant).
   *
   * @param grant La stratégie de concession à utiliser (ex: AuthorizationCodeGrant,
   *     ClientCredentialsGrant).
   * @return L'objet {@link OAuth2AccessToken} contenant le jeton d'accès.
   * @throws IOException en cas d'erreur réseau.
   * @throws InterruptedException si le thread est interrompu.
   * @throws ExecutionException si l'exécution de la requête échoue.
   * @see <a href="https://tools.ietf.org/html/rfc6749#section-4">RFC 6749, Section 4 (Obtaining
   *     Authorization)</a>
   */
  public OAuth2AccessToken getAccessToken(
      com.github.scribejava.core.oauth2.grant.OAuth20Grant grant)
      throws IOException, InterruptedException, ExecutionException {
    return sendAccessTokenRequestSync(grant.createRequest(this));
  }

  /**
   * Start the request to retrieve the access token. The optionally provided callback will be called
   * with the Token when it is available.
   *
   * @param params params
   * @param callback optional callback
   * @return CompletableFuture
   * @deprecated use {@link
   *     #getAccessTokenAsync(com.github.scribejava.core.oauth2.grant.OAuth20Grant)}
   */
  @Deprecated
  public CompletableFuture<OAuth2AccessToken> getAccessToken(
      AccessTokenRequestParams params, OAuthAsyncRequestCallback<OAuth2AccessToken> callback) {
    return sendAccessTokenRequestAsync(createAccessTokenRequest(params), callback);
  }

  /**
   * @param code code
   * @param callback callback
   * @return CompletableFuture
   * @deprecated use {@link
   *     #getAccessTokenAsync(com.github.scribejava.core.oauth2.grant.OAuth20Grant)}
   */
  @Deprecated
  public CompletableFuture<OAuth2AccessToken> getAccessToken(
      String code, OAuthAsyncRequestCallback<OAuth2AccessToken> callback) {
    return getAccessToken(AccessTokenRequestParams.create(code), callback);
  }

  /**
   * @param code code
   * @return CompletableFuture
   * @deprecated use {@link
   *     #getAccessTokenAsync(com.github.scribejava.core.oauth2.grant.OAuth20Grant)}
   */
  @Deprecated
  public CompletableFuture<OAuth2AccessToken> getAccessTokenAsync(String code) {
    return getAccessToken(AccessTokenRequestParams.create(code), null);
  }

  /**
   * @param params params
   * @return CompletableFuture
   * @deprecated use {@link
   *     #getAccessTokenAsync(com.github.scribejava.core.oauth2.grant.OAuth20Grant)}
   */
  @Deprecated
  public CompletableFuture<OAuth2AccessToken> getAccessTokenAsync(AccessTokenRequestParams params) {
    return getAccessToken(params, null);
  }

  /**
   * Version asynchrone de l'obtention de jeton d'accès utilisant une stratégie de concession.
   *
   * @param grant La stratégie de concession à utiliser.
   * @return Un {@link CompletableFuture} résolvant vers {@link OAuth2AccessToken}.
   */
  public CompletableFuture<OAuth2AccessToken> getAccessTokenAsync(
      com.github.scribejava.core.oauth2.grant.OAuth20Grant grant) {
    return sendAccessTokenRequestAsync(grant.createRequest(this), null);
  }

  // ===== refresh AccessToken methods =====
  protected OAuthRequest createRefreshTokenRequest(String refreshToken, String scope) {
    if (refreshToken == null || refreshToken.isEmpty()) {
      throw new IllegalArgumentException("The refreshToken cannot be null or empty");
    }
    final OAuthRequest request =
        new OAuthRequest(api.getAccessTokenVerb(), api.getRefreshTokenEndpoint());

    api.getClientAuthentication().addClientAuthentication(request, getApiKey(), getApiSecret());

    if (scope != null) {
      request.addParameter(OAuthConstants.SCOPE, scope);
    } else if (defaultScope != null) {
      request.addParameter(OAuthConstants.SCOPE, defaultScope);
    }

    request.addParameter(OAuthConstants.REFRESH_TOKEN, refreshToken);
    request.addParameter(OAuthConstants.GRANT_TYPE, OAuthConstants.REFRESH_TOKEN);

    logRequestWithParams("refresh token", request);

    return request;
  }

  /**
   * @param refreshToken refreshToken
   * @return CompletableFuture
   * @deprecated use {@link
   *     #getAccessTokenAsync(com.github.scribejava.core.oauth2.grant.OAuth20Grant)}
   */
  @Deprecated
  public CompletableFuture<OAuth2AccessToken> refreshAccessTokenAsync(String refreshToken) {
    return refreshAccessToken(refreshToken, (OAuthAsyncRequestCallback<OAuth2AccessToken>) null);
  }

  /**
   * @param refreshToken refreshToken
   * @param scope scope
   * @return CompletableFuture
   * @deprecated use {@link
   *     #getAccessTokenAsync(com.github.scribejava.core.oauth2.grant.OAuth20Grant)}
   */
  @Deprecated
  public CompletableFuture<OAuth2AccessToken> refreshAccessTokenAsync(
      String refreshToken, String scope) {
    return refreshAccessToken(refreshToken, scope, null);
  }

  /**
   * @param refreshToken refreshToken
   * @return OAuth2AccessToken
   * @throws IOException IOException
   * @throws InterruptedException InterruptedException
   * @throws ExecutionException ExecutionException
   * @deprecated use {@link #getAccessToken(com.github.scribejava.core.oauth2.grant.OAuth20Grant)}
   */
  @Deprecated
  public OAuth2AccessToken refreshAccessToken(String refreshToken)
      throws IOException, InterruptedException, ExecutionException {
    return refreshAccessToken(refreshToken, (String) null);
  }

  /**
   * @param refreshToken refreshToken
   * @param scope scope
   * @return OAuth2AccessToken
   * @throws IOException IOException
   * @throws InterruptedException InterruptedException
   * @throws ExecutionException ExecutionException
   * @deprecated use {@link #getAccessToken(com.github.scribejava.core.oauth2.grant.OAuth20Grant)}
   */
  @Deprecated
  public OAuth2AccessToken refreshAccessToken(String refreshToken, String scope)
      throws IOException, InterruptedException, ExecutionException {
    final OAuthRequest request = createRefreshTokenRequest(refreshToken, scope);

    return sendAccessTokenRequestSync(request);
  }

  /**
   * @param refreshToken refreshToken
   * @param callback callback
   * @return CompletableFuture
   * @deprecated use {@link
   *     #getAccessTokenAsync(com.github.scribejava.core.oauth2.grant.OAuth20Grant)}
   */
  @Deprecated
  public CompletableFuture<OAuth2AccessToken> refreshAccessToken(
      String refreshToken, OAuthAsyncRequestCallback<OAuth2AccessToken> callback) {
    final OAuthRequest request = createRefreshTokenRequest(refreshToken, null);

    return sendAccessTokenRequestAsync(request, callback);
  }

  /**
   * @param refreshToken refreshToken
   * @param scope scope
   * @param callback callback
   * @return CompletableFuture
   * @deprecated use {@link
   *     #getAccessTokenAsync(com.github.scribejava.core.oauth2.grant.OAuth20Grant)}
   */
  @Deprecated
  public CompletableFuture<OAuth2AccessToken> refreshAccessToken(
      String refreshToken, String scope, OAuthAsyncRequestCallback<OAuth2AccessToken> callback) {
    final OAuthRequest request = createRefreshTokenRequest(refreshToken, scope);

    return sendAccessTokenRequestAsync(request, callback);
  }

  // ===== get AccessToken password grant flow methods =====
  protected OAuthRequest createAccessTokenPasswordGrantRequest(
      String username, String password, String scope) {
    final OAuthRequest request =
        new OAuthRequest(api.getAccessTokenVerb(), api.getAccessTokenEndpoint());
    request.addParameter(OAuthConstants.USERNAME, username);
    request.addParameter(OAuthConstants.PASSWORD, password);

    if (scope != null) {
      request.addParameter(OAuthConstants.SCOPE, scope);
    } else if (defaultScope != null) {
      request.addParameter(OAuthConstants.SCOPE, defaultScope);
    }

    request.addParameter(OAuthConstants.GRANT_TYPE, OAuthConstants.PASSWORD);

    api.getClientAuthentication().addClientAuthentication(request, getApiKey(), getApiSecret());

    logRequestWithParams("access token password grant", request);

    return request;
  }

  /**
   * @param username username
   * @param password password
   * @return OAuth2AccessToken
   * @throws IOException IOException
   * @throws InterruptedException InterruptedException
   * @throws ExecutionException ExecutionException
   * @deprecated use {@link #getAccessToken(com.github.scribejava.core.oauth2.grant.OAuth20Grant)}
   *     with {@link com.github.scribejava.core.oauth2.grant.PasswordGrant}
   */
  @Deprecated
  public OAuth2AccessToken getAccessTokenPasswordGrant(String username, String password)
      throws IOException, InterruptedException, ExecutionException {
    return getAccessTokenPasswordGrant(username, password, null);
  }

  /**
   * @param username username
   * @param password password
   * @param scope scope
   * @return OAuth2AccessToken
   * @throws IOException IOException
   * @throws InterruptedException InterruptedException
   * @throws ExecutionException ExecutionException
   * @deprecated use {@link #getAccessToken(com.github.scribejava.core.oauth2.grant.OAuth20Grant)}
   *     with {@link com.github.scribejava.core.oauth2.grant.PasswordGrant}
   */
  @Deprecated
  public OAuth2AccessToken getAccessTokenPasswordGrant(
      String username, String password, String scope)
      throws IOException, InterruptedException, ExecutionException {
    return getAccessToken(
        new com.github.scribejava.core.oauth2.grant.PasswordGrant(username, password, scope));
  }

  /**
   * @param username username
   * @param password password
   * @return CompletableFuture
   * @deprecated use {@link
   *     #getAccessTokenAsync(com.github.scribejava.core.oauth2.grant.OAuth20Grant)} with {@link
   *     com.github.scribejava.core.oauth2.grant.PasswordGrant}
   */
  @Deprecated
  public CompletableFuture<OAuth2AccessToken> getAccessTokenPasswordGrantAsync(
      String username, String password) {
    return getAccessTokenPasswordGrantAsync(
        username, password, (OAuthAsyncRequestCallback<OAuth2AccessToken>) null);
  }

  /**
   * @param username username
   * @param password password
   * @param scope scope
   * @return CompletableFuture
   * @deprecated use {@link
   *     #getAccessTokenAsync(com.github.scribejava.core.oauth2.grant.OAuth20Grant)} with {@link
   *     com.github.scribejava.core.oauth2.grant.PasswordGrant}
   */
  @Deprecated
  public CompletableFuture<OAuth2AccessToken> getAccessTokenPasswordGrantAsync(
      String username, String password, String scope) {
    return getAccessTokenPasswordGrantAsync(username, password, scope, null);
  }

  /**
   * Request Access Token Password Grant async version
   *
   * @param username User name
   * @param password User password
   * @param callback Optional callback
   * @return CompletableFuture
   * @deprecated use {@link
   *     #getAccessTokenAsync(com.github.scribejava.core.oauth2.grant.OAuth20Grant)} with {@link
   *     com.github.scribejava.core.oauth2.grant.PasswordGrant}
   */
  @Deprecated
  public CompletableFuture<OAuth2AccessToken> getAccessTokenPasswordGrantAsync(
      String username, String password, OAuthAsyncRequestCallback<OAuth2AccessToken> callback) {
    return getAccessTokenPasswordGrantAsync(username, password, null, callback);
  }

  /**
   * @param username username
   * @param password password
   * @param scope scope
   * @param callback callback
   * @return CompletableFuture
   * @deprecated use {@link
   *     #getAccessTokenAsync(com.github.scribejava.core.oauth2.grant.OAuth20Grant)} with {@link
   *     com.github.scribejava.core.oauth2.grant.PasswordGrant}
   */
  @Deprecated
  public CompletableFuture<OAuth2AccessToken> getAccessTokenPasswordGrantAsync(
      String username,
      String password,
      String scope,
      OAuthAsyncRequestCallback<OAuth2AccessToken> callback) {
    final OAuthRequest request = createAccessTokenPasswordGrantRequest(username, password, scope);

    return sendAccessTokenRequestAsync(request, callback);
  }

  // ===== get AccessToken client credentials flow methods =====
  protected OAuthRequest createAccessTokenClientCredentialsGrantRequest(String scope) {
    final OAuthRequest request =
        new OAuthRequest(api.getAccessTokenVerb(), api.getAccessTokenEndpoint());

    api.getClientAuthentication().addClientAuthentication(request, getApiKey(), getApiSecret());

    if (scope != null) {
      request.addParameter(OAuthConstants.SCOPE, scope);
    } else if (defaultScope != null) {
      request.addParameter(OAuthConstants.SCOPE, defaultScope);
    }
    request.addParameter(OAuthConstants.GRANT_TYPE, OAuthConstants.CLIENT_CREDENTIALS);

    logRequestWithParams("access token client credentials grant", request);

    return request;
  }

  /**
   * @return CompletableFuture
   * @deprecated use {@link
   *     #getAccessTokenAsync(com.github.scribejava.core.oauth2.grant.OAuth20Grant)} with {@link
   *     com.github.scribejava.core.oauth2.grant.ClientCredentialsGrant}
   */
  @Deprecated
  public CompletableFuture<OAuth2AccessToken> getAccessTokenClientCredentialsGrantAsync() {
    return getAccessTokenClientCredentialsGrant(
        (OAuthAsyncRequestCallback<OAuth2AccessToken>) null);
  }

  /**
   * @param scope scope
   * @return CompletableFuture
   * @deprecated use {@link
   *     #getAccessTokenAsync(com.github.scribejava.core.oauth2.grant.OAuth20Grant)} with {@link
   *     com.github.scribejava.core.oauth2.grant.ClientCredentialsGrant}
   */
  @Deprecated
  public CompletableFuture<OAuth2AccessToken> getAccessTokenClientCredentialsGrantAsync(
      String scope) {
    return getAccessTokenClientCredentialsGrant(scope, null);
  }

  /**
   * @return OAuth2AccessToken
   * @throws IOException IOException
   * @throws InterruptedException InterruptedException
   * @throws ExecutionException ExecutionException
   * @deprecated use {@link #getAccessToken(com.github.scribejava.core.oauth2.grant.OAuth20Grant)}
   *     with {@link com.github.scribejava.core.oauth2.grant.ClientCredentialsGrant}
   */
  @Deprecated
  public OAuth2AccessToken getAccessTokenClientCredentialsGrant()
      throws IOException, InterruptedException, ExecutionException {
    final OAuthRequest request = createAccessTokenClientCredentialsGrantRequest(null);

    return sendAccessTokenRequestSync(request);
  }

  /**
   * @param scope scope
   * @return OAuth2AccessToken
   * @throws IOException IOException
   * @throws InterruptedException InterruptedException
   * @throws ExecutionException ExecutionException
   * @deprecated use {@link #getAccessToken(com.github.scribejava.core.oauth2.grant.OAuth20Grant)}
   *     with {@link com.github.scribejava.core.oauth2.grant.ClientCredentialsGrant}
   */
  @Deprecated
  public OAuth2AccessToken getAccessTokenClientCredentialsGrant(String scope)
      throws IOException, InterruptedException, ExecutionException {
    return getAccessToken(
        new com.github.scribejava.core.oauth2.grant.ClientCredentialsGrant(scope));
  }

  /**
   * Start the request to retrieve the access token using client-credentials grant. The optionally
   * provided callback will be called with the Token when it is available.
   *
   * @param callback optional callback
   * @return CompletableFuture
   * @deprecated use {@link
   *     #getAccessTokenAsync(com.github.scribejava.core.oauth2.grant.OAuth20Grant)} with {@link
   *     com.github.scribejava.core.oauth2.grant.ClientCredentialsGrant}
   */
  @Deprecated
  public CompletableFuture<OAuth2AccessToken> getAccessTokenClientCredentialsGrant(
      OAuthAsyncRequestCallback<OAuth2AccessToken> callback) {
    final OAuthRequest request = createAccessTokenClientCredentialsGrantRequest(null);

    return sendAccessTokenRequestAsync(request, callback);
  }

  /**
   * @param scope scope
   * @param callback callback
   * @return CompletableFuture
   * @deprecated use {@link
   *     #getAccessTokenAsync(com.github.scribejava.core.oauth2.grant.OAuth20Grant)} with {@link
   *     com.github.scribejava.core.oauth2.grant.ClientCredentialsGrant}
   */
  @Deprecated
  public CompletableFuture<OAuth2AccessToken> getAccessTokenClientCredentialsGrant(
      String scope, OAuthAsyncRequestCallback<OAuth2AccessToken> callback) {
    final OAuthRequest request = createAccessTokenClientCredentialsGrantRequest(scope);

    return sendAccessTokenRequestAsync(request, callback);
  }

  // ===== revoke AccessToken methods =====
  protected OAuthRequest createRevokeTokenRequest(
      String tokenToRevoke, TokenTypeHint tokenTypeHint) {
    return revocationHandler.createRevokeTokenRequest(tokenToRevoke, tokenTypeHint);
  }

  /**
   * Révoque un jeton de manière asynchrone.
   *
   * @param tokenToRevoke Le jeton à invalider.
   * @return Un {@link CompletableFuture} représentant l'opération de révocation.
   * @see <a href="https://tools.ietf.org/html/rfc7009">RFC 7009 (Token Revocation)</a>
   */
  public CompletableFuture<Void> revokeTokenAsync(String tokenToRevoke) {
    return revokeTokenAsync(tokenToRevoke, null);
  }

  /**
   * Révoque un jeton de manière asynchrone avec un indice de type.
   *
   * @param tokenToRevoke Le jeton à invalider.
   * @param tokenTypeHint Indice sur le type de jeton.
   * @return Un {@link CompletableFuture}.
   * @see <a href="https://tools.ietf.org/html/rfc7009">RFC 7009 (Token Revocation)</a>
   */
  public CompletableFuture<Void> revokeTokenAsync(
      String tokenToRevoke, TokenTypeHint tokenTypeHint) {
    return revokeToken(tokenToRevoke, null, tokenTypeHint);
  }

  /**
   * Révoque un jeton de manière synchrone.
   *
   * @param tokenToRevoke Le jeton à invalider.
   * @throws IOException en cas d'erreur réseau.
   * @throws InterruptedException si le thread est interrompu.
   * @throws ExecutionException si l'exécution échoue.
   * @see <a href="https://tools.ietf.org/html/rfc7009">RFC 7009 (Token Revocation)</a>
   */
  public void revokeToken(String tokenToRevoke)
      throws IOException, InterruptedException, ExecutionException {
    revokeToken(tokenToRevoke, (TokenTypeHint) null);
  }

  /**
   * Révoque un jeton de manière synchrone.
   *
   * @param tokenToRevoke Le jeton à invalider.
   * @param tokenTypeHint Indice optionnel sur le type de jeton (access_token ou refresh_token).
   * @throws IOException en cas d'erreur réseau.
   * @throws InterruptedException si le thread est interrompu.
   * @throws ExecutionException si la requête échoue.
   * @see <a href="https://tools.ietf.org/html/rfc7009">RFC 7009 (Token Revocation)</a>
   */
  public void revokeToken(String tokenToRevoke, TokenTypeHint tokenTypeHint)
      throws IOException, InterruptedException, ExecutionException {
    revocationHandler.revokeToken(tokenToRevoke, tokenTypeHint);
  }

  /**
   * Révoque un jeton de manière asynchrone avec un rappel.
   *
   * @param tokenToRevoke Le jeton à invalider.
   * @param callback Le rappel à invoquer après l'opération.
   * @return Un {@link CompletableFuture}.
   * @see <a href="https://tools.ietf.org/html/rfc7009">RFC 7009 (Token Revocation)</a>
   */
  public CompletableFuture<Void> revokeToken(
      String tokenToRevoke, OAuthAsyncRequestCallback<Void> callback) {
    return revokeToken(tokenToRevoke, callback, null);
  }

  /**
   * Révoque un jeton de manière asynchrone avec un rappel et un indice de type.
   *
   * @param tokenToRevoke Le jeton à invalider.
   * @param callback Le rappel.
   * @param tokenTypeHint Indice optionnel sur le type de jeton.
   * @return Un {@link CompletableFuture}.
   * @see <a href="https://tools.ietf.org/html/rfc7009">RFC 7009 (Token Revocation)</a>
   */
  public CompletableFuture<Void> revokeToken(
      String tokenToRevoke, OAuthAsyncRequestCallback<Void> callback, TokenTypeHint tokenTypeHint) {
    final OAuthRequest request = createRevokeTokenRequest(tokenToRevoke, tokenTypeHint);

    return execute(
        request,
        callback,
        new OAuthRequest.ResponseConverter<Void>() {
          @Override
          public Void convert(Response response) throws IOException {
            checkForErrorRevokeToken(response);
            response.close();
            return null;
          }
        });
  }

  private void checkForErrorRevokeToken(Response response) throws IOException {
    revocationHandler.checkForError(response);
  }

  // ===== device Authorisation codes methods =====
  protected OAuthRequest createDeviceAuthorizationCodesRequest(String scope) {
    return deviceFlowHandler.createDeviceAuthorizationCodesRequest(scope);
  }

  /**
   * Demande des codes d'autorisation pour le flux appareil (Device Flow) de manière synchrone.
   *
   * @return Un objet {@link DeviceAuthorization} contenant les codes.
   * @throws InterruptedException si le thread est interrompu.
   * @throws ExecutionException si la requête échoue.
   * @throws IOException en cas d'erreur réseau.
   * @see <a href="https://tools.ietf.org/html/rfc8628#section-3.1">RFC 8628, Section 3.1</a>
   */
  public DeviceAuthorization getDeviceAuthorizationCodes()
      throws InterruptedException, ExecutionException, IOException {
    return getDeviceAuthorizationCodes((String) null);
  }

  /**
   * Demande des codes d'autorisation pour le flux appareil avec une portée spécifique.
   *
   * @param scope La portée demandée.
   * @return Un objet {@link DeviceAuthorization}.
   * @throws InterruptedException si le thread est interrompu.
   * @throws ExecutionException si la requête échoue.
   * @throws IOException en cas d'erreur réseau.
   * @see <a href="https://tools.ietf.org/html/rfc8628#section-3.1">RFC 8628, Section 3.1</a>
   */
  public DeviceAuthorization getDeviceAuthorizationCodes(String scope)
      throws InterruptedException, ExecutionException, IOException {
    return deviceFlowHandler.getDeviceAuthorizationCodes(scope);
  }

  /**
   * Demande des codes d'autorisation appareil de manière asynchrone avec un rappel.
   *
   * @param callback Le rappel.
   * @return Un {@link CompletableFuture}.
   */
  public CompletableFuture<DeviceAuthorization> getDeviceAuthorizationCodes(
      OAuthAsyncRequestCallback<DeviceAuthorization> callback) {
    return getDeviceAuthorizationCodes(null, callback);
  }

  /**
   * Demande des codes d'autorisation appareil de manière asynchrone avec une portée et un rappel.
   *
   * @param scope La portée.
   * @param callback Le rappel.
   * @return Un {@link CompletableFuture}.
   */
  public CompletableFuture<DeviceAuthorization> getDeviceAuthorizationCodes(
      String scope, OAuthAsyncRequestCallback<DeviceAuthorization> callback) {
    final OAuthRequest request = createDeviceAuthorizationCodesRequest(scope);

    return execute(
        request,
        callback,
        new OAuthRequest.ResponseConverter<DeviceAuthorization>() {
          @Override
          public DeviceAuthorization convert(Response response) throws IOException {
            final DeviceAuthorization deviceAuthorization =
                api.getDeviceAuthorizationExtractor().extract(response);
            response.close();
            return deviceAuthorization;
          }
        });
  }

  /**
   * Demande des codes d'autorisation appareil de manière asynchrone.
   *
   * @return Un {@link CompletableFuture}.
   */
  public CompletableFuture<DeviceAuthorization> getDeviceAuthorizationCodesAsync() {
    return getDeviceAuthorizationCodesAsync(null);
  }

  /**
   * Demande des codes d'autorisation appareil de manière asynchrone avec une portée.
   *
   * @param scope La portée.
   * @return Un {@link CompletableFuture}.
   */
  public CompletableFuture<DeviceAuthorization> getDeviceAuthorizationCodesAsync(String scope) {
    return getDeviceAuthorizationCodes(scope, null);
  }

  // ===== get AccessToken Device Authorisation grant flow methods =====
  protected OAuthRequest createAccessTokenDeviceAuthorizationGrantRequest(
      DeviceAuthorization deviceAuthorization) {
    final OAuthRequest request =
        new OAuthRequest(api.getAccessTokenVerb(), api.getAccessTokenEndpoint());
    request.addParameter(OAuthConstants.GRANT_TYPE, "urn:ietf:params:oauth:grant-type:device_code");
    request.addParameter("device_code", deviceAuthorization.getDeviceCode());
    api.getClientAuthentication().addClientAuthentication(request, getApiKey(), getApiSecret());
    return request;
  }

  /**
   * Obtient un jeton d'accès pour le flux appareil de manière synchrone.
   *
   * @param deviceAuthorization Les informations d'autorisation appareil obtenues précédemment.
   * @return Le jeton d'accès.
   * @throws InterruptedException si le thread est interrompu.
   * @throws ExecutionException si la requête échoue.
   * @throws IOException en cas d'erreur réseau.
   */
  public OAuth2AccessToken getAccessTokenDeviceAuthorizationGrant(
      DeviceAuthorization deviceAuthorization)
      throws InterruptedException, ExecutionException, IOException {
    final OAuthRequest request =
        createAccessTokenDeviceAuthorizationGrantRequest(deviceAuthorization);

    try (Response response = execute(request)) {
      if (isDebug()) {
        log("got AccessTokenDeviceAuthorizationGrant response");
        log("response status code: %s", response.getCode());
        log("response body: %s", response.getBody());
      }
      return api.getAccessTokenExtractor().extract(response);
    }
  }

  /**
   * Obtient un jeton d'accès pour le flux appareil de manière asynchrone avec un rappel.
   *
   * @param deviceAuthorization Les informations d'autorisation appareil.
   * @param callback Le rappel.
   * @return Un {@link CompletableFuture}.
   */
  public CompletableFuture<OAuth2AccessToken> getAccessTokenDeviceAuthorizationGrant(
      DeviceAuthorization deviceAuthorization,
      OAuthAsyncRequestCallback<OAuth2AccessToken> callback) {
    final OAuthRequest request =
        createAccessTokenDeviceAuthorizationGrantRequest(deviceAuthorization);

    return execute(
        request,
        callback,
        new OAuthRequest.ResponseConverter<OAuth2AccessToken>() {
          @Override
          public OAuth2AccessToken convert(Response response) throws IOException {
            final OAuth2AccessToken accessToken = api.getAccessTokenExtractor().extract(response);
            response.close();
            return accessToken;
          }
        });
  }

  /**
   * Obtient un jeton d'accès pour le flux appareil de manière asynchrone.
   *
   * @param deviceAuthorization Les informations d'autorisation appareil.
   * @return Un {@link CompletableFuture}.
   */
  public CompletableFuture<OAuth2AccessToken> getAccessTokenDeviceAuthorizationGrantAsync(
      DeviceAuthorization deviceAuthorization) {
    return getAccessTokenDeviceAuthorizationGrant(deviceAuthorization, null);
  }

  /**
   * Interroge périodiquement le serveur pour obtenir un jeton d'accès (Device Flow).
   *
   * @param deviceAuthorization Les informations d'autorisation appareil.
   * @return Le jeton d'accès une fois obtenu.
   * @throws InterruptedException si le thread est interrompu.
   * @throws ExecutionException si la requête échoue.
   * @throws IOException en cas d'erreur réseau.
   */
  public OAuth2AccessToken pollAccessTokenDeviceAuthorizationGrant(
      DeviceAuthorization deviceAuthorization)
      throws InterruptedException, ExecutionException, IOException {
    return deviceFlowHandler.pollAccessTokenDeviceAuthorizationGrant(deviceAuthorization);
  }

  /**
   * Envoie une requête d'autorisation poussée (PAR) de manière asynchrone.
   *
   * @param responseType Type de réponse demandé.
   * @param apiKey Clé API du client.
   * @param callback URL de rappel.
   * @param scope Portée demandée.
   * @param state État opaque.
   * @param additionalParams Paramètres additionnels.
   * @return Un {@link CompletableFuture}.
   * @see <a href="https://tools.ietf.org/html/rfc9126">RFC 9126 (OAuth 2.0 PAR)</a>
   */
  public CompletableFuture<PushedAuthorizationResponse> pushAuthorizationRequestAsync(
      String responseType,
      String apiKey,
      String callback,
      String scope,
      String state,
      Map<String, String> additionalParams) {
    return pushAuthorizationRequestAsync(
        responseType, apiKey, callback, scope, state, additionalParams, null);
  }

  /**
   * Envoie une requête d'autorisation poussée (PAR) de manière asynchrone avec un rappel.
   *
   * @param responseType Le type de réponse.
   * @param apiKey Le Client ID.
   * @param callback L'URI de redirection.
   * @param scope La portée.
   * @param state L'état.
   * @param additionalParams Paramètres additionnels.
   * @param callbackConsumer Le rappel à invoquer.
   * @return Un {@link CompletableFuture}.
   * @see <a href="https://tools.ietf.org/html/rfc9126">RFC 9126 (OAuth 2.0 PAR)</a>
   */
  public CompletableFuture<PushedAuthorizationResponse> pushAuthorizationRequestAsync(
      String responseType,
      String apiKey,
      String callback,
      String scope,
      String state,
      Map<String, String> additionalParams,
      OAuthAsyncRequestCallback<PushedAuthorizationResponse> callbackConsumer) {
    return pushedAuthHandler.pushAuthorizationRequestAsync(
        responseType, apiKey, callback, scope, state, additionalParams, callbackConsumer);
  }
}
