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

import com.github.scribejava.oidc.model.OidcKey;
import com.github.scribejava.oidc.model.OidcNonce;
import com.github.scribejava.oidc.model.RsaOidcKey;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests de la matrice de compatibilité cryptographique pour OpenID Connect. */
public class OidcCryptoMatrixTest {

  private static final String ISSUER = "https://issuer.example.com";
  private static final String CLIENT_ID = "client-id";
  private static final String SECURE_NONCE = "nonce1234567890123456";
  private RSAKey rsaKey;
  private ECKey ecKey;
  private Map<String, OidcKey> keys;

  @BeforeEach
  public void setUp() throws Exception {
    rsaKey = new RSAKeyGenerator(2048).keyID("rsa-1").generate();
    ecKey = new ECKeyGenerator(Curve.P_256).keyID("ec-1").generate();
    keys = new HashMap<>();
    keys.put(rsaKey.getKeyID(), new RsaOidcKey(rsaKey.getKeyID(), "RS256", rsaKey.toPublicKey()));
    keys.put(ecKey.getKeyID(), new RsaOidcKey(ecKey.getKeyID(), "ES256", ecKey.toPublicKey()));
  }

  @Test
  public void shouldValidateRS256() throws Exception {
    final SignedJWT signedJWT = createSignedJWT(rsaKey, JWSAlgorithm.RS256);
    final IdTokenValidator validator = new IdTokenValidator(ISSUER, CLIENT_ID, "RS256", keys);
    final IdToken token = validator.validate(signedJWT.serialize(), new OidcNonce(SECURE_NONCE), 0);
    assertThat(token).isNotNull();
  }

  @Test
  @org.junit.jupiter.api.Disabled("ES256 non supporté en natif")
  public void shouldValidateES256() throws Exception {
    final SignedJWT signedJWT = createSignedJWT(ecKey, JWSAlgorithm.ES256);
    final IdTokenValidator validator = new IdTokenValidator(ISSUER, CLIENT_ID, "ES256", keys);
    final IdToken token = validator.validate(signedJWT.serialize(), new OidcNonce(SECURE_NONCE), 0);
    assertThat(token).isNotNull();
  }

  @Test
  @org.junit.jupiter.api.Disabled("PS256 non supporté en natif")
  public void shouldValidatePS256() throws Exception {
    final SignedJWT signedJWT = createSignedJWT(rsaKey, JWSAlgorithm.PS256);
    final IdTokenValidator validator =
        new IdTokenValidator(
            ISSUER, CLIENT_ID, "PS256", OidcTestFixture.convert(new JWKSet(rsaKey)));
    final IdToken token = validator.validate(signedJWT.serialize(), new OidcNonce(SECURE_NONCE), 0);
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
        .audience(CLIENT_ID)
        .subject("user123")
        .expirationTime(new Date(new Date().getTime() + 3600000))
        .issueTime(new Date())
        .claim("nonce", SECURE_NONCE);
  }
}
