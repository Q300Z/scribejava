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

import com.github.scribejava.core.model.OAuth2AccessToken;
import java.util.Objects;

/** Jeton d'accès OAuth 2.0 incluant un ID Token OpenID Connect. */
public class OpenIdOAuth2AccessToken extends OAuth2AccessToken {

  private static final long serialVersionUID = -4534058186528117610L;

  /**
   * Le jeton d'identité (ID Token) fait partie de la spécification OpenID Connect. Il peut contenir
   * des informations utilisateur que vous pouvez extraire directement.
   */
  private final String openIdToken;

  /**
   * Constructeur simple.
   *
   * @param accessToken Le jeton d'accès.
   * @param openIdToken Le jeton d'identité (ID Token).
   * @param rawResponse La réponse brute.
   */
  public OpenIdOAuth2AccessToken(String accessToken, String openIdToken, String rawResponse) {
    this(accessToken, null, null, null, null, openIdToken, rawResponse);
  }

  /**
   * Constructeur complet.
   *
   * @param accessToken Le jeton d'accès.
   * @param tokenType Le type de jeton.
   * @param expiresIn Durée de validité.
   * @param refreshToken Jeton de renouvellement.
   * @param scope Portée.
   * @param openIdToken Le jeton d'identité.
   * @param rawResponse La réponse brute.
   */
  public OpenIdOAuth2AccessToken(
      String accessToken,
      String tokenType,
      Integer expiresIn,
      String refreshToken,
      String scope,
      String openIdToken,
      String rawResponse) {
    super(accessToken, tokenType, expiresIn, refreshToken, scope, rawResponse);
    this.openIdToken = openIdToken;
  }

  /**
   * @return Le jeton d'identité (ID Token).
   */
  public String getOpenIdToken() {
    return openIdToken;
  }

  @Override
  public int hashCode() {
    int hash = super.hashCode();
    hash = 37 * hash + Objects.hashCode(openIdToken);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    if (!super.equals(obj)) {
      return false;
    }

    return Objects.equals(openIdToken, ((OpenIdOAuth2AccessToken) obj).getOpenIdToken());
  }
}
