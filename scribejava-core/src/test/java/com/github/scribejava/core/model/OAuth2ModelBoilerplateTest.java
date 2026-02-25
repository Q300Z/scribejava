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
package com.github.scribejava.core.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;

/** Tests de boilerplate pour les modèles OAuth 2.0. */
public class OAuth2ModelBoilerplateTest {

  /** Vérifie le boilerplate exhaustif de OAuth2AccessToken. */
  @Test
  public void shouldTestOAuth2AccessTokenBoilerplate() {
    final OAuth2AccessToken token1 =
        new OAuth2AccessToken("at", "bearer", 3600, "rt", "scope", "raw");
    final OAuth2AccessToken token2 =
        new OAuth2AccessToken("at", "bearer", 3600, "rt", "scope", "raw");
    final OAuth2AccessToken token3 = new OAuth2AccessToken("at_diff");

    assertThat(token1.getAccessToken()).isEqualTo("at");
    assertThat(token1.getTokenType()).isEqualTo("bearer");
    assertThat(token1.getExpiresIn()).isEqualTo(3600);
    assertThat(token1.getRefreshToken()).isEqualTo("rt");
    assertThat(token1.getScope()).isEqualTo("scope");
    assertThat(token1.getRawResponse()).isEqualTo("raw");

    assertThat(token1).isEqualTo(token2);
    assertThat(token1.hashCode()).isEqualTo(token2.hashCode());
    assertThat(token1).isNotEqualTo(token3);
    assertThat(token1).isNotEqualTo(null);
    assertThat(token1).isNotEqualTo(new Object());
  }

  /** Vérifie les différents constructeurs et la gestion de rawResponse. */
  @Test
  public void shouldHandleSimpleConstructors() {
    final OAuth2AccessToken tokenSimple = new OAuth2AccessToken("at");
    assertThat(tokenSimple.getAccessToken()).isEqualTo("at");

    // Comportement attendu du ScribeJava : getRawResponse lève une exception si non fourni
    assertThatExceptionOfType(IllegalStateException.class)
        .isThrownBy(() -> tokenSimple.getRawResponse());

    final OAuth2AccessToken tokenWithRaw = new OAuth2AccessToken("at", "raw_response");
    assertThat(tokenWithRaw.getAccessToken()).isEqualTo("at");
    assertThat(tokenWithRaw.getRawResponse()).isEqualTo("raw_response");
  }

  /** Vérifie la robustesse du constructeur. */
  @Test
  public void shouldFailOnNullAccessToken() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> new OAuth2AccessToken(null));
  }
}
