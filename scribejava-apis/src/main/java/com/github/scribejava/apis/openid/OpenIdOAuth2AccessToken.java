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

public class OpenIdOAuth2AccessToken extends OAuth2AccessToken {

  private static final long serialVersionUID = -4534058186528117610L;

  /**
   * Id_token is part of OpenID Connect specification. It can hold user information that you can
   * directly extract without additional request to provider.
   *
   * <p>See http://openid.net/specs/openid-connect-core-1_0.html#id_token-tokenExample and
   * https://bitbucket.org/nimbusds/nimbus-jose-jwt/wiki/Home
   *
   * <p>Here will be encoded and signed id token in JWT format or null, if not defined.
   */
  private final String openIdToken;

  public OpenIdOAuth2AccessToken(String accessToken, String openIdToken, String rawResponse) {
    this(accessToken, null, null, null, null, openIdToken, rawResponse);
  }

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
