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

import org.junit.jupiter.api.Test;

/** Tests pour les modèles de données de base. */
public class CoreDataModelsTest {

  /** Vérifie le fonctionnement de base de Token. */
  @Test
  public void shouldTestToken() {
    final Token token = new Token("raw-response") {};
    assertThat(token.getRawResponse()).isEqualTo("raw-response");
  }

  /** Vérifie OAuth2AccessToken. */
  @Test
  public void shouldTestOAuth2AccessToken() {
    final OAuth2AccessToken token =
        new OAuth2AccessToken("access", "bearer", 3600, "refresh", "scope", "raw");
    assertThat(token.getAccessToken()).isEqualTo("access");
    assertThat(token.getTokenType()).isEqualTo("bearer");
    assertThat(token.getExpiresIn()).isEqualTo(3600);
    assertThat(token.getRefreshToken()).isEqualTo("refresh");
    assertThat(token.getScope()).isEqualTo("scope");
  }

  /** Vérifie PushedAuthorizationResponse. */
  @Test
  public void shouldTestPushedAuthorizationResponse() {
    final PushedAuthorizationResponse par = new PushedAuthorizationResponse("uri-123", 3600);
    assertThat(par.getRequestUri()).isEqualTo("uri-123");
    assertThat(par.getExpiresIn()).isEqualTo(3600);
  }
}
