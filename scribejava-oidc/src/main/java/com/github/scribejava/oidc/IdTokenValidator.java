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

  private final OidcDiscoveryService discoveryService;
  private final String jwksUri;
  private long lastReloadTime;
  private long lastReloadFailureTime;

  private long reloadCooldownMs = 300_000L; // 5 minutes
  private long reloadFailureCooldownMs = 10_000L; // 10 seconds

  private final Map<String, Boolean> signatureCache =
      java.util.Collections.synchronizedMap(
          new java.util.LinkedHashMap<String, Boolean>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
              return size() > 100;
            }
          });

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

    final Object algObj = jwt.getHeader().get("alg");
    if (!(algObj instanceof String) || !expectedAlg.equals(algObj)) {
      throw new OAuthException("Invalid algorithm. Expected " + expectedAlg + " but got " + algObj);
    }

    verifySignature(jwt);
    validateBaseClaims(claims);

    if (maxAuthAgeSeconds > 0) {
      validateMaxAuthAge(claims, maxAuthAgeSeconds);
    }

    if (expectedNonce != null) {
      final Object nonceObj = claims.get("nonce");
      if (!(nonceObj instanceof String) || !expectedNonce.getValue().equals(nonceObj)) {
        throw new OAuthException("Nonce mismatch.");
      }
    }

    return new IdToken(jwt.getRawToken());
  }

  /**
   * Valide un jeton JWT retourné par le point de terminaison UserInfo (OIDC Core §5.3.2).
   *
   * <p>Vérifie que l'algorithme est asymétrique supporté ({@code RS256} ou {@code ES256}), contrôle
   * la signature via les clés JWKS, valide {@code iss}, {@code aud}, la présence non vide de {@code
   * sub}, ainsi que {@code exp} uniquement s'il est présent dans les revendications.
   *
   * @param jwt Le jeton JWT UserInfo parsé.
   * @return Les revendications standards {@link StandardClaims}.
   * @throws OAuthException si l'algorithme, la signature ou les revendications sont invalides.
   */
  public StandardClaims validateUserInfoJwt(Jwt jwt) throws OAuthException {
    if (jwt == null) {
      throw new OAuthException("UserInfo JWT cannot be null.");
    }
    final Object algObj = jwt.getHeader().get("alg");
    if (!(algObj instanceof String) || !"RS256".equals(algObj) && !"ES256".equals(algObj)) {
      throw new OAuthException("Unsupported UserInfo JWT algorithm: " + algObj);
    }

    verifySignature(jwt);

    final Map<String, Object> claims = jwt.getPayload();
    validateIssuerAndAudience(claims);

    final Object subObj = claims.get("sub");
    if (!(subObj instanceof String) || ((String) subObj).trim().isEmpty()) {
      throw new OAuthException("UserInfo JWT missing or empty 'sub' claim.");
    }

    final Object expObj = claims.get("exp");
    if (expObj != null) {
      if (!(expObj instanceof Number)) {
        throw new OAuthException("Invalid 'exp' claim in UserInfo JWT.");
      }
      final long now = new Date().getTime() / 1000;
      if (now > ((Number) expObj).longValue()) {
        throw new OAuthException("UserInfo JWT has expired.");
      }
    }

    return new StandardClaims(claims);
  }

  private void validateBaseClaims(Map<String, Object> claims) throws OAuthException {
    validateIssuerAndAudience(claims);

    final long now = new Date().getTime() / 1000;
    final Object expObj = claims.get("exp");
    if (!(expObj instanceof Number) || now > ((Number) expObj).longValue()) {
      throw new OAuthException("Token has expired.");
    }
  }

  private void validateIssuerAndAudience(Map<String, Object> claims) throws OAuthException {
    final Object claimIssuerObj = claims.get("iss");
    if (!(claimIssuerObj instanceof String)
        || !isIssuerMatching(issuer, (String) claimIssuerObj, claims)) {
      throw new OAuthException("Issuer mismatch.");
    }

    final Object aud = claims.get("aud");
    boolean audValid = false;
    if (aud instanceof String) {
      audValid = clientID != null && clientID.equals(aud);
    } else if (aud instanceof List) {
      final List<?> audList = (List<?>) aud;
      audValid = clientID != null && audList.contains(clientID);
      if (audValid && audList.size() > 1) {
        final Object azpObj = claims.get("azp");
        if (azpObj == null) {
          throw new OAuthException("ID Token has multiple audiences but 'azp' claim is missing.");
        }
        if (!(azpObj instanceof String) || !clientID.equals(azpObj)) {
          throw new OAuthException("Authorized party ('azp') mismatch.");
        }
      }
    }

    if (!audValid) {
      throw new OAuthException("Audience mismatch.");
    }

    final Object azpObj = claims.get("azp");
    if (azpObj != null && (!(azpObj instanceof String) || !clientID.equals(azpObj))) {
      throw new OAuthException("Authorized party ('azp') mismatch.");
    }
  }

  private void validateMaxAuthAge(Map<String, Object> claims, long maxAuthAgeSeconds)
      throws OAuthException {
    final Object authTimeObj = claims.get("auth_time");
    if (!(authTimeObj instanceof Number)) {
      throw new OAuthException("ID Token does not contain 'auth_time' claim.");
    }
    final long now = new Date().getTime() / 1000;
    if (now - ((Number) authTimeObj).longValue() > maxAuthAgeSeconds) {
      throw new OAuthException("Auth time too old.");
    }
  }

  private void verifySignature(Jwt jwt) throws OAuthException {
    if (Boolean.TRUE.equals(signatureCache.get(jwt.getRawToken()))) {
      return;
    }

    final Object kidObj = jwt.getHeader().get("kid");
    if (!(kidObj instanceof String) || ((String) kidObj).isEmpty()) {
      throw new OAuthException("Missing 'kid' in header.");
    }
    final String kid = (String) kidObj;

    OidcKey key = keys.get(kid);
    if (key == null && discoveryService != null && jwksUri != null) {
      reloadKeys();
      key = keys.get(kid);
    }

    if (key == null) {
      throw new OAuthException("Key not found for kid: " + kid);
    }

    final Object algObj = jwt.getHeader().get("alg");
    if (!(algObj instanceof String) || ((String) algObj).isEmpty()) {
      throw new OAuthException("Missing 'alg' in header.");
    }
    final String alg = (String) algObj;

    if (!signatureVerifier.verify(
        alg, jwt.getSignedContent(), jwt.getSignature(), key.getPublicKey())) {
      throw new OAuthException("Signature verification failed.");
    }

    signatureCache.put(jwt.getRawToken(), Boolean.TRUE);
  }

  private boolean isIssuerMatching(
      String configuredIssuer, String claimIssuer, Map<String, Object> claims) {
    if (issuerValidator != null) {
      return issuerValidator.isValid(configuredIssuer, claimIssuer, claims);
    }
    return new DefaultIssuerValidator().isValid(configuredIssuer, claimIssuer, claims);
  }

  private synchronized boolean reloadKeys() {
    final long now = System.currentTimeMillis();
    if (now - lastReloadTime < reloadCooldownMs) {
      LOGGER.log(Level.FINE, "JWKS reload skipped due to cooldown.");
      return false;
    }
    if (now - lastReloadFailureTime < reloadFailureCooldownMs) {
      LOGGER.log(Level.FINE, "JWKS reload skipped due to failure cooldown.");
      return false;
    }
    try {
      final Map<String, OidcKey> updatedKeys = discoveryService.getJwks(jwksUri);
      if (updatedKeys != null) {
        keys.putAll(updatedKeys);
        lastReloadTime = System.currentTimeMillis();
        lastReloadFailureTime = 0L;
        return true;
      }
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Failed to reload OIDC provider keys from JWKS URI: " + jwksUri, e);
      lastReloadFailureTime = System.currentTimeMillis();
    }
    return false;
  }

  void setReloadCooldownMs(long reloadCooldownMs) {
    this.reloadCooldownMs = reloadCooldownMs;
  }

  void setReloadFailureCooldownMs(long reloadFailureCooldownMs) {
    this.reloadFailureCooldownMs = reloadFailureCooldownMs;
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
