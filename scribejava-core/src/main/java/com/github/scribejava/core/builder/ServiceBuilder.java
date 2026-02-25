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
package com.github.scribejava.core.builder;

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.httpclient.HttpClientConfig;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.utils.Preconditions;
import java.io.OutputStream;

/**
 * Implémentation du patron de conception Builder, avec une interface fluide permettant de créer un
 * service OAuth.
 */
public class ServiceBuilder implements ServiceBuilderOAuth20 {

  private String callback;
  private String apiKey;
  private String apiSecret;
  private String scope;
  private OutputStream debugStream;
  private String responseType = "code";
  private String userAgent;

  private HttpClientConfig httpClientConfig;
  private HttpClient httpClient;
  private com.github.scribejava.core.dpop.DPoPProofCreator dpopProofCreator;
  private com.github.scribejava.core.oauth.AuthorizationRequestConverter
      authorizationRequestConverter;

  private String discoveryIssuer;
  private com.github.scribejava.core.oauth.DiscoveryService discoveryService;

  /**
   * Constructeur.
   *
   * @param apiKey La clé API du client (Client ID).
   */
  public ServiceBuilder(String apiKey) {
    apiKey(apiKey);
  }

  /**
   * @return La clé API configurée.
   */
  public String getApiKey() {
    return apiKey;
  }

  /**
   * Définit le convertisseur de requête d'autorisation (ex: pour JAR ou PAR).
   *
   * @param authorizationRequestConverter L'instance du convertisseur.
   * @return L'instance actuelle du builder.
   */
  public ServiceBuilder authorizationRequestConverter(
      com.github.scribejava.core.oauth.AuthorizationRequestConverter
          authorizationRequestConverter) {
    this.authorizationRequestConverter = authorizationRequestConverter;
    return this;
  }

  protected com.github.scribejava.core.oauth.AuthorizationRequestConverter
      getAuthorizationRequestConverter() {
    return authorizationRequestConverter;
  }

  /**
   * Configure la découverte automatique des points de terminaison à partir d'un émetteur (Issuer).
   *
   * @param issuer L'URL de l'émetteur (Issuer URL).
   * @param service Le service de découverte à utiliser.
   * @return L'instance actuelle du builder.
   */
  public ServiceBuilder discoverFromIssuer(
      String issuer, com.github.scribejava.core.oauth.DiscoveryService service) {
    this.discoveryIssuer = issuer;
    this.discoveryService = service;
    return this;
  }

  /**
   * Active le support DPoP.
   *
   * @param creator Le créateur de preuves DPoP.
   * @return L'instance actuelle du builder.
   */
  public ServiceBuilder dpop(com.github.scribejava.core.dpop.DPoPProofCreator creator) {
    this.dpopProofCreator = creator;
    return this;
  }

  @Override
  public ServiceBuilder callback(String callback) {
    this.callback = callback;
    return this;
  }

  public final ServiceBuilder apiKey(String apiKey) {
    Preconditions.checkEmptyString(apiKey, "Invalid Api key");
    this.apiKey = apiKey;
    return this;
  }

  public ServiceBuilder apiSecret(String apiSecret) {
    Preconditions.checkEmptyString(apiSecret, "Invalid Api secret");
    this.apiSecret = apiSecret;
    return this;
  }

  public ServiceBuilder defaultScope(String defaultScope) {
    Preconditions.checkEmptyString(defaultScope, "Invalid OAuth scope");
    this.scope = defaultScope;
    return this;
  }

  @Override
  public ServiceBuilder httpClientConfig(HttpClientConfig httpClientConfig) {
    this.httpClientConfig = httpClientConfig;
    return this;
  }

  @Override
  public ServiceBuilder userAgent(String userAgent) {
    this.userAgent = userAgent;
    return this;
  }

  @Override
  public ServiceBuilder debug() {
    return debugStream(System.out);
  }

  public ServiceBuilder debugStream(OutputStream debugStream) {
    Preconditions.checkNotNull(debugStream, "debug stream can't be null");
    this.debugStream = debugStream;
    return this;
  }

  @Override
  public ServiceBuilder responseType(String responseType) {
    Preconditions.checkEmptyString(responseType, "Invalid OAuth responseType");
    this.responseType = responseType;
    return this;
  }

  /**
   * Définit le client HTTP à utiliser.
   *
   * @param httpClient Le client HTTP.
   * @return L'instance actuelle du builder.
   */
  public ServiceBuilder httpClient(HttpClient httpClient) {
    this.httpClient = httpClient;
    return this;
  }

  @Override
  public OAuth20Service build(final DefaultApi20 api) {
    DefaultApi20 apiToUse = api;
    if (discoveryIssuer != null && discoveryService != null) {
      try {
        final com.github.scribejava.core.oauth.DiscoveredEndpoints endpoints =
            discoveryService.discoverAsync().get();
        apiToUse =
            new DefaultApi20() {
              @Override
              public String getAccessTokenEndpoint() {
                return endpoints.getTokenEndpoint();
              }

              @Override
              public String getAuthorizationBaseUrl() {
                return endpoints.getAuthorizationEndpoint();
              }
            };
      } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
        throw new com.github.scribejava.core.exceptions.OAuthException(
            "Failed to discover endpoints", e);
      }
    }
    final OAuth20Service service;
    if (dpopProofCreator == null) {
      service =
          apiToUse.createService(
              apiKey,
              apiSecret,
              callback,
              scope,
              responseType,
              debugStream,
              userAgent,
              httpClientConfig,
              httpClient);
    } else {
      service =
          apiToUse.createService(
              apiKey,
              apiSecret,
              callback,
              scope,
              responseType,
              debugStream,
              userAgent,
              httpClientConfig,
              httpClient,
              dpopProofCreator);
    }

    if (authorizationRequestConverter != null) {
      service.setAuthorizationRequestConverter(authorizationRequestConverter);
    }
    return service;
  }
}
