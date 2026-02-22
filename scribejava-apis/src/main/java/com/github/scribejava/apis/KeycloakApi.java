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
package com.github.scribejava.apis;

import com.github.scribejava.apis.openid.OpenIdJsonTokenExtractor;
import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.extractors.TokenExtractor;
import com.github.scribejava.core.model.OAuth2AccessToken;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** API OAuth 2.0 pour Keycloak. */
public class KeycloakApi extends DefaultApi20 {

  private static final ConcurrentMap<String, KeycloakApi> INSTANCES = new ConcurrentHashMap<>();

  private final String baseUrlWithRealm;

  /**
   * Constructeur protégé.
   *
   * @param baseUrlWithRealm L'URL de base incluant le chemin du royaume.
   */
  protected KeycloakApi(String baseUrlWithRealm) {
    this.baseUrlWithRealm = baseUrlWithRealm;
  }

  /**
   * Retourne l'instance par défaut (localhost:8080, realm master).
   *
   * @return L'instance de {@link KeycloakApi}.
   */
  public static KeycloakApi instance() {
    return instance("http://localhost:8080/", "master");
  }

  /**
   * Retourne une instance pour un serveur et un royaume spécifiques.
   *
   * @param baseUrl L'URL de base du serveur Keycloak.
   * @param realm Le nom du royaume (realm).
   * @return L'instance de {@link KeycloakApi}.
   */
  public static KeycloakApi instance(String baseUrl, String realm) {
    final String defaultBaseUrlWithRealm = composeBaseUrlWithRealm(baseUrl, realm);

    // java8: switch to ConcurrentMap::computeIfAbsent
    KeycloakApi api = INSTANCES.get(defaultBaseUrlWithRealm);
    if (api == null) {
      api = new KeycloakApi(defaultBaseUrlWithRealm);
      final KeycloakApi alreadyCreatedApi = INSTANCES.putIfAbsent(defaultBaseUrlWithRealm, api);
      if (alreadyCreatedApi != null) {
        return alreadyCreatedApi;
      }
    }
    return api;
  }

  protected static String composeBaseUrlWithRealm(String baseUrl, String realm) {
    return baseUrl + (baseUrl.endsWith("/") ? "" : "/") + "auth/realms/" + realm;
  }

  @Override
  public String getAccessTokenEndpoint() {
    return baseUrlWithRealm + "/protocol/openid-connect/token";
  }

  @Override
  public String getAuthorizationBaseUrl() {
    return baseUrlWithRealm + "/protocol/openid-connect/auth";
  }

  @Override
  public TokenExtractor<OAuth2AccessToken> getAccessTokenExtractor() {
    return OpenIdJsonTokenExtractor.instance();
  }

  @Override
  public String getRevokeTokenEndpoint() {
    throw new RuntimeException("Not implemented yet");
  }
}
