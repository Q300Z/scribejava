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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.httpclient.HttpClientConfig;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthConstants;
import com.github.scribejava.core.model.ParameterList;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.oidc.model.OidcNonce;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;

/**
 * Service OAuth 2.0 spécialisé pour OpenID Connect 1.0.
 *
 * <p>Étend {@link OAuth20Service} pour ajouter des fonctionnalités spécifiques à OIDC telles que la
 * validation de l'ID Token, la récupération des informations utilisateur (UserInfo), la déconnexion
 * initiée par le client (RP-Initiated Logout) et le support du mode "form_post".
 *
 * <h3>Exemple d'utilisation OpenID Connect</h3>
 *
 * <pre>
 * // 1. Construction du service (via Discovery recommandé)
 * final OidcService service = (OidcService) new ServiceBuilder("votre_client_id")
 *     .apiSecret("votre_client_secret")
 *     .defaultScope("openid profile email")
 *     .build(OidcGoogleApi20.instance());
 *
 * // 2. Échange du code contre un jeton (ID Token inclus)
 * final com.github.scribejava.core.model.OAuth2AccessToken token = service.getAccessToken(
 *     new com.github.scribejava.core.oauth2.grant.AuthorizationCodeGrant(code)
 * );
 *
 * // 3. Lecture des claims de l'ID Token
 * if (token instanceof com.github.scribejava.apis.openid.OpenIdOAuth2AccessToken) {
 *     final String rawIdToken = ((com.github.scribejava.apis.openid.OpenIdOAuth2AccessToken) token).getOpenIdToken();
 *     final com.github.scribejava.oidc.IdToken idToken = service.validateIdToken(rawIdToken);
 *     System.out.println("Sujet : " + idToken.getSubject());
 * }
 *
 * // 4. Récupération des informations utilisateur complètes (Asynchrone par défaut)
 * final com.github.scribejava.oidc.StandardClaims claims = service.getUserInfoAsync(token).get();
 * System.out.println("Email vérifié : " + claims.getEmail());
 * </pre>
 *
 * @see <a href="http://openid.net/specs/openid-connect-core-1_0.html">OpenID Connect Core 1.0</a>
 */
public class OidcService extends OAuth20Service {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private final IdTokenValidator idTokenValidator;

  /**
   * Constructeur complet.
   *
   * @param api L'instance de l'API OIDC.
   * @param apiKey La clé API du client.
   * @param apiSecret Le secret API du client.
   * @param callback L'URI de redirection.
   * @param defaultScope La portée par défaut.
   * @param responseType Le type de réponse.
   * @param debugStream Flux de débogage.
   * @param userAgent Chaîne User-Agent.
   * @param httpClientConfig Configuration du client HTTP.
   * @param httpClient Le client HTTP.
   * @param idTokenValidator Le validateur de jetons d'identité.
   */
  public OidcService(
      final DefaultApi20 api,
      final String apiKey,
      final String apiSecret,
      final String callback,
      final String defaultScope,
      final String responseType,
      final OutputStream debugStream,
      final String userAgent,
      final HttpClientConfig httpClientConfig,
      final HttpClient httpClient,
      final IdTokenValidator idTokenValidator) {
    super(
        api,
        apiKey,
        apiSecret,
        callback,
        defaultScope,
        responseType,
        debugStream,
        userAgent,
        httpClientConfig,
        httpClient);
    this.idTokenValidator = idTokenValidator;
  }

  /**
   * Valide l'ID Token contenu dans la réponse du point de terminaison de jeton.
   *
   * @param accessToken Le jeton d'accès contenant l'ID Token brut dans sa réponse.
   * @param expectedNonce La valeur de nonce attendue.
   * @return L'instance de {@link IdToken} validée.
   * @see <a href="http://openid.net/specs/openid-connect-core-1_0.html#IDTokenValidation">OIDC
   *     Core, Section 3.1.3.7</a>
   */
  public IdToken validateIdToken(
      final OAuth2AccessToken accessToken, final OidcNonce expectedNonce) {
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

  /**
   * Construit l'URL pour initier une déconnexion de l'utilisateur final.
   *
   * @param endSessionEndpoint L'URL du point de terminaison de fin de session du fournisseur.
   * @param idTokenHint L'ID Token précédemment délivré (recommandé).
   * @param postLogoutRedirectUri L'URI vers laquelle rediriger après la déconnexion.
   * @param state Valeur d'état opaque pour maintenir l'état entre la requête et le rappel.
   * @return L'URL de déconnexion configurée.
   * @see <a href="https://openid.net/specs/openid-connect-rpinitiated-1_0.html#RPLogout">OIDC
   *     RP-Initiated Logout 1.0, Section 2</a>
   */
  public String getRpInitiatedLogoutUrl(
      final String endSessionEndpoint,
      final String idTokenHint,
      final String postLogoutRedirectUri,
      final String state) {
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

  /**
   * Valide la présence et la valeur du paramètre {@code iss} dans la réponse d'autorisation.
   *
   * @param issuerResponse La valeur du paramètre {@code iss} reçue.
   * @param expectedIssuer L'identifiant de l'émetteur attendu.
   * @see <a href="https://tools.ietf.org/html/rfc9207">RFC 9207 (OAuth 2.0 AS Issuer
   *     Identification)</a>
   */
  public void validateIssuerResponse(final String issuerResponse, final String expectedIssuer) {
    if (issuerResponse != null && !issuerResponse.equals(expectedIssuer)) {
      throw new com.github.scribejava.core.exceptions.OAuthException(
          "Issuer mismatch in authorization response. Expected: "
              + expectedIssuer
              + ", Got: "
              + issuerResponse);
    }
  }

  /**
   * Récupère les informations utilisateur (Claims) de manière asynchrone.
   *
   * @param accessToken Le jeton d'accès autorisé.
   * @return Un {@link CompletableFuture} résolvant vers {@link StandardClaims}.
   * @see <a href="http://openid.net/specs/openid-connect-core-1_0.html#UserInfo">OIDC Core, Section
   *     5.3 (UserInfo Endpoint)</a>
   */
  public CompletableFuture<StandardClaims> getUserInfoAsync(final OAuth2AccessToken accessToken) {
    String userInfoEndpoint = null;
    if (getApi() instanceof DefaultOidcApi20) {
      userInfoEndpoint = ((DefaultOidcApi20) getApi()).getUserinfoEndpoint();
    }

    if (userInfoEndpoint == null || userInfoEndpoint.isEmpty()) {
      throw new com.github.scribejava.core.exceptions.OAuthException(
          "UserInfo endpoint is not defined.");
    }

    final com.github.scribejava.core.model.OAuthRequest request =
        new com.github.scribejava.core.model.OAuthRequest(
            com.github.scribejava.core.model.Verb.GET, userInfoEndpoint);
    signRequest(accessToken, request);

    return execute(
        request,
        null,
        response -> {
          try (com.github.scribejava.core.model.Response resp = response) {
            if (resp.getCode() != 200) {
              throw new com.github.scribejava.core.exceptions.OAuthException(
                  "UserInfo request failed. Status: "
                      + resp.getCode()
                      + ", Body: "
                      + resp.getBody());
            }
            return UserInfoJsonExtractor.instance().extract(resp);
          }
        });
  }
}
