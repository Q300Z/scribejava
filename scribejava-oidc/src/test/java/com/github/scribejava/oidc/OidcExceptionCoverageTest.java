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
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.id.ClientID;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OidcExceptionCoverageTest {

  private IdTokenValidator validator;

  @BeforeEach
  public void setUp() {
    validator =
        new IdTokenValidator(
            "https://idp.com", new ClientID("client-1"), JWSAlgorithm.RS256, new JWKSet());
  }

  @Test
  public void shouldHandleInvalidJwtFormatInValidate() {
    assertThrows(OAuthException.class, () -> validator.validate("part1.part2", null, 0));
  }

  @Test
  public void shouldHandleECDecryptionError() throws Exception {
    final ECKey ecKey = new ECKeyGenerator(Curve.P_256).keyID("kid-ec").generate();
    final IdTokenValidator ecValidator =
        new IdTokenValidator(
            "iss", new ClientID("c"), JWSAlgorithm.ES256, new JWKSet(), null, ecKey);
    // Not a real JWE
    assertThrows(OAuthException.class, () -> ecValidator.validate("p1.p2.p3.p4.p5", null, 0));
  }

  @Test
  public void shouldHandleSignatureVerificationFailure() throws Exception {
    final ECKey ecKey = new ECKeyGenerator(Curve.P_256).keyID("kid-1").generate();
    final JWKSet jwkSet = new JWKSet(ecKey.toPublicJWK());
    final IdTokenValidator sigValidator =
        new IdTokenValidator(
            "https://idp.com", new ClientID("client-1"), JWSAlgorithm.ES256, jwkSet);

    final JWTClaimsSet claims =
        new JWTClaimsSet.Builder()
            .subject("user")
            .issuer("https://idp.com")
            .audience("client-1")
            .expirationTime(new Date(System.currentTimeMillis() + 10000))
            .build();

    final SignedJWT signedJWT =
        new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.ES256).keyID("kid-1").build(), claims);
    signedJWT.sign(new ECDSASigner(ecKey));

    // Tamper with the signature string to cause verification failure
    final String raw = signedJWT.serialize();
    final String tampered = raw.substring(0, raw.lastIndexOf('.') + 1) + "dmVyeS1iYWQtc2lnbmF0dXJl";
    assertThrows(OAuthException.class, () -> sigValidator.validate(tampered, null, 0));
  }
}
