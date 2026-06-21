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
import com.github.scribejava.oidc.model.DefaultSignatureVerifier;
import com.github.scribejava.oidc.model.Jwt;
import com.github.scribejava.oidc.model.OidcKey;
import com.github.scribejava.oidc.model.OidcNonce;
import com.github.scribejava.oidc.model.SignatureVerifier;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Validateur natif pour les ID Tokens OpenID Connect. */
public class IdTokenValidator {

  private static final Logger LOGGER = Logger.getLogger(IdTokenValidator.class.getName());

  private final String issuer;
  private final String clientID;
  private final String expectedAlg;
  private final OidcKeyCache keys;
  private SignatureVerifier signatureVerifier;
  private IssuerValidator issuerValidator = new DefaultIssuerValidator();

  // Optionnel pour la rotation automatique
  private final OidcDiscoveryService discoveryService;
  private final String jwksUri;
  private long lastReloadTime;

  // Anti-DoS cooldown for failed kids resolution
  private static final long FAILED_KID_COOLDOWN_MS = 300_000L; // 5 minutes
  private final Map<String, Long> failedKids = new ConcurrentHashMap<>();

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
    this(issuer, clientID, expectedAlg, toCache(keys), discoveryService, jwksUri);
  }

  /**
   * Constructor with key rotation support using a custom key cache.
   *
   * @param issuer the expected issuer URL
   * @param clientID the client ID registered with the OIDC provider
   * @param expectedAlg the expected JWS algorithm used to sign the ID token (e.g., RS256)
   * @param keys the custom {@link OidcKeyCache} implementation
   * @param discoveryService the OIDC discovery service to fetch provider metadata and keys
   * @param jwksUri the OIDC provider's JWKS URI
   */
  public IdTokenValidator(
      String issuer,
      String clientID,
      String expectedAlg,
      OidcKeyCache keys,
      OidcDiscoveryService discoveryService,
      String jwksUri) {
    this.issuer = issuer;
    this.clientID = clientID;
    this.expectedAlg = expectedAlg;
    this.keys = keys != null ? keys : new DefaultOidcKeyCache();
    this.signatureVerifier = new DefaultSignatureVerifier();
    this.discoveryService = discoveryService;
    this.jwksUri = jwksUri;
  }

  private static OidcKeyCache toCache(Map<String, OidcKey> keysMap) {
    final OidcKeyCache cache = new DefaultOidcKeyCache();
    if (keysMap != null) {
      cache.putAll(keysMap);
    }
    return cache;
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
    final String claimIssuer = (String) claims.get("iss");
    if (!isIssuerMatching(issuer, claimIssuer, claims)) {
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
      // Cooldown check for failed kid
      final long now = System.currentTimeMillis();
      final Long lastFailure = failedKids.get(kid);
      if (lastFailure == null || now - lastFailure > FAILED_KID_COOLDOWN_MS) {
        reloadKeys();
        key = keys.get(kid);
        if (key == null) {
          failedKids.put(kid, now);
        } else {
          failedKids.remove(kid);
        }
      }
    }

    if (key == null) {
      throw new OAuthException("Key not found for kid: " + kid);
    }

    final String alg = (String) jwt.getHeader().get("alg");
    if (alg == null) {
      throw new OAuthException("Missing 'alg' in header.");
    }

    if (!signatureVerifier.verify(
        alg, jwt.getSignedContent(), jwt.getSignature(), key.getPublicKey())) {
      throw new OAuthException("Signature verification failed.");
    }
  }

  private boolean isIssuerMatching(
      String configuredIssuer, String claimIssuer, Map<String, Object> claims) {
    if (issuerValidator != null) {
      return issuerValidator.isValid(configuredIssuer, claimIssuer, claims);
    }
    return new DefaultIssuerValidator().isValid(configuredIssuer, claimIssuer, claims);
  }

  private static final long RELOAD_COOLDOWN_MS = 300_000L; // 5 minutes

  private synchronized void reloadKeys() {
    final long now = System.currentTimeMillis();
    if (now - lastReloadTime < RELOAD_COOLDOWN_MS) {
      LOGGER.log(Level.FINE, "JWKS reload skipped due to cooldown.");
      return;
    }
    lastReloadTime = now;
    try {
      final Map<String, OidcKey> updatedKeys = discoveryService.getJwks(jwksUri);
      if (updatedKeys != null) {
        keys.putAll(updatedKeys);
      }
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Failed to reload OIDC provider keys from JWKS URI: " + jwksUri, e);
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

  /**
   * Gets the signature verifier.
   *
   * @return the {@link SignatureVerifier}
   */
  public SignatureVerifier getSignatureVerifier() {
    return signatureVerifier;
  }

  /**
   * Sets the signature verifier.
   *
   * @param signatureVerifier the {@link SignatureVerifier} to use
   */
  public void setSignatureVerifier(SignatureVerifier signatureVerifier) {
    this.signatureVerifier = signatureVerifier;
  }

  /**
   * Gets the issuer validator.
   *
   * @return the {@link IssuerValidator}
   */
  public IssuerValidator getIssuerValidator() {
    return issuerValidator;
  }

  /**
   * Sets the issuer validator.
   *
   * @param issuerValidator the {@link IssuerValidator} to use
   */
  public void setIssuerValidator(IssuerValidator issuerValidator) {
    this.issuerValidator = issuerValidator;
  }

  /**
   * Gets the key cache.
   *
   * @return the {@link OidcKeyCache}
   */
  public OidcKeyCache getKeys() {
    return keys;
  }
}
