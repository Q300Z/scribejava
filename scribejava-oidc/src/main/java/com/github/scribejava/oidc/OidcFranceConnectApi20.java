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

import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.httpclient.HttpClientConfig;
import com.github.scribejava.core.oauth2.clientauthentication.ClientAuthentication;
import com.github.scribejava.core.oauth2.clientauthentication.RequestBodyAuthenticationScheme;
import java.io.OutputStream;

/**
 * Implémentation de l'API FranceConnect OpenID Connect (v2).
 *
 * <p>Configure les points de terminaison Production et Bac à sable (Sandbox), l'authentification
 * client via corps de requête ({@link RequestBodyAuthenticationScheme}), l'absence de point de
 * révocation ({@code getRevokeTokenEndpoint() == null}) ainsi que le niveau eIDAS par défaut
 * ({@code acr_values=eidas1}).
 *
 * <p>Note sur les domaines de production : {@link #PROD_BASE_URL} ({@code
 * https://app.franceconnect.gouv.fr/api/v2}) renvoie actuellement une redirection HTTP 301 alors
 * que {@link #OIDC_PROD_BASE_URL} ({@code https://oidc.franceconnect.gouv.fr/api/v2}) est le point
 * d'entrée OIDC actif ; utilisez {@link #custom(String)} avec {@link #OIDC_PROD_BASE_URL} si vous
 * souhaitez cibler ce dernier directement.
 */
public class OidcFranceConnectApi20 extends DefaultOidcApi20 {

  /** URL de base Production historique de FranceConnect v2 (renvoie actuellement 301). */
  public static final String PROD_BASE_URL = "https://app.franceconnect.gouv.fr/api/v2";

  /** URL de base Production OIDC active de FranceConnect v2. */
  public static final String OIDC_PROD_BASE_URL = "https://oidc.franceconnect.gouv.fr/api/v2";

  /** URL de base Bac à sable (Sandbox / intégration) de FranceConnect v2. */
  public static final String SANDBOX_BASE_URL = "https://fcp-low.sbx.dev-franceconnect.fr/api/v2";

  /** Valeur {@code acr_values} par défaut injectée dans les requêtes d'autorisation. */
  public static final String DEFAULT_ACR_VALUES = "eidas1";

  private final String baseUrl;

  /** Constructeur protégé utilisant l'environnement Production par défaut. */
  protected OidcFranceConnectApi20() {
    this(PROD_BASE_URL);
  }

  /**
   * Constructeur protégé pour une URL de base FranceConnect personnalisée.
   *
   * @param baseUrl L'URL de base de l'API FranceConnect v2 (ex. {@link #SANDBOX_BASE_URL}).
   */
  protected OidcFranceConnectApi20(String baseUrl) {
    this.baseUrl = normalizeBaseUrl(baseUrl);
  }

  private static String normalizeBaseUrl(String baseUrl) {
    if (baseUrl == null || baseUrl.trim().isEmpty()) {
      throw new IllegalArgumentException("FranceConnect baseUrl must not be null or empty");
    }
    final String trimmed = baseUrl.trim();
    return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
  }

  /**
   * Retourne l'instance unique (singleton) configurée pour l'environnement Production.
   *
   * @return L'instance Production de {@link OidcFranceConnectApi20}.
   */
  public static OidcFranceConnectApi20 instance() {
    return InstanceHolder.INSTANCE;
  }

  /**
   * Retourne l'instance unique (singleton) configurée pour l'environnement Bac à sable (Sandbox).
   *
   * @return L'instance Sandbox de {@link OidcFranceConnectApi20}.
   */
  public static OidcFranceConnectApi20 sandbox() {
    return SandboxHolder.INSTANCE;
  }

  /**
   * Crée une instance configurée pour une URL de base personnalisée (ex. {@link
   * #OIDC_PROD_BASE_URL} ou serveur de test).
   *
   * @param baseUrl L'URL de base FranceConnect v2.
   * @return Une nouvelle instance de {@link OidcFranceConnectApi20}.
   */
  public static OidcFranceConnectApi20 custom(String baseUrl) {
    return new OidcFranceConnectApi20(baseUrl);
  }

  /**
   * Retourne l'identifiant de l'émetteur (Issuer) configuré ou issu des métadonnées.
   *
   * @return L'URL de l'émetteur FranceConnect.
   */
  public String getIssuer() {
    final OidcProviderMetadata metadata = getMetadata();
    return metadata != null && metadata.getIssuer() != null ? metadata.getIssuer() : baseUrl;
  }

  @Override
  public String getAuthorizationBaseUrl() {
    final String endpoint = super.getAuthorizationBaseUrl();
    return endpoint != null ? endpoint : baseUrl + "/authorize";
  }

  @Override
  public String getAccessTokenEndpoint() {
    final String endpoint = super.getAccessTokenEndpoint();
    return endpoint != null ? endpoint : baseUrl + "/token";
  }

  @Override
  public String getUserinfoEndpoint() {
    final String endpoint = super.getUserinfoEndpoint();
    return endpoint != null ? endpoint : baseUrl + "/userinfo";
  }

  @Override
  public String getJwksUri() {
    final String endpoint = super.getJwksUri();
    return endpoint != null ? endpoint : baseUrl + "/jwks";
  }

  /**
   * Retourne l'URL du point de terminaison de déconnexion ({@code end_session_endpoint}).
   *
   * @return L'URL de fin de session FranceConnect.
   */
  public String getEndSessionEndpoint() {
    final OidcProviderMetadata metadata = getMetadata();
    return metadata != null && metadata.getEndSessionEndpoint() != null
        ? metadata.getEndSessionEndpoint()
        : baseUrl + "/session/end";
  }

  @Override
  public String getRevokeTokenEndpoint() {
    return null;
  }

  @Override
  public ClientAuthentication getClientAuthentication() {
    return RequestBodyAuthenticationScheme.instance();
  }

  @Override
  public OidcService createService(
      String apiKey,
      String apiSecret,
      String callback,
      String defaultScope,
      String responseType,
      OutputStream debugStream,
      String userAgent,
      HttpClientConfig httpClientConfig,
      HttpClient httpClient) {
    final OidcService service =
        super.createService(
            apiKey,
            apiSecret,
            callback,
            defaultScope,
            responseType,
            debugStream,
            userAgent,
            httpClientConfig,
            httpClient);
    // Injecte acr_values=eidas1 par défaut si l'appelant n'a pas fourni de valeur non vide
    service
        .getAuthorizationRequestInterceptors()
        .add(
            params -> {
              final String currentAcr = params.get("acr_values");
              if (currentAcr == null || currentAcr.trim().isEmpty()) {
                params.put("acr_values", DEFAULT_ACR_VALUES);
              }
            });
    return service;
  }

  private static class InstanceHolder {
    private static final OidcFranceConnectApi20 INSTANCE =
        new OidcFranceConnectApi20(PROD_BASE_URL);
  }

  private static class SandboxHolder {
    private static final OidcFranceConnectApi20 INSTANCE =
        new OidcFranceConnectApi20(SANDBOX_BASE_URL);
  }
}
