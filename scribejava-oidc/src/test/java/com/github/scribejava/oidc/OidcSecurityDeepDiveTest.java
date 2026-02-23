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

import com.github.scribejava.core.exceptions.OAuthException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.openid.connect.sdk.Nonce;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests de sécurité approfondis pour OpenID Connect.
 *
 * <p>Se concentre sur la validation rigoureuse des ID Tokens et des Logout Tokens conformément aux
 * spécifications OIDC Core et Back-Channel Logout.
 */
public class OidcSecurityDeepDiveTest {

  private static final String ISSUER = "https://issuer.example.com";
  private static final ClientID CLIENT_ID = new ClientID("client-id");
  private RSAKey rsaKey;
  private JWKSet jwkSet;

  /** Configuration des clés cryptographiques avant chaque test. */
  @BeforeEach
  public void setUp() throws Exception {
    rsaKey = new RSAKeyGenerator(2048).keyID("rsa-1").generate();
    jwkSet = new JWKSet(rsaKey);
  }

  /** Vérifie que les jetons dont la date d'expiration est dépassée sont rejetés. */
  @Test
  public void shouldRejectExpiredToken() throws Exception {
    final JWTClaimsSet claimsSet =
        createBaseClaims()
            .issueTime(new Date())
            .expirationTime(new Date(new Date().getTime() - 3600000)) // 1 hour in the past
            .build();
    final String token = sign(claimsSet);

    final IdTokenValidator validator =
        new IdTokenValidator(ISSUER, CLIENT_ID, JWSAlgorithm.RS256, jwkSet);
    assertThrows(OAuthException.class, () -> validator.validate(token, new Nonce("nonce123"), 0));
  }

  /** Vérifie que les jetons émis dans le futur (au-delà de la marge de tolérance) sont rejetés. */
  @Test
  public void shouldRejectTokenFromFuture() throws Exception {
    final JWTClaimsSet claimsSet =
        createBaseClaims()
            .issueTime(new Date())
            .expirationTime(new Date(System.currentTimeMillis() + 3600000))
            .issueTime(new Date(new Date().getTime() + 100000)) // 100s in the future
            .build();
    final String token = sign(claimsSet);

    final IdTokenValidator validator =
        new IdTokenValidator(ISSUER, CLIENT_ID, JWSAlgorithm.RS256, jwkSet);
    assertThrows(OAuthException.class, () -> validator.validate(token, new Nonce("nonce123"), 0));
  }

  /** Vérifie la validation correcte d'un jeton contenant plusieurs audiences si azp est présent. */
  @Test
  public void shouldHandleMultipleAudiencesWithAzp() throws Exception {
    final JWTClaimsSet claimsSet =
        createBaseClaims()
            .issueTime(new Date())
            .expirationTime(new Date(System.currentTimeMillis() + 3600000))
            .audience(Arrays.asList(CLIENT_ID.getValue(), "other-client"))
            .claim("azp", CLIENT_ID.getValue())
            .build();
    final String token = sign(claimsSet);

    final IdTokenValidator validator =
        new IdTokenValidator(ISSUER, CLIENT_ID, JWSAlgorithm.RS256, jwkSet);
    final IdToken validatedToken = validator.validate(token, new Nonce("nonce123"), 0);
    assertThat(validatedToken).isNotNull();
  }

  /** Vérifie le rejet d'audiences multiples si le champ azp est manquant (requis par OIDC Core). */
  @Test
  public void shouldRejectMultipleAudiencesWithoutAzp() throws Exception {
    // OIDC Core says azp is REQUIRED if aud has multiple values
    final JWTClaimsSet claimsSet =
        createBaseClaims()
            .issueTime(new Date())
            .expirationTime(new Date(System.currentTimeMillis() + 3600000))
            .audience(Arrays.asList(CLIENT_ID.getValue(), "other-client"))
            // azp missing
            .build();
    final String token = sign(claimsSet);

    final IdTokenValidator validator =
        new IdTokenValidator(ISSUER, CLIENT_ID, JWSAlgorithm.RS256, jwkSet);
    assertThrows(OAuthException.class, () -> validator.validate(token, new Nonce("nonce123"), 0));
  }

