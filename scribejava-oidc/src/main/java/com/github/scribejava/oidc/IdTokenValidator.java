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

import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.oidc.model.Jwt;
import com.github.scribejava.oidc.model.JwtSignatureVerifier;
import com.github.scribejava.oidc.model.OidcKey;
import com.github.scribejava.oidc.model.OidcNonce;
import java.util.Date;
import java.util.List;
import java.util.Map;

/** Validateur natif et autonome pour les ID Tokens OpenID Connect. */
public class IdTokenValidator {

  private final String issuer;
  private final String clientID;
  private final String expectedAlg;
  private final Map<String, OidcKey> keys;
  private final String clientSecret;
  private final JwtSignatureVerifier signatureVerifier;

  public IdTokenValidator(
      String issuer, String clientID, String expectedAlg, Map<String, OidcKey> keys) {
    this(issuer, clientID, expectedAlg, keys, null);
  }

  public IdTokenValidator(
      String issuer,
      String clientID,
      String expectedAlg,
      Map<String, OidcKey> keys,
      String clientSecret) {
    this.issuer = issuer;
    this.clientID = clientID;
    this.expectedAlg = expectedAlg;
    this.keys = keys;
    this.clientSecret = clientSecret;
    this.signatureVerifier = new JwtSignatureVerifier();
  }

  public IdToken validate(String idTokenString, OidcNonce expectedNonce, long maxAuthAgeSeconds)
      throws OAuthException {
    final Jwt jwt = Jwt.parse(idTokenString);
    final Map<String, Object> claims = jwt.getPayload();

    // 1. Algorithme
    final String alg = (String) jwt.getHeader().get("alg");
    if (!expectedAlg.equals(alg)) {
      throw new OAuthException("Invalid algorithm. Expected " + expectedAlg + " but got " + alg);
    }

    // 2. Signature
    verifySignature(jwt);

    // 3. Claims standards (RFC 7519 & OIDC Core)
    validateBaseClaims(claims);

    // 4. Nonce (Anti-Rejeu)
    if (expectedNonce != null) {
      final String nonce = (String) claims.get("nonce");
      if (!expectedNonce.getValue().equals(nonce)) {
        throw new OAuthException("Nonce mismatch.");
      }
    }

    // 5. Max Auth Age
    if (maxAuthAgeSeconds > 0) {
      validateMaxAuthAge(claims, maxAuthAgeSeconds);
    }

    return new IdToken(idTokenString);
  }

  private void validateBaseClaims(Map<String, Object> claims) throws OAuthException {
    // Issuer
    if (!issuer.equals(claims.get("iss"))) {
      throw new OAuthException("Issuer mismatch.");
    }

    // Audience
    final Object aud = claims.get("aud");
    boolean audValid = false;
    if (aud instanceof String) {
      audValid = clientID.equals(aud);
    } else if (aud instanceof List) {
      audValid = ((List<?>) aud).contains(clientID);
      // OIDC Core: azp is REQUIRED if there are multiple audiences
      if (audValid && ((List<?>) aud).size() > 1 && claims.get("azp") == null) {
        throw new OAuthException("ID Token has multiple audiences but 'azp' claim is missing.");
      }
    }

    if (!audValid) {
      throw new OAuthException("Audience mismatch.");
    }

    // Expiration
    final long now = new Date().getTime() / 1000;
    final Number exp = (Number) claims.get("exp");
    if (exp == null || now > exp.longValue()) {
      throw new OAuthException("Token has expired.");
    }
  }

  private void validateMaxAuthAge(Map<String, Object> claims, long maxAuthAgeSeconds)
      throws OAuthException {
    final Number authTime = (Number) claims.get("auth_time");
    if (authTime == null) {
      throw new OAuthException("ID Token does not contain 'auth_time' claim.");
    }
    final long now = new Date().getTime() / 1000;
    if (now - authTime.longValue() > maxAuthAgeSeconds) {
      throw new OAuthException("Auth time too old.");
    }
  }

  private void verifySignature(Jwt jwt) throws OAuthException {
    final String kid = (String) jwt.getHeader().get("kid");
    if (kid == null) {
      throw new OAuthException("Missing 'kid' in header.");
    }

    final OidcKey key = keys.get(kid);
    if (key == null) {
      throw new OAuthException("Key not found for kid: " + kid);
    }

    if (!signatureVerifier.verifyRS256(
        jwt.getSignedContent(), jwt.getSignature(), key.getPublicKey())) {
      throw new OAuthException("Signature verification failed.");
    }
  }

  public void validateLogoutToken(String logoutTokenString) throws OAuthException {
    final Jwt jwt = Jwt.parse(logoutTokenString);
    verifySignature(jwt);
    if (jwt.getPayload().containsKey("nonce")) {
      throw new OAuthException("Logout Token MUST NOT contain a nonce.");
    }
  }
}
