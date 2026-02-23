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

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.scribejava.core.exceptions.OAuthException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.id.ClientID;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OidcTokenBindingTest {

  private IdTokenValidator validator;
  private RSAKey rsaJWK;

  @BeforeEach
  public void setUp() throws Exception {
    rsaJWK = new RSAKeyGenerator(2048).keyID("kid-1").generate();
    final JWKSet jwkSet = new JWKSet(rsaJWK.toPublicJWK());
    validator =
        new IdTokenValidator(
            "https://idp.com", new ClientID("client-1"), JWSAlgorithm.RS256, jwkSet);
  }

  private IdToken createSignedIdToken(final Map<String, Object> extraClaims) throws Exception {
    final JWTClaimsSet.Builder builder =
        new JWTClaimsSet.Builder()
            .subject("user1")
            .issuer("https://idp.com")
            .audience("client-1")
            .expirationTime(new Date(System.currentTimeMillis() + 10000))
            .issueTime(new Date());

    extraClaims.forEach(builder::claim);

    final SignedJWT signedJWT =
        new SignedJWT(
            new JWSHeader.Builder(JWSAlgorithm.RS256).keyID("kid-1").build(), builder.build());
    signedJWT.sign(new RSASSASigner(rsaJWK));
    return new IdToken(signedJWT.serialize());
  }

  @Test
  public void shouldValidateJktBinding() throws Exception {
    final Map<String, String> cnf = new HashMap<>();
    cnf.put("jkt", "expected-jkt-value");

    final Map<String, Object> claims = new HashMap<>();
    claims.put("cnf", cnf);

    final IdToken idToken = createSignedIdToken(claims);
    validator.validateTokenBinding(idToken, "expected-jkt-value", null);
  }

  @Test
  public void shouldRejectJktMismatch() throws Exception {
    final Map<String, String> cnf = new HashMap<>();
    cnf.put("jkt", "wrong-jkt-value");

    final Map<String, Object> claims = new HashMap<>();
    claims.put("cnf", cnf);

    final IdToken idToken = createSignedIdToken(claims);
    assertThrows(
        OAuthException.class,
        () -> validator.validateTokenBinding(idToken, "expected-jkt-value", null));
  }

  @Test
  public void shouldValidateX5tBinding() throws Exception {
    final Map<String, String> cnf = new HashMap<>();
    cnf.put("x5t#S256", "expected-x5t-value");

    final Map<String, Object> claims = new HashMap<>();
    claims.put("cnf", cnf);

    final IdToken idToken = createSignedIdToken(claims);
    validator.validateTokenBinding(idToken, null, "expected-x5t-value");
  }

  @Test
  public void shouldRejectX5tMismatch() throws Exception {
    final Map<String, String> cnf = new HashMap<>();
    cnf.put("x5t#S256", "wrong-x5t-value");

    final Map<String, Object> claims = new HashMap<>();
    claims.put("cnf", cnf);

    final IdToken idToken = createSignedIdToken(claims);
    assertThrows(
        OAuthException.class,
        () -> validator.validateTokenBinding(idToken, null, "expected-x5t-value"));
  }
}
