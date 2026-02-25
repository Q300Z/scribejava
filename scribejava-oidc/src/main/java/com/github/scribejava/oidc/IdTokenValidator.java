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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Validateur natif pour les ID Tokens OpenID Connect. */
public class IdTokenValidator {

  private final String issuer;
  private final String clientID;
  private final String expectedAlg;
  private final Map<String, OidcKey> keys;
  private final JwtSignatureVerifier signatureVerifier;

  // Optionnel pour la rotation automatique
  private final OidcDiscoveryService discoveryService;
  private final String jwksUri;

  /**
   * Constructeur sans support de rotation.
   *
   * @param issuer émetteur
   * @param clientID client
   * @param expectedAlg algorithme
   * @param keys clés initiales
   */
  public IdTokenValidator(
      String issuer, String clientID, String expectedAlg, Map<String, OidcKey> keys) {
    this(issuer, clientID, expectedAlg, keys, null, null);
  }

  /**
   * Constructeur avec support de rotation.
   *
   * @param issuer émetteur
   * @param clientID client
   * @param expectedAlg algorithme
   * @param keys clés initiales
   * @param discoveryService service de découverte
   * @param jwksUri URI du JWKS
   */
  public IdTokenValidator(
      String issuer,
      String clientID,
      String expectedAlg,
      Map<String, OidcKey> keys,
      OidcDiscoveryService discoveryService,
      String jwksUri) {
    this.issuer = issuer;
    this.clientID = clientID;
    this.expectedAlg = expectedAlg;
    this.keys = new HashMap<>(keys != null ? keys : new HashMap<>());
    this.signatureVerifier = new JwtSignatureVerifier();
    this.discoveryService = discoveryService;
    this.jwksUri = jwksUri;
  }

  /**
   * Valide un ID Token brut.
   *
   * @param idTokenString La chaîne brute du jeton
   * @param expectedNonce Le nonce attendu
   * @param maxAuthAgeSeconds L'âge maximum
   * @return L'IdToken validé
   * @throws OAuthException erreur
   */
  public IdToken validate(String idTokenString, OidcNonce expectedNonce, long maxAuthAgeSeconds)
      throws OAuthException {
    return validate(Jwt.parse(idTokenString), expectedNonce, maxAuthAgeSeconds);
  }

  /**
   * Valide un objet Jwt.
   *
   * @param jwt L'objet JWT
   * @param expectedNonce Le nonce attendu
   * @param maxAuthAgeSeconds L'âge maximum
   * @return L'IdToken validé
   * @throws OAuthException erreur
   */
  public IdToken validate(Jwt jwt, OidcNonce expectedNonce, long maxAuthAgeSeconds)
      throws OAuthException {
    final Map<String, Object> claims = jwt.getPayload();

    final String alg = (String) jwt.getHeader().get("alg");
    if (!expectedAlg.equals(alg)) {
      throw new OAuthException("Invalid algorithm. Expected " + expectedAlg + " but got " + alg);
    }

    verifySignature(jwt);
    validateBaseClaims(claims);

    if (maxAuthAgeSeconds > 0) {
      validateMaxAuthAge(claims, maxAuthAgeSeconds);
    }

    if (expectedNonce != null) {
      final String nonce = (String) claims.get("nonce");
      if (!expectedNonce.getValue().equals(nonce)) {
        throw new OAuthException("Nonce mismatch.");
      }
    }

    return new IdToken(jwt.getRawToken());
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
      final List<?> audList = (List<?>) aud;
      audValid = audList.contains(clientID);
      if (audValid && audList.size() > 1 && claims.get("azp") == null) {
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

    OidcKey key = keys.get(kid);
    if (key == null && discoveryService != null && jwksUri != null) {
      // Rotation automatique : recharger les clés une seule fois
      reloadKeys();
      key = keys.get(kid);
    }

    if (key == null) {
      throw new OAuthException("Key not found for kid: " + kid);
    }

    if (!signatureVerifier.verifyRS256(
        jwt.getSignedContent(), jwt.getSignature(), key.getPublicKey())) {
      throw new OAuthException("Signature verification failed.");
    }
  }

  private synchronized void reloadKeys() {
    try {
      final Map<String, OidcKey> updatedKeys = discoveryService.getJwks(jwksUri);
      if (updatedKeys != null) {
        keys.putAll(updatedKeys);
      }
    } catch (Exception e) {
      // Échec silencieux, on garde les anciennes clés
    }
  }

  /**
   * @param logoutTokenString token brut
   * @throws OAuthException si invalide
   */
  public void validateLogoutToken(String logoutTokenString) throws OAuthException {
    final Jwt jwt = Jwt.parse(logoutTokenString);
    verifySignature(jwt);
  }

  /**
   * Valide la liaison.
   *
   * @param idToken token
   * @param expectedJkt jkt
   * @param expectedX5t x5t
   * @throws OAuthException erreur
   */
  public void validateTokenBinding(IdToken idToken, String expectedJkt, String expectedX5t)
      throws OAuthException {
    final Object cnf = idToken.getClaim("cnf");
    if (!(cnf instanceof Map)) {
      return;
    }
    final Map<?, ?> cnfMap = (Map<?, ?>) cnf;
    if (expectedJkt != null && !expectedJkt.equals(cnfMap.get("jkt"))) {
      throw new OAuthException("DPoP proof key mismatch.");
    }
    if (expectedX5t != null && !expectedX5t.equals(cnfMap.get("x5t#S256"))) {
      throw new OAuthException("mTLS certificate mismatch.");
    }
  }
}
