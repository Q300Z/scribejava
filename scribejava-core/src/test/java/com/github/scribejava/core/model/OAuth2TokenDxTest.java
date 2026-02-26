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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;

/** Tests DX : Gestion intelligente de l'expiration. */
public class OAuth2TokenDxTest {

  @Test
  public void shouldReportExpirationCorrectly() {
    // Un jeton qui expire dans 1 heure
    final OAuth2AccessToken token = new OAuth2AccessToken("at", "bearer", 3600, null, null, null);

    assertThat(token.isExpired()).isFalse();
    assertThat(token.getExpiresAt()).isPresent();

    // On vérifie que la date d'expiration est cohérente (environ now + 1h)
    final Instant expected = Instant.now().plus(1, ChronoUnit.HOURS);
    final Instant actual = token.getExpiresAt().get();
    assertThat(Math.abs(ChronoUnit.SECONDS.between(expected, actual))).isLessThan(5);
  }

  @Test
  public void shouldHandleExpiredToken() throws InterruptedException {
    // Un jeton expiré (expires_in = 0 ou négatif)
    final OAuth2AccessToken token = new OAuth2AccessToken("at", "bearer", -10, null, null, null);

    assertThat(token.isExpired()).isTrue();
  }

  @Test
  public void shouldHandleTokenWithoutExpiration() {
    final OAuth2AccessToken token = new OAuth2AccessToken("at");

    assertThat(token.isExpired()).isFalse();
    assertThat(token.getExpiresAt()).isEmpty();
  }
}
