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
import com.github.scribejava.oidc.model.RsaOidcKey;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.openid.connect.sdk.Nonce;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Validateur hybride (Compatibilité Nimbus / Moteur ScribeJava Natif).
 */
public class IdTokenValidator {

  private final String issuer;
  private final String clientID;
  private final String expectedAlg;
  private final Map<String, OidcKey> keys;
  private final JwtSignatureVerifier signatureVerifier;

  /**
   * Constructeur compatible Nimbus.
   * @param issuer émetteur
   * @param clientID id client
   * @param jwsAlgorithm algorithme
   * @param jwkSet clés
   */
  public IdTokenValidator(String issuer, ClientID clientID, JWSAlgorithm jwsAlgorithm, JWKSet jwkSet) {
    this(issuer, clientID.getValue(), jwsAlgorithm.getName(), convert(jwkSet));
  }

  /**
   * Constructeur compatible Nimbus avec secret.
   * @param issuer émetteur
   * @param clientID id client
   * @param jwsAlgorithm algorithme
   * @param jwkSet clés
   * @param clientSecret secret
   */
  public IdTokenValidator(String issuer, ClientID clientID, JWSAlgorithm jwsAlgorithm, JWKSet jwkSet, String clientSecret) {
    this(issuer, clientID.getValue(), jwsAlgorithm.getName(), convert(jwkSet));
    Objects.requireNonNull(clientSecret, "Client secret is not used in this implementation but kept for compatibility.");
  }

  /**
   * Constructeur compatible JWE.
   * @param issuer émetteur
   * @param clientID id client
   * @param jwsAlgorithm algorithme
   * @param jwkSet clés
   * @param clientSecret secret
   * @param clientPrivateJWK clé privée
   */
  public IdTokenValidator(String issuer, ClientID clientID, JWSAlgorithm jwsAlgorithm, JWKSet jwkSet, String clientSecret, JWK clientPrivateJWK) {
    this(issuer, clientID.getValue(), jwsAlgorithm.getName(), convert(jwkSet));
    Objects.requireNonNull(clientSecret);
    Objects.requireNonNull(clientPrivateJWK);
  }

  /**
   * Constructeur Natif ScribeJava.
   * @param issuer émetteur
   * @param clientID id client
   * @param expectedAlg algorithme
   * @param keys clés natives
   */
  public IdTokenValidator(String issuer, String clientID, String expectedAlg, Map<String, OidcKey> keys) {
    this.issuer = issuer;
    this.clientID = clientID;
    this.expectedAlg = expectedAlg;
    this.keys = keys;
    this.signatureVerifier = new JwtSignatureVerifier();
  }

  private static Map<String, OidcKey> convert(JWKSet set) {
    Map<String, OidcKey> map = new HashMap<>();
    if (set == null) {
      return map;
    }
    for (JWK jwk : set.getKeys()) {
        try {
            if (jwk instanceof RSAKey) {
                RSAKey rsa = (RSAKey) jwk;
                map.put(rsa.getKeyID(), new RsaOidcKey(rsa.getKeyID(), "RS256", rsa.toPublicKey()));
            }
        } catch (Exception e) {
            // Ignored
        }
    }
    return map;
  }

  /**
   * Valide via OidcNonce natif.
   * @param idTokenString token
   * @param expectedNonce nonce
   * @param maxAuthAgeSeconds age max
   * @return IdToken
   * @throws OAuthException erreur
   */
  public IdToken validate(String idTokenString, OidcNonce expectedNonce, long maxAuthAgeSeconds) throws OAuthException {
    final String nonceValue = expectedNonce != null ? expectedNonce.getValue() : null;
    return performValidation(idTokenString, nonceValue, maxAuthAgeSeconds);
  }

  /**
   * Valide via Nonce Nimbus (compatibilité).
   * @param idTokenString token
   * @param expectedNonce nonce
   * @param maxAuthAgeSeconds age max
   * @return IdToken
   * @throws OAuthException erreur
   */
  public IdToken validate(String idTokenString, Nonce expectedNonce, long maxAuthAgeSeconds) throws OAuthException {
    final String nonceValue = expectedNonce != null ? expectedNonce.getValue() : null;
    return performValidation(idTokenString, nonceValue, maxAuthAgeSeconds);
  }

  private IdToken performValidation(String idTokenString, String nonceValue, long maxAuthAgeSeconds) throws OAuthException {
    final Jwt jwt = Jwt.parse(idTokenString);
    final Map<String, Object> claims = jwt.getPayload();

    final String alg = (String) jwt.getHeader().get("alg");
    if (!expectedAlg.equals(alg)) {
      throw new OAuthException("Invalid algorithm.");
    }

    verifySignature(jwt);
    validateBaseClaims(claims);

    if (maxAuthAgeSeconds > 0) {
        validateMaxAuthAge(claims, maxAuthAgeSeconds);
    }

    if (nonceValue != null) {
      final String nonce = (String) claims.get("nonce");
      if (!nonceValue.equals(nonce)) {
        throw new OAuthException("Nonce mismatch.");
      }
    }

    return new IdToken(idTokenString);
  }

  private void validateBaseClaims(Map<String, Object> claims) throws OAuthException {
    if (!issuer.equals(claims.get("iss"))) {
      throw new OAuthException("Issuer mismatch.");
    }

    final Object aud = claims.get("aud");
    boolean audValid = false;
    if (aud instanceof String) {
      audValid = clientID.equals(aud);
    } else if (aud instanceof List) {
      audValid = ((List<?>) aud).contains(clientID);
      if (audValid && ((List<?>) aud).size() > 1 && claims.get("azp") == null) {
        throw new OAuthException("ID Token has multiple audiences but 'azp' claim is missing.");
      }
    }

    if (!audValid) {
      throw new OAuthException("Audience mismatch.");
    }

    final long now = new Date().getTime() / 1000;
    final Number exp = (Number) claims.get("exp");
    if (exp == null || now > exp.longValue()) {
      throw new OAuthException("Token has expired.");
    }
  }

  private void validateMaxAuthAge(Map<String, Object> claims, long maxAuthAgeSeconds) throws OAuthException {
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
      throw new OAuthException("Key not found.");
    }

    if (!signatureVerifier.verifyRS256(jwt.getSignedContent(), jwt.getSignature(), key.getPublicKey())) {
      throw new OAuthException("Signature verification failed.");
    }
  }

  /**
   * Valide un Logout Token.
   * @param logoutTokenString token
   * @throws OAuthException erreur
   */
  public void validateLogoutToken(String logoutTokenString) throws OAuthException {
    final Jwt jwt = Jwt.parse(logoutTokenString);
    verifySignature(jwt);
    if (jwt.getPayload().containsKey("nonce")) {
      throw new OAuthException("Logout Token MUST NOT contain a nonce.");
    }
  }

  /**
   * Valide la liaison.
   * @param idToken token
   * @param expectedJkt jkt
   * @param expectedX5t x5t
   * @throws OAuthException erreur
   */
  public void validateTokenBinding(IdToken idToken, String expectedJkt, String expectedX5t) throws OAuthException {
      final Object cnf = idToken.getClaim("cnf");
      if (!(cnf instanceof Map)) {
        return;
      }
      final Map<?, ?> cnfMap = (Map<?, ?>) cnf;
      if (expectedJkt != null && !expectedJkt.equals(cnfMap.get("jkt"))) {
          throw new OAuthException("DPoP proof key mismatch.");
      }
  }
}
