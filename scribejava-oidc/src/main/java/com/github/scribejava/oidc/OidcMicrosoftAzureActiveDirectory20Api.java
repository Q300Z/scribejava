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

/**
 * Implémentation de l'API Microsoft Azure Active Directory OpenID Connect 1.0 (v2.0).
 *
 * <p>Supporte la découverte dynamique et la validation des jetons d'identité pour Microsoft Entra
 * ID (ex-Azure AD).
 *
 * @see <a
 *     href="https://learn.microsoft.com/en-us/entra/identity-platform/v2-protocols-oidc">Microsoft
 *     Entra OIDC Documentation</a>
 * @see <a href="http://openid.net/specs/openid-connect-core-1_0.html">OpenID Connect Core 1.0</a>
 */
public class OidcMicrosoftAzureActiveDirectory20Api extends DefaultOidcApi20 {

  private static final String DEFAULT_TENANT = "common";
  private final String tenant;

  /** Constructeur protégé utilisant le tenant par défaut ("common"). */
  protected OidcMicrosoftAzureActiveDirectory20Api() {
    this(DEFAULT_TENANT);
  }

  /**
   * Constructeur protégé pour un tenant spécifique.
   *
   * @param tenant L'identifiant ou le nom du tenant Azure AD.
   */
  protected OidcMicrosoftAzureActiveDirectory20Api(String tenant) {
    this.tenant = tenant;
  }

  /**
   * Retourne l'instance unique (singleton) liée au tenant "common".
   *
   * @return L'instance de {@link OidcMicrosoftAzureActiveDirectory20Api}.
   */
  public static OidcMicrosoftAzureActiveDirectory20Api instance() {
    return InstanceHolder.INSTANCE;
  }

  /**
   * Crée une nouvelle instance de l'API pour un tenant spécifique.
   *
   * @param tenant L'identifiant du tenant.
   * @return Une nouvelle instance de {@link OidcMicrosoftAzureActiveDirectory20Api}.
   */
  public static OidcMicrosoftAzureActiveDirectory20Api custom(String tenant) {
    return new OidcMicrosoftAzureActiveDirectory20Api(tenant);
  }

  /**
   * Retourne l'identifiant de l'émetteur (Issuer) pour le tenant configuré.
   *
   * @return L'URL de l'émetteur Azure AD v2.0.
   */
  @Override
  public String getIssuer() {
    return "https://login.microsoftonline.com/" + tenant + "/v2.0";
  }

  @Override
  public String getAccessTokenEndpoint() {
    final String endpoint = super.getAccessTokenEndpoint();
    return endpoint != null
        ? endpoint
        : "https://login.microsoftonline.com/" + tenant + "/oauth2/v2.0/token";
  }

  @Override
  public String getAuthorizationBaseUrl() {
    final String baseUrl = super.getAuthorizationBaseUrl();
    return baseUrl != null
        ? baseUrl
        : "https://login.microsoftonline.com/" + tenant + "/oauth2/v2.0/authorize";
  }

  private static class InstanceHolder {
    private static final OidcMicrosoftAzureActiveDirectory20Api INSTANCE =
        new OidcMicrosoftAzureActiveDirectory20Api();
  }
}
