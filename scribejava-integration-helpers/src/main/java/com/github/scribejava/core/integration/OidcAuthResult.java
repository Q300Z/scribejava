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
package com.github.scribejava.core.integration;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.oidc.StandardClaims;

/** Résultat d'une authentification OIDC réussie. */
public class OidcAuthResult extends AuthResult {

  private final StandardClaims userInfoClaims;

  /**
   * @param token token
   * @param userInfoClaims claims
   */
  public OidcAuthResult(OAuth2AccessToken token, StandardClaims userInfoClaims) {
    super(token);
    this.userInfoClaims = userInfoClaims;
  }

  @Override
  public OAuth2AccessToken getToken() {
    return super.getToken();
  }

  /**
   * Retourne l'email consolidé.
   *
   * @return Email de l'utilisateur.
   */
  public String getEmail() {
    return userInfoClaims != null ? userInfoClaims.getEmail().orElse(null) : null;
  }

  /**
   * @return claims
   */
  public StandardClaims getUserInfoClaims() {
    return userInfoClaims;
  }
}
