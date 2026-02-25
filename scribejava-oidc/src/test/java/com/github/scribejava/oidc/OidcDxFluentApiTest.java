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

import java.util.Optional;
import org.junit.jupiter.api.Test;

/** Tests DX : Fluent API et Optionals pour OIDC. */
public class OidcDxFluentApiTest {

  @Test
  public void shouldAccessClaimsFluently() {
    // Header: {"alg":"none"} -> eyJhbGciOiJub25lIn0
    // Payload: {"sub":"123", "given_name":"John"} -> eyJzdWIiOiIxMjMiLCJnaXZlbl9uYW1lIjoiSm9obiJ9
    final String rawToken = "eyJhbGciOiJub25lIn0.eyJzdWIiOiIxMjMiLCJnaXZlbl9uYW1lIjoiSm9obiJ9.sig";
    final IdToken token = new IdToken(rawToken);

    // Test Point 1 : Fluent Access
    final StandardClaims claims = token.getStandardClaims();
    assertThat(claims.getGivenName()).contains("John");
    assertThat(claims.getFamilyName()).isEmpty(); // Test Point 5 : Optional vide au lieu de null

    // Test Point 5 : Getters de base en Optional
    final Optional<Object> sub = token.getClaimOptional("sub");
    assertThat(sub).contains("123");
    assertThat(token.getClaimOptional("absent")).isEmpty();
  }
}