  /** Vérifie la validation conforme d'un Logout Token (Back-channel logout). */
  @Test
  public void shouldValidateLogoutToken() throws Exception {
    final Map<String, Object> events = new HashMap<>();
    events.put("http://schemas.openid.net/event/backchannel-logout", Collections.emptyMap());

    final JWTClaimsSet claimsSet =
        new JWTClaimsSet.Builder()
            .issuer(ISSUER)
            .audience(CLIENT_ID.getValue())
            .subject("user123")
            .issueTime(new Date())
            .expirationTime(new Date(System.currentTimeMillis() + 3600000))
            .jwtID("logout-123")
            .claim("events", events)
            .claim("sid", "session-123")
            .build();
    final String token = sign(claimsSet);

    final IdTokenValidator validator =
        new IdTokenValidator(ISSUER, CLIENT_ID, JWSAlgorithm.RS256, jwkSet);
    validator.validateLogoutToken(token);
  }

  /** Vérifie qu'un Logout Token contenant un nonce est rejeté (interdit par la spec). */
  @Test
  public void shouldRejectLogoutTokenWithNonce() throws Exception {
    final Map<String, Object> events = new HashMap<>();
    events.put("http://schemas.openid.net/event/backchannel-logout", Collections.emptyMap());

    final JWTClaimsSet claimsSet =
        new JWTClaimsSet.Builder()
            .issuer(ISSUER)
            .audience(CLIENT_ID.getValue())
            .subject("user123")
            .issueTime(new Date())
            .expirationTime(new Date(System.currentTimeMillis() + 3600000))
            .claim("events", events)
            .claim("nonce", "nonce-should-not-be-here")
            .build();
    final String token = sign(claimsSet);

    final IdTokenValidator validator =
        new IdTokenValidator(ISSUER, CLIENT_ID, JWSAlgorithm.RS256, jwkSet);
    assertThrows(OAuthException.class, () -> validator.validateLogoutToken(token));
  }

  /** Vérifie qu'un Logout Token sans l'événement de déconnexion est rejeté. */
  @Test
  public void shouldRejectLogoutTokenWithoutEvents() throws Exception {
    final JWTClaimsSet claimsSet =
        new JWTClaimsSet.Builder()
            .issuer(ISSUER)
            .audience(CLIENT_ID.getValue())
            .subject("user123")
            .issueTime(new Date())
            .expirationTime(new Date(System.currentTimeMillis() + 3600000))
            // Missing events claim
            .build();
    final String token = sign(claimsSet);

    final IdTokenValidator validator =
        new IdTokenValidator(ISSUER, CLIENT_ID, JWSAlgorithm.RS256, jwkSet);
    assertThrows(OAuthException.class, () -> validator.validateLogoutToken(token));
  }

  /**
   * Vérifie le rejet d'un jeton si le type de clé (ex: RSA vs EC) ne correspond pas à l'algorithme.
   */
  @Test
  public void shouldRejectTokenIfKeyTypeMismatch() throws Exception {
    final SignedJWT signedJWT = createSignedJWTWithRsa(rsaKey);
    // We configure the validator with ONLY an EC Key but we send an RSA signed JWT
    final com.nimbusds.jose.jwk.ECKey ecKey =
        new com.nimbusds.jose.jwk.gen.ECKeyGenerator(com.nimbusds.jose.jwk.Curve.P_256)
            .keyID("ec-1")
            .generate();
    final IdTokenValidator validator =
        new IdTokenValidator(ISSUER, CLIENT_ID, JWSAlgorithm.RS256, new JWKSet(ecKey));

    assertThrows(
        OAuthException.class,
        () -> validator.validate(signedJWT.serialize(), new Nonce("nonce123"), 0));
  }

  private SignedJWT createSignedJWTWithRsa(final RSAKey key) throws Exception {
    final SignedJWT signedJWT =
        new SignedJWT(
            new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(key.getKeyID()).build(),
            createBaseClaims().build());
    signedJWT.sign(new RSASSASigner(key));
    return signedJWT;
  }

  private JWTClaimsSet.Builder createBaseClaims() {
    return new JWTClaimsSet.Builder()
        .issuer(ISSUER)
        .audience(CLIENT_ID.getValue())
        .subject("user123")
        .claim("nonce", "nonce123");
  }

  private String sign(final JWTClaimsSet claims) throws Exception {
    final JWSSigner signer = new RSASSASigner(rsaKey);
    final SignedJWT signedJWT =
        new SignedJWT(
            new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.getKeyID()).build(), claims);
    signedJWT.sign(signer);
    return signedJWT.serialize();
  }
}
