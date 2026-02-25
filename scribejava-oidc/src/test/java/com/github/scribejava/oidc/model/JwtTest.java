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
package com.github.scribejava.oidc.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class JwtTest {

  // Token fictif : header.payload.signature
  // Header: {"alg":"RS256","typ":"JWT"}
  // Payload: {"sub":"1234567890","name":"John Doe","iat":1516239022}
  private static final String VALID_TOKEN =
      "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9."
          + "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ."
          + "SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

  @Test
  void shouldParseValidToken() {
    Jwt jwt = Jwt.parse(VALID_TOKEN);

    assertThat(jwt.getHeader().get("alg")).isEqualTo("RS256");
    assertThat(jwt.getPayload().get("sub")).isEqualTo("1234567890");
    assertThat(jwt.getSignature()).isNotNull();
  }

  @Test
  void shouldRejectMalformedToken() {
    assertThatThrownBy(() -> Jwt.parse("not.a.jwt")).isInstanceOf(IllegalArgumentException.class);

    assertThatThrownBy(() -> Jwt.parse("one.two.three.four"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldExtractRawDataForSignatureVerification() {
    Jwt jwt = Jwt.parse(VALID_TOKEN);

    // La signature se vérifie sur "header.payload"
    String expectedSignedData = VALID_TOKEN.substring(0, VALID_TOKEN.lastIndexOf('.'));
    assertThat(jwt.getSignedContent()).isEqualTo(expectedSignedData);
  }
}
