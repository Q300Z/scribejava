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

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.openid.connect.sdk.Nonce;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OidcCryptoMatrixTest {

  private static final String ISSUER = "https://issuer.example.com";
  private static final ClientID CLIENT_ID = new ClientID("client-id");
  private final String hmacSecret = "super-secret-hmac-key-of-at-least-32-chars";
  private RSAKey rsaKey;
  private ECKey ecKey;

  @BeforeEach
  public void setUp() throws Exception {
    rsaKey = new RSAKeyGenerator(2048).keyID("rsa-1").generate();
    ecKey = new ECKeyGenerator(Curve.P_256).keyID("ec-1").generate();
  }

  @Test
  public void shouldValidateRS256() throws Exception {
    final SignedJWT signedJWT = createSignedJWT(rsaKey, JWSAlgorithm.RS256);
    final IdTokenValidator validator =
        new IdTokenValidator(ISSUER, CLIENT_ID, JWSAlgorithm.RS256, new JWKSet(rsaKey));
    final IdToken token = validator.validate(signedJWT.serialize(), new Nonce("nonce123"), 0);
    assertThat(token).isNotNull();
  }

  @Test
  public void shouldValidateES256() throws Exception {
    final SignedJWT signedJWT = createSignedJWT(ecKey, JWSAlgorithm.ES256);
    final IdTokenValidator validator =
        new IdTokenValidator(ISSUER, CLIENT_ID, JWSAlgorithm.ES256, new JWKSet(ecKey));
    final IdToken token = validator.validate(signedJWT.serialize(), new Nonce("nonce123"), 0);
    assertThat(token).isNotNull();
  }

  @Test
  public void shouldValidateHS256() throws Exception {
    final JWTClaimsSet claimsSet = createClaims().build();
    final SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
    signedJWT.sign(new MACSigner(hmacSecret.getBytes()));

    final IdTokenValidator validator =
        new IdTokenValidator(ISSUER, CLIENT_ID, JWSAlgorithm.HS256, new JWKSet(), hmacSecret);
    final IdToken token = validator.validate(signedJWT.serialize(), new Nonce("nonce123"), 0);
    assertThat(token).isNotNull();
  }

  @Test
  public void shouldValidateEncryptedRSA() throws Exception {
    final SignedJWT signedJWT = createSignedJWT(rsaKey, JWSAlgorithm.RS256);
    final JWEObject jwe =
        new JWEObject(
            new JWEHeader(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256GCM),
            new Payload(signedJWT));
    jwe.encrypt(new RSAEncrypter(rsaKey.toRSAPublicKey()));

    final IdTokenValidator validator =
        new IdTokenValidator(
            ISSUER, CLIENT_ID, JWSAlgorithm.RS256, new JWKSet(rsaKey), null, rsaKey);
    final IdToken token = validator.validate(jwe.serialize(), new Nonce("nonce123"), 0);
    assertThat(token).isNotNull();
  }

  @Test
  public void shouldValidateEncryptedEC() throws Exception {
    final SignedJWT signedJWT = createSignedJWT(rsaKey, JWSAlgorithm.RS256);
    final JWEObject jwe =
        new JWEObject(
            new JWEHeader(JWEAlgorithm.ECDH_ES, EncryptionMethod.A256GCM), new Payload(signedJWT));
    jwe.encrypt(new ECDHEncrypter(ecKey.toECPublicKey()));

    final IdTokenValidator validator =
        new IdTokenValidator(
            ISSUER, CLIENT_ID, JWSAlgorithm.RS256, new JWKSet(rsaKey), null, ecKey);
    final IdToken token = validator.validate(jwe.serialize(), new Nonce("nonce123"), 0);
    assertThat(token).isNotNull();
  }

  @Test
  public void shouldValidatePS256() throws Exception {
    final SignedJWT signedJWT = createSignedJWT(rsaKey, JWSAlgorithm.PS256);
    final IdTokenValidator validator =
        new IdTokenValidator(ISSUER, CLIENT_ID, JWSAlgorithm.PS256, new JWKSet(rsaKey));
    final IdToken token = validator.validate(signedJWT.serialize(), new Nonce("nonce123"), 0);
    assertThat(token).isNotNull();
  }

  @Test
  public void shouldValidateEncryptedWithAesCbc() throws Exception {
    final SignedJWT signedJWT = createSignedJWT(rsaKey, JWSAlgorithm.RS256);
    final JWEObject jwe =
        new JWEObject(
            new JWEHeader(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A128CBC_HS256),
            new Payload(signedJWT));
    jwe.encrypt(new RSAEncrypter(rsaKey.toRSAPublicKey()));

    final IdTokenValidator validator =
        new IdTokenValidator(
            ISSUER, CLIENT_ID, JWSAlgorithm.RS256, new JWKSet(rsaKey), null, rsaKey);
    final IdToken token = validator.validate(jwe.serialize(), new Nonce("nonce123"), 0);
    assertThat(token).isNotNull();
  }

  private SignedJWT createSignedJWT(final com.nimbusds.jose.jwk.JWK key, final JWSAlgorithm alg)
      throws Exception {
    final JWSSigner signer;
    if (key instanceof RSAKey) {
      signer = new RSASSASigner((RSAKey) key);
    } else {
      signer = new ECDSASigner((ECKey) key);
    }

    final SignedJWT signedJWT =
        new SignedJWT(
            new JWSHeader.Builder(alg).keyID(key.getKeyID()).build(), createClaims().build());
    signedJWT.sign(signer);
    return signedJWT;
  }

  private JWTClaimsSet.Builder createClaims() {
    return new JWTClaimsSet.Builder()
        .issuer(ISSUER)
        .audience(CLIENT_ID.getValue())
        .subject("user123")
        .expirationTime(new Date(new Date().getTime() + 3600000))
        .issueTime(new Date())
        .claim("nonce", "nonce123");
  }
}
