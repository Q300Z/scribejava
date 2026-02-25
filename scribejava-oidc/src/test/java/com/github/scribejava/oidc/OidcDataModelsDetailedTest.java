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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Tests de boilerplate pour les modèles OIDC. */
public class OidcDataModelsDetailedTest {

  /** Vérifie le boilerplate de StandardClaims. */
  @Test
  public void shouldTestStandardClaimsBoilerplate() {
    final Map<String, Object> claimsMap = new HashMap<>();
    claimsMap.put("sub", "123");
    claimsMap.put("name", "John");

    final StandardClaims claims1 = new StandardClaims(claimsMap);
    final StandardClaims claims2 = new StandardClaims(new HashMap<>(claimsMap));
    final StandardClaims claims3 = new StandardClaims(Collections.singletonMap("sub", "456"));

    assertThat(claims1).isEqualTo(claims2);
    assertThat(claims1.hashCode()).isEqualTo(claims2.hashCode());
    assertThat(claims1).isNotEqualTo(claims3);
    assertThat(claims1.toString()).contains("sub=123");
  }

  /** Vérifie le boilerplate de IdToken. */
  @Test
  public void shouldTestIdTokenBoilerplate() {
    // Header: {"alg":"none"} -> eyJhbGciOiJub25lIn0
    // Payload: {"sub":"123"} -> eyJzdWIiOiIxMjMifQ
    final String rawToken = "eyJhbGciOiJub25lIn0.eyJzdWIiOiIxMjMifQ.sig";
    final IdToken token1 = new IdToken(rawToken);
    final IdToken token2 = new IdToken(rawToken);

    assertThat(token1).isEqualTo(token2);
    assertThat(token1.hashCode()).isEqualTo(token2.hashCode());
    assertThat(token1.toString()).contains("rawIdToken='" + rawToken + "'");
    assertThat(token1.getClaim("sub")).isEqualTo("123");
  }
}
