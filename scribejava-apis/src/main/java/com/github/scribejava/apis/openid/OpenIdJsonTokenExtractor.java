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
package com.github.scribejava.apis.openid;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.scribejava.core.extractors.OAuth2AccessTokenJsonExtractor;

/**
 * Extracteur JSON pour les jetons OpenID Connect.
 *
 * <p>Analyse également le paramètre {@code id_token} en plus des paramètres OAuth 2.0 standards.
 */
public class OpenIdJsonTokenExtractor extends OAuth2AccessTokenJsonExtractor {

  /** Constructeur protégé. */
  protected OpenIdJsonTokenExtractor() {}

  /**
   * Retourne l'instance unique (singleton) de l'extracteur.
   *
   * @return L'instance de {@link OpenIdJsonTokenExtractor}.
   */
  public static OpenIdJsonTokenExtractor instance() {
    return InstanceHolder.INSTANCE;
  }

  @Override
  protected OpenIdOAuth2AccessToken createToken(
      String accessToken,
      String tokenType,
      Integer expiresIn,
      String refreshToken,
      String scope,
      JsonNode response,
      String rawResponse) {
    final JsonNode idToken = response.get("id_token");
    return new OpenIdOAuth2AccessToken(
        accessToken,
        tokenType,
        expiresIn,
        refreshToken,
        scope,
        idToken == null ? null : idToken.asText(),
        rawResponse);
  }

  private static class InstanceHolder {

    private static final OpenIdJsonTokenExtractor INSTANCE = new OpenIdJsonTokenExtractor();
  }
}
