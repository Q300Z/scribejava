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
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class OidcBoilerplateTest {

  @Test
  public void testStandardClaimsBoilerplate() {
    final Map<String, Object> map = new HashMap<>();
    map.put("sub", "123");
    map.put("name", "John");
    map.put("email_verified", true);

    final StandardClaims claims = new StandardClaims(map);
    assertThat(claims.getSub()).contains("123");
    assertThat(claims.getName()).contains("John");
    assertThat(claims.isEmailVerified()).contains(true);

    assertThat(claims.getGivenName()).isEmpty();
    assertThat(claims.getFamilyName()).isEmpty();
    assertThat(claims.getMiddleName()).isEmpty();
    assertThat(claims.getNickname()).isEmpty();
    assertThat(claims.getPreferredUsername()).isEmpty();
    assertThat(claims.getProfile()).isEmpty();
    assertThat(claims.getPicture()).isEmpty();
    assertThat(claims.getWebsite()).isEmpty();
    assertThat(claims.getEmail()).isEmpty();
    assertThat(claims.getGender()).isEmpty();
    assertThat(claims.getBirthdate()).isEmpty();
    assertThat(claims.getZoneinfo()).isEmpty();
    assertThat(claims.getLocale()).isEmpty();
    assertThat(claims.getPhoneNumber()).isEmpty();
    assertThat(claims.isPhoneNumberVerified()).isEmpty();
    assertThat(claims.getAddress()).isEmpty();
    assertThat(claims.getUpdatedAt()).isEmpty();
    assertThat(claims.getAllClaims()).isNotNull();
  }

  @Test
  public void testIdTokenBoilerplate() throws Exception {
    final JWTClaimsSet claimsSet =
        new JWTClaimsSet.Builder()
            .subject("123")
            .issuer("https://idp.com")
            .claim("nonce", "nonce123")
            .build();
    final SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
    signedJWT.sign(new MACSigner("secretsecretsecretsecretsecretsecretsecretsecret"));
    final String rawToken = signedJWT.serialize();

    final IdToken token = new IdToken(rawToken);
    assertThat(token.getRawResponse()).isEqualTo(rawToken);
    assertThat(token.getSubject()).isEqualTo("123");
    assertThat(token.getIssuer()).isEqualTo("https://idp.com");
    assertThat(token.getNonce()).isEqualTo("nonce123");
    assertThat(token.getClaimsSet()).isNotNull();
    assertThat(token.getStandardClaims().getSub()).contains("123");
    assertThat(token.getClaim("sub")).isEqualTo("123");
    assertThat(token.getClaims()).isNotNull();
  }
}
