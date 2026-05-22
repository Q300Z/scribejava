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

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.oidc.jar.JarAuthorizationRequestConverter;
import com.github.scribejava.oidc.model.JwtSigner;
import java.security.PrivateKey;

/** Builder spécifique pour les services OpenID Connect (Support Discovery). */
public class OidcServiceBuilder extends ServiceBuilder {

  /**
   * Constructeur.
   *
   * @param apiKey Client ID
   */
  public OidcServiceBuilder(String apiKey) {
    super(apiKey);
  }

  @Override
  public OidcServiceBuilder defaultScope(String defaultScope) {
    super.defaultScope(defaultScope);
    return this;
  }

  @Override
  public OidcServiceBuilder callback(String callback) {
    super.callback(callback);
    return this;
  }

  @Override
  public OidcServiceBuilder httpClient(HttpClient httpClient) {
    super.httpClient(httpClient);
    return this;
  }

  @Override
  public OidcServiceBuilder apiSecret(String apiSecret) {
    super.apiSecret(apiSecret);
    return this;
  }

  /**
   * Configure automatiquement les endpoints via OIDC Discovery.
   *
   * @param issuerUri L'URI de l'émetteur
   * @param httpClient Le client HTTP pour faire la requête
   * @param userAgent Le user agent
   * @return this
   */
  public OidcServiceBuilder baseOnDiscovery(
      String issuerUri, HttpClient httpClient, String userAgent) {
    discoverFromIssuer(issuerUri, new OidcDiscoveryService(issuerUri, httpClient, userAgent));
    return this;
  }

  /**
   * @param audience audience
   * @param signingKey clé privée
   * @param keyId id clé
   * @param signer signataire
   * @return builder
   */
  public OidcServiceBuilder jwtSecuredAuthorizationRequest(
      String audience, PrivateKey signingKey, String keyId, JwtSigner signer) {
    this.authorizationRequestConverter(
        new JarAuthorizationRequestConverter(
            this.getApiKey(), audience, signingKey, keyId, signer));
    return this;
  }

  @Override
  public OidcServiceBuilder logger(com.github.scribejava.core.model.OAuthLogger logger) {
    super.logger(logger);
    return this;
  }

  /**
   * Construit une instance de service OIDC sans nécessiter de cast manuel.
   *
   * @param api L'instance de DefaultOidcApi20 configurée.
   * @return Une instance typée de OidcService.
   */
  public OidcService build(DefaultOidcApi20 api) {
    DefaultOidcApi20 apiToUse = api;
    if (getDiscoveryIssuer() != null && getDiscoveryService() != null) {
      try {
        final com.github.scribejava.core.oauth.DiscoveredEndpoints endpoints =
            getDiscoveryService().discoverAsync().get();
        apiToUse =
            new DefaultOidcApi20() {
              @Override
              public String getAccessTokenEndpoint() {
                return endpoints.getTokenEndpoint();
              }

              @Override
              public String getAuthorizationBaseUrl() {
                return endpoints.getAuthorizationEndpoint();
              }
            };
        apiToUse.setMetadata(api.getMetadata());
      } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
        throw new com.github.scribejava.core.exceptions.OAuthException(
            "Failed to discover OIDC endpoints", e);
      }
    }

    final OidcService service =
        apiToUse.createService(
            getApiKey(),
            getApiSecret(),
            getCallback(),
            getDefaultScope(),
            getResponseType(),
            getDebugStream(),
            getUserAgent(),
            getHttpClientConfig(),
            getHttpClient());

    if (getAuthorizationRequestConverter() != null) {
      service.setAuthorizationRequestConverter(getAuthorizationRequestConverter());
    }
    if (getLogger() != null) {
      service.setLogger(getLogger());
    } else if (getDebugStream() != null) {
      service.setLogger(new com.github.scribejava.core.model.DefaultOAuthLogger(getDebugStream()));
    }
    return service;
  }
}
