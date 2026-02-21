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
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import java.util.Map;

/** OpenID Connect ID Token. */
public class IdToken extends Token {

  private static final long serialVersionUID = 1L;
  private final String rawToken;
  private final JWTClaimsSet claimsSet;

  public IdToken(final String rawToken) {
    super(rawToken);
    this.rawToken = rawToken;
    try {
      this.claimsSet = SignedJWT.parse(rawToken).getJWTClaimsSet();
    } catch (final ParseException e) {
      throw new com.github.scribejava.core.exceptions.OAuthException("Failed to parse ID Token", e);
    }
  }

  @Override
  public String getRawResponse() {
    return rawToken;
  }

  public JWTClaimsSet getClaimsSet() {
    return claimsSet;
  }

  public String getSubject() {
    return claimsSet.getSubject();
  }

  public String getIssuer() {
    return claimsSet.getIssuer();
  }

  public String getNonce() {
    return (String) claimsSet.getClaim("nonce");
  }

  public StandardClaims getStandardClaims() {
    return new StandardClaims(claimsSet.getClaims());
  }

  public Object getClaim(final String name) {
    return claimsSet.getClaim(name);
  }

  public Map<String, Object> getClaims() {
    return claimsSet.getClaims();
  }
}
