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

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.github.scribejava.core.exceptions.OAuthException;
import org.junit.jupiter.api.Test;

/** Tests d'échec de parsing JWT (TDD). */
public class JwtParsingFailureTest {

  @Test
  public void shouldFailOnInvalidSegmentsCount() {
    assertThatExceptionOfType(OAuthException.class)
        .isThrownBy(() -> Jwt.parse("one.two"))
        .withMessageContaining("Invalid JWT");
  }

  @Test
  public void shouldFailOnInvalidBase64() {
    assertThatExceptionOfType(OAuthException.class)
        .isThrownBy(() -> Jwt.parse("invalid!base64.payload.signature"));
  }

  @Test
  public void shouldFailOnInvalidJsonInside() {
    // Header valide (ey...) mais payload corrompu
    assertThatExceptionOfType(OAuthException.class)
        .isThrownBy(() -> Jwt.parse("eyJhbGciOiJSUzI1NiJ9.bm90LWpzb24.signature"));
  }
}
