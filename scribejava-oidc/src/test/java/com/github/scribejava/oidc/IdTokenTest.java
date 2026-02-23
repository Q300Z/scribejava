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

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.util.Date;
import org.junit.jupiter.api.Test;

public class IdTokenTest {

  private static final int KEY_SIZE = 2048;
  private static final int EXPIRATION_MS = 60 * 1000;

  @Test
  public void shouldParseIdToken() throws Exception {
    // Generate RSA key for signing
    final RSAKey rsaJWK = new RSAKeyGenerator(KEY_SIZE).keyID("123").generate();
    final JWSSigner signer = new RSASSASigner(rsaJWK);

    // Prepare JWT with claims
    final JWTClaimsSet claimsSet =
        new JWTClaimsSet.Builder()
            .subject("alice")
            .issuer("https://c2id.com")
            .audience("123")
            .expirationTime(new Date(new Date().getTime() + EXPIRATION_MS))
            .issueTime(new Date())
            .claim("name", "Alice Doe")
            .claim("email", "alice@doe.com")
            .claim("email_verified", true)
            .claim("nonce", "xyz")
            .build();

    final SignedJWT signedJWT =
        new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID("123").build(), claimsSet);
    signedJWT.sign(signer);

    final String rawToken = signedJWT.serialize();

    // Test IdToken
    final IdToken idToken = new IdToken(rawToken);

    assertThat(idToken.getIssuer()).isEqualTo("https://c2id.com");
    assertThat(idToken.getSubject()).isEqualTo("alice");
    assertThat(idToken.getNonce()).isEqualTo("xyz");
    assertThat(idToken.getStandardClaims().getName()).contains("Alice Doe");
    assertThat(idToken.getStandardClaims().getEmail()).contains("alice@doe.com");
    assertThat(idToken.getStandardClaims().isEmailVerified()).contains(true);
  }
}
