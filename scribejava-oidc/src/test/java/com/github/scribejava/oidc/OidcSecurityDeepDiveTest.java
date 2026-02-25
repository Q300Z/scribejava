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

/** Tests de sécurité approfondis utilisant le validateur natif ScribeJava. */
public class OidcSecurityDeepDiveTest {

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

  @Test
  public void shouldRejectExpiredToken() throws Exception {
    final JWTClaimsSet claimsSet =
        createBaseClaims()
            .issueTime(new Date())
            .expirationTime(new Date(new Date().getTime() - 3600000))
            .build();
    final String token = sign(claimsSet);

    final IdTokenValidator validator = new IdTokenValidator(ISSUER, CLIENT_ID, "RS256", keys);
    assertThrows(
        OAuthException.class, () -> validator.validate(token, new OidcNonce(SECURE_NONCE), 0));
  }

  @Test
  public void shouldHandleMultipleAudiencesWithAzp() throws Exception {
    final JWTClaimsSet claimsSet =
        createBaseClaims()
            .issueTime(new Date())
            .expirationTime(new Date(System.currentTimeMillis() + 3600000))
            .audience(Arrays.asList(CLIENT_ID, "other-client"))
            .claim("azp", CLIENT_ID)
            .build();
    final String token = sign(claimsSet);

    final IdTokenValidator validator = new IdTokenValidator(ISSUER, CLIENT_ID, "RS256", keys);
    final IdToken validatedToken = validator.validate(token, new OidcNonce(SECURE_NONCE), 0);
    assertThat(validatedToken).isNotNull();
  }

  @Test
  public void shouldRejectMultipleAudiencesWithoutAzp() throws Exception {
    final JWTClaimsSet claimsSet =
        createBaseClaims()
            .issueTime(new Date())
            .expirationTime(new Date(System.currentTimeMillis() + 3600000))
            .audience(Arrays.asList(CLIENT_ID, "other-client"))
            .build();
    final String token = sign(claimsSet);

    final IdTokenValidator validator = new IdTokenValidator(ISSUER, CLIENT_ID, "RS256", keys);
    assertThrows(
        OAuthException.class, () -> validator.validate(token, new OidcNonce(SECURE_NONCE), 0));
  }

  private String sign(final JWTClaimsSet claims) throws Exception {
    final JWSSigner signer = new RSASSASigner(rsaKey);
    final SignedJWT signedJWT =
        new SignedJWT(
            new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.getKeyID()).build(), claims);
    signedJWT.sign(signer);
    return signedJWT.serialize();
  }

  private JWTClaimsSet.Builder createBaseClaims() {
    return new JWTClaimsSet.Builder()
        .issuer(ISSUER)
        .audience(CLIENT_ID)
        .subject("user123")
        .claim("nonce", SECURE_NONCE);
  }
}
