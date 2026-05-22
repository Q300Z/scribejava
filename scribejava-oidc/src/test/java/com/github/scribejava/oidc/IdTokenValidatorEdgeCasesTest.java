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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.oidc.model.OidcKey;
import com.github.scribejava.oidc.model.OidcNonce;
import com.github.scribejava.oidc.model.RsaOidcKey;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class IdTokenValidatorEdgeCasesTest {

  private static final String ISSUER = "https://issuer.example.com";
  private static final String CLIENT_ID = "client-id";
  private static final String SECURE_NONCE = "nonce1234567890123456";
  private RSAKey rsaKey;
  private Map<String, OidcKey> keys;

  @BeforeEach
  public void setUp() throws Exception {
    rsaKey = new RSAKeyGenerator(2048).keyID("rsa-1").generate();
    keys = new HashMap<>();
    keys.put(rsaKey.getKeyID(), new RsaOidcKey(rsaKey.getKeyID(), "RS256", rsaKey.toPublicKey()));
  }

  private String sign(final JWTClaimsSet claims) throws Exception {
    return sign(claims, rsaKey.getKeyID(), rsaKey);
  }

  private String sign(final JWTClaimsSet claims, String kid, RSAKey signingKey) throws Exception {
    final JWSSigner signer = new RSASSASigner(signingKey);
    final SignedJWT signedJWT =
        new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(kid).build(), claims);
    signedJWT.sign(signer);
    return signedJWT.serialize();
  }

  private JWTClaimsSet.Builder createBaseClaims() {
    return new JWTClaimsSet.Builder()
        .issuer(ISSUER)
        .audience(CLIENT_ID)
        .subject("user123")
        .claim("nonce", SECURE_NONCE)
        .issueTime(new Date())
        .expirationTime(new Date(System.currentTimeMillis() + 3600000));
  }

  @Test
  public void shouldRejectInvalidAlgorithm() throws Exception {
    final JWTClaimsSet claimsSet = createBaseClaims().build();
    final String token = sign(claimsSet);

    // Validator expects ES256, but token is RS256
    final IdTokenValidator validator = new IdTokenValidator(ISSUER, CLIENT_ID, "ES256", keys);
    final OAuthException exception =
        assertThrows(
            OAuthException.class, () -> validator.validate(token, new OidcNonce(SECURE_NONCE), 0));
    assertThat(exception.getMessage()).contains("Invalid algorithm");
  }

  @Test
  public void shouldRejectIssuerMismatch() throws Exception {
    final JWTClaimsSet claimsSet = createBaseClaims().issuer("https://wrong-issuer.com").build();
    final String token = sign(claimsSet);

    final IdTokenValidator validator = new IdTokenValidator(ISSUER, CLIENT_ID, "RS256", keys);
    final OAuthException exception =
        assertThrows(
            OAuthException.class, () -> validator.validate(token, new OidcNonce(SECURE_NONCE), 0));
    assertThat(exception.getMessage()).contains("Issuer mismatch");
  }

  @Test
  public void shouldRejectAudienceMismatchString() throws Exception {
    final JWTClaimsSet claimsSet = createBaseClaims().audience("wrong-client").build();
    final String token = sign(claimsSet);

    final IdTokenValidator validator = new IdTokenValidator(ISSUER, CLIENT_ID, "RS256", keys);
    final OAuthException exception =
        assertThrows(
            OAuthException.class, () -> validator.validate(token, new OidcNonce(SECURE_NONCE), 0));
    assertThat(exception.getMessage()).contains("Audience mismatch");
  }

  @Test
  public void shouldRejectAudienceMismatchList() throws Exception {
    final JWTClaimsSet claimsSet =
        createBaseClaims().audience(Arrays.asList("wrong-1", "wrong-2")).build();
    final String token = sign(claimsSet);

    final IdTokenValidator validator = new IdTokenValidator(ISSUER, CLIENT_ID, "RS256", keys);
    final OAuthException exception =
        assertThrows(
            OAuthException.class, () -> validator.validate(token, new OidcNonce(SECURE_NONCE), 0));
    assertThat(exception.getMessage()).contains("Audience mismatch");
  }

  @Test
  public void shouldRejectMultipleAudiencesWithoutAzp() throws Exception {
    final JWTClaimsSet claimsSet =
        createBaseClaims().audience(Arrays.asList(CLIENT_ID, "other-client")).build();
    final String token = sign(claimsSet);

    final IdTokenValidator validator = new IdTokenValidator(ISSUER, CLIENT_ID, "RS256", keys);
    final OAuthException exception =
        assertThrows(
            OAuthException.class, () -> validator.validate(token, new OidcNonce(SECURE_NONCE), 0));
    assertThat(exception.getMessage()).contains("azp' claim is missing");
  }

  @Test
  public void shouldRejectExpiredToken() throws Exception {
    final JWTClaimsSet claimsSet =
        createBaseClaims().expirationTime(new Date(System.currentTimeMillis() - 3600000)).build();
    final String token = sign(claimsSet);

    final IdTokenValidator validator = new IdTokenValidator(ISSUER, CLIENT_ID, "RS256", keys);
    final OAuthException exception =
        assertThrows(
            OAuthException.class, () -> validator.validate(token, new OidcNonce(SECURE_NONCE), 0));
    assertThat(exception.getMessage()).contains("Token has expired");
  }

  @Test
  public void shouldRejectTokenWithoutExpiration() throws Exception {
    final JWTClaimsSet claimsSet =
        new JWTClaimsSet.Builder()
            .issuer(ISSUER)
            .audience(CLIENT_ID)
            .subject("user123")
            .claim("nonce", SECURE_NONCE)
            .issueTime(new Date())
            // no exp claim
            .build();
    final String token = sign(claimsSet);

    final IdTokenValidator validator = new IdTokenValidator(ISSUER, CLIENT_ID, "RS256", keys);
    final OAuthException exception =
        assertThrows(
            OAuthException.class, () -> validator.validate(token, new OidcNonce(SECURE_NONCE), 0));
    assertThat(exception.getMessage()).contains("Token has expired");
  }

  @Test
  public void shouldRejectMaxAuthAgeWhenNoAuthTime() throws Exception {
    final JWTClaimsSet claimsSet = createBaseClaims().build(); // no auth_time claim
    final String token = sign(claimsSet);

    final IdTokenValidator validator = new IdTokenValidator(ISSUER, CLIENT_ID, "RS256", keys);
    final OAuthException exception =
        assertThrows(
            OAuthException.class,
            () -> validator.validate(token, new OidcNonce(SECURE_NONCE), 100));
    assertThat(exception.getMessage()).contains("does not contain 'auth_time' claim");
  }

  @Test
  public void shouldRejectMaxAuthAgeWhenAuthTimeTooOld() throws Exception {
    final long nowSec = System.currentTimeMillis() / 1000;
    final JWTClaimsSet claimsSet = createBaseClaims().claim("auth_time", nowSec - 200).build();
    final String token = sign(claimsSet);

    final IdTokenValidator validator = new IdTokenValidator(ISSUER, CLIENT_ID, "RS256", keys);
    final OAuthException exception =
        assertThrows(
            OAuthException.class,
            () -> validator.validate(token, new OidcNonce(SECURE_NONCE), 100));
    assertThat(exception.getMessage()).contains("Auth time too old");
  }

  @Test
  public void shouldRejectMissingKid() throws Exception {
    final JWTClaimsSet claimsSet = createBaseClaims().build();
    final JWSSigner signer = new RSASSASigner(rsaKey);
    final SignedJWT signedJWT =
        new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).build(), claimsSet); // no kid
    signedJWT.sign(signer);
    final String token = signedJWT.serialize();

    final IdTokenValidator validator = new IdTokenValidator(ISSUER, CLIENT_ID, "RS256", keys);
    final OAuthException exception =
        assertThrows(
            OAuthException.class, () -> validator.validate(token, new OidcNonce(SECURE_NONCE), 0));
    assertThat(exception.getMessage()).contains("Missing 'kid' in header");
  }

  @Test
  public void shouldRejectUnknownKid() throws Exception {
    final JWTClaimsSet claimsSet = createBaseClaims().build();
    final String token = sign(claimsSet, "unknown-kid", rsaKey);

    final IdTokenValidator validator = new IdTokenValidator(ISSUER, CLIENT_ID, "RS256", keys);
    final OAuthException exception =
        assertThrows(
            OAuthException.class, () -> validator.validate(token, new OidcNonce(SECURE_NONCE), 0));
    assertThat(exception.getMessage()).contains("Key not found for kid");
  }

  @Test
  public void shouldAutomaticRotationOnUnknownKidSuccess() throws Exception {
    final JWTClaimsSet claimsSet = createBaseClaims().build();
    final String token = sign(claimsSet, "rotated-kid", rsaKey);

    final OidcDiscoveryService discoveryService = mock(OidcDiscoveryService.class);
    final Map<String, OidcKey> updatedKeys = new HashMap<>();
    updatedKeys.put("rotated-kid", new RsaOidcKey("rotated-kid", "RS256", rsaKey.toPublicKey()));

    when(discoveryService.getJwks("https://jwks-uri")).thenReturn(updatedKeys);

    // Initialize with empty keys
    final IdTokenValidator validator =
        new IdTokenValidator(
            ISSUER, CLIENT_ID, "RS256", new HashMap<>(), discoveryService, "https://jwks-uri");

    final IdToken idToken = validator.validate(token, new OidcNonce(SECURE_NONCE), 0);
    assertThat(idToken).isNotNull();
  }

  @Test
  public void shouldSilentFailOnAutomaticRotationException() throws Exception {
    final JWTClaimsSet claimsSet = createBaseClaims().build();
    final String token = sign(claimsSet, "rotated-kid", rsaKey);

    final OidcDiscoveryService discoveryService = mock(OidcDiscoveryService.class);
    when(discoveryService.getJwks("https://jwks-uri"))
        .thenThrow(new RuntimeException("Network Error"));

    final IdTokenValidator validator =
        new IdTokenValidator(
            ISSUER, CLIENT_ID, "RS256", new HashMap<>(), discoveryService, "https://jwks-uri");

    final OAuthException exception =
        assertThrows(
            OAuthException.class, () -> validator.validate(token, new OidcNonce(SECURE_NONCE), 0));
    assertThat(exception.getMessage()).contains("Key not found for kid");
  }

  @Test
  public void shouldRejectSignatureVerificationFailure() throws Exception {
    final JWTClaimsSet claimsSet = createBaseClaims().build();
    final RSAKey wrongKey = new RSAKeyGenerator(2048).keyID("rsa-1").generate();
    final String token =
        sign(claimsSet, "rsa-1", wrongKey); // Signed with wrongKey but kid is "rsa-1"

    final IdTokenValidator validator = new IdTokenValidator(ISSUER, CLIENT_ID, "RS256", keys);
    final OAuthException exception =
        assertThrows(
            OAuthException.class, () -> validator.validate(token, new OidcNonce(SECURE_NONCE), 0));
    assertThat(exception.getMessage()).contains("Signature verification failed");
  }

  @Test
  public void shouldValidateLogoutToken() throws Exception {
    final JWTClaimsSet claimsSet = new JWTClaimsSet.Builder().issuer(ISSUER).build();
    final String token = sign(claimsSet);

    final IdTokenValidator validator = new IdTokenValidator(ISSUER, CLIENT_ID, "RS256", keys);
    validator.validateLogoutToken(token); // Should pass without exception
  }

  @Test
  public void shouldValidateTokenBindingCnfNotMap() throws Exception {
    final JWTClaimsSet claimsSet = createBaseClaims().claim("cnf", "not-a-map").build();
    final String token = sign(claimsSet);

    final IdTokenValidator validator = new IdTokenValidator(ISSUER, CLIENT_ID, "RS256", keys);
    final IdToken idToken = validator.validate(token, new OidcNonce(SECURE_NONCE), 0);

    // Should return immediately because cnf is not a Map
    validator.validateTokenBinding(idToken, "expected-jkt", "expected-x5t");
  }

  @Test
  public void shouldRejectTokenBindingJktMismatch() throws Exception {
    final Map<String, Object> cnf = new HashMap<>();
    cnf.put("jkt", "wrong-jkt");
    final JWTClaimsSet claimsSet = createBaseClaims().claim("cnf", cnf).build();
    final String token = sign(claimsSet);

    final IdTokenValidator validator = new IdTokenValidator(ISSUER, CLIENT_ID, "RS256", keys);
    final IdToken idToken = validator.validate(token, new OidcNonce(SECURE_NONCE), 0);

    final OAuthException exception =
        assertThrows(
            OAuthException.class,
            () -> validator.validateTokenBinding(idToken, "expected-jkt", null));
    assertThat(exception.getMessage()).contains("DPoP proof key mismatch");
  }

  @Test
  public void shouldRejectTokenBindingX5tMismatch() throws Exception {
    final Map<String, Object> cnf = new HashMap<>();
    cnf.put("x5t#S256", "wrong-x5t");
    final JWTClaimsSet claimsSet = createBaseClaims().claim("cnf", cnf).build();
    final String token = sign(claimsSet);

    final IdTokenValidator validator = new IdTokenValidator(ISSUER, CLIENT_ID, "RS256", keys);
    final IdToken idToken = validator.validate(token, new OidcNonce(SECURE_NONCE), 0);

    final OAuthException exception =
        assertThrows(
            OAuthException.class,
            () -> validator.validateTokenBinding(idToken, null, "expected-x5t"));
    assertThat(exception.getMessage()).contains("mTLS certificate mismatch");
  }

  @Test
  public void shouldValidateTokenBindingSuccess() throws Exception {
    final Map<String, Object> cnf = new HashMap<>();
    cnf.put("jkt", "expected-jkt");
    cnf.put("x5t#S256", "expected-x5t");
    final JWTClaimsSet claimsSet = createBaseClaims().claim("cnf", cnf).build();
    final String token = sign(claimsSet);

    final IdTokenValidator validator = new IdTokenValidator(ISSUER, CLIENT_ID, "RS256", keys);
    final IdToken idToken = validator.validate(token, new OidcNonce(SECURE_NONCE), 0);

    validator.validateTokenBinding(idToken, "expected-jkt", "expected-x5t"); // should pass
  }
}
