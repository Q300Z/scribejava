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

import com.github.scribejava.core.oauth2.clientauthentication.ClientAuthentication;
import com.github.scribejava.core.oauth2.clientauthentication.HttpBasicAuthenticationScheme;
import com.github.scribejava.core.oauth2.clientauthentication.RequestBodyAuthenticationScheme;
import java.util.List;

/**
 * Implémentation de l'API ProConnect (ex-AgentConnect) OpenID Connect (v2).
 *
 * <p>Permet de configurer librement le domaine ou l'URL de base (Production, Intégration/Sandbox ou
 * serveur local) et sélectionne le schéma d'authentification client ({@link
 * RequestBodyAuthenticationScheme} par défaut ou {@link HttpBasicAuthenticationScheme} selon les
 * métadonnées {@code token_endpoint_auth_methods_supported}).
 *
 * <p>Note sur les domaines : {@link #PROD_DOMAIN} ({@code partenaires.proconnect.gouv.fr}) est le
 * domaine partenaires/documentation, {@link #AGENTCONNECT_PROD_DOMAIN} ({@code
 * auth.agentconnect.gouv.fr}) correspond à l'IdP Production actif et {@link #SANDBOX_DOMAIN}
 * ({@code fca.integ01.dev-agentconnect.fr}) à l'environnement d'intégration/sandbox.
 */
public class OidcProConnectApi20 extends DefaultOidcApi20 {

  /** Domaine Production ProConnect configuré par défaut. */
  public static final String PROD_DOMAIN = "partenaires.proconnect.gouv.fr";

  /** Domaine Intégration / Sandbox ProConnect (AgentConnect). */
  public static final String SANDBOX_DOMAIN = "fca.integ01.dev-agentconnect.fr";

  /** Domaine IdP Production actif ProConnect / AgentConnect. */
  public static final String AGENTCONNECT_PROD_DOMAIN = "auth.agentconnect.gouv.fr";

  /** URL de base Production par défaut. */
  public static final String PROD_BASE_URL = "https://" + PROD_DOMAIN + "/api/v2";

  /** URL de base Sandbox / Intégration par défaut. */
  public static final String SANDBOX_BASE_URL = "https://" + SANDBOX_DOMAIN + "/api/v2";

  private final String baseUrl;

  /** Constructeur protégé utilisant l'environnement Production par défaut. */
  protected OidcProConnectApi20() {
    this(PROD_BASE_URL);
  }

  /**
   * Constructeur protégé acceptant un nom de domaine nu (ex. {@code
   * fca.integ01.dev-agentconnect.fr}) ou une URL complète {@code http(s)://...}.
   *
   * @param domainOrBaseUrl Le nom de domaine ou l'URL de base ProConnect.
   */
  protected OidcProConnectApi20(String domainOrBaseUrl) {
    this.baseUrl = normalizeBaseUrl(domainOrBaseUrl);
  }

  private static String normalizeBaseUrl(String domainOrBaseUrl) {
    if (domainOrBaseUrl == null || domainOrBaseUrl.trim().isEmpty()) {
      throw new IllegalArgumentException("ProConnect domain or baseUrl must not be null or empty");
    }
    String trimmed = domainOrBaseUrl.trim();
    while (trimmed.endsWith("/")) {
      trimmed = trimmed.substring(0, trimmed.length() - 1);
    }
    if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
      return trimmed;
    }
    if (trimmed.endsWith("/api/v2")) {
      return "https://" + trimmed;
    }
    return "https://" + trimmed + "/api/v2";
  }

  /**
   * Retourne l'instance unique (singleton) configurée pour l'environnement Production.
   *
   * @return L'instance Production de {@link OidcProConnectApi20}.
   */
  public static OidcProConnectApi20 instance() {
    return InstanceHolder.INSTANCE;
  }

  /**
   * Retourne l'instance unique (singleton) configurée pour l'environnement Sandbox / Intégration.
   *
   * @return L'instance Sandbox de {@link OidcProConnectApi20}.
   */
  public static OidcProConnectApi20 sandbox() {
    return SandboxHolder.INSTANCE;
  }

  /**
   * Crée une instance configurée pour un domaine ou une URL de base spécifique.
   *
   * @param domainOrBaseUrl Nom de domaine (ex. {@link #AGENTCONNECT_PROD_DOMAIN}) ou URL complète
   *     {@code http(s)://...}.
   * @return Une nouvelle instance de {@link OidcProConnectApi20}.
   */
  public static OidcProConnectApi20 custom(String domainOrBaseUrl) {
    return new OidcProConnectApi20(domainOrBaseUrl);
  }

  /**
   * Retourne l'identifiant de l'émetteur (Issuer) issu des métadonnées ou dérivé du domaine.
   *
   * @return L'URL de l'émetteur ProConnect.
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
   * @return L'URL de fin de session ProConnect.
   */
  public String getEndSessionEndpoint() {
    final OidcProviderMetadata metadata = getMetadata();
    return metadata != null && metadata.getEndSessionEndpoint() != null
        ? metadata.getEndSessionEndpoint()
        : baseUrl + "/session/end";
  }

  @Override
  public ClientAuthentication getClientAuthentication() {
    final OidcProviderMetadata metadata = getMetadata();
    if (metadata != null) {
      final List<String> authMethods = metadata.getTokenEndpointAuthMethodsSupported();
      if (authMethods != null && !authMethods.isEmpty()) {
        if (authMethods.contains("client_secret_post")) {
          return RequestBodyAuthenticationScheme.instance();
        }
        if (authMethods.contains("client_secret_basic")) {
          return HttpBasicAuthenticationScheme.instance();
        }
      }
    }
    return RequestBodyAuthenticationScheme.instance();
  }

  private static class InstanceHolder {
    private static final OidcProConnectApi20 INSTANCE = new OidcProConnectApi20(PROD_BASE_URL);
  }

  private static class SandboxHolder {
    private static final OidcProConnectApi20 INSTANCE = new OidcProConnectApi20(SANDBOX_BASE_URL);
  }
}
