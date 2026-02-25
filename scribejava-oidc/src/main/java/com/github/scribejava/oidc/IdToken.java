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

import com.github.scribejava.core.model.Token;
import com.github.scribejava.oidc.model.Jwt;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Représentation d'un ID Token OpenID Connect natif. */
public class IdToken extends Token {

  private static final long serialVersionUID = -543543543543L;
  private final Map<String, Object> claims;
  private transient StandardClaims standardClaims;

  /**
   * @param rawIdToken chaîne brute
   */
  public IdToken(String rawIdToken) {
    super(rawIdToken);
    this.claims = Jwt.parse(rawIdToken).getPayload();
  }

  /**
   * @return les revendications (claims)
   */
  public Map<String, Object> getClaims() {
    return claims;
  }

  /**
   * @param name nom du claim
   * @return valeur
   */
  public Object getClaim(String name) {
    return claims.get(name);
  }

  /**
   * Retourne une revendication de manière sécurisée.
   *
   * @param name nom
   * @return Optional
   */
  public Optional<Object> getClaimOptional(String name) {
    return Optional.ofNullable(claims.get(name));
  }

  /**
   * Retourne les revendications standards typées.
   *
   * @return StandardClaims
   */
  public synchronized StandardClaims getStandardClaims() {
    if (standardClaims == null) {
      standardClaims = new StandardClaims(claims);
    }
    return standardClaims;
  }

  @Override
  public String toString() {
    return "IdToken{rawIdToken='" + getRawResponse() + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IdToken)) {
      return false;
    }
    final IdToken idToken = (IdToken) o;
    return Objects.equals(getRawResponse(), idToken.getRawResponse())
        && Objects.equals(claims, idToken.claims);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getRawResponse(), claims);
  }
}
