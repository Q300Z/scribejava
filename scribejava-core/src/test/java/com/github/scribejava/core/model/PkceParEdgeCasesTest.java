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
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.scribejava.core.exceptions.OAuthException;
import java.io.IOException;
import org.junit.jupiter.api.Test;

/** Tests des cas limites pour PKCE et PAR. */
public class PkceParEdgeCasesTest {

  /** Vérifie l'analyse d'une réponse PAR (Pushed Authorization Response) valide. */
  @Test
  public void shouldParseValidParResponse() throws IOException {
    final String json = "{\"request_uri\":\"urn:par:123\", \"expires_in\":3600}";
    final PushedAuthorizationResponse resp = PushedAuthorizationResponse.parse(json);
    assertThat(resp.getRequestUri()).isEqualTo("urn:par:123");
    assertThat(resp.getExpiresIn()).isEqualTo(3600L);
  }

  /** Vérifie l'analyse d'une réponse PAR où le champ expires_in est manquant. */
  @Test
  public void shouldParseParResponseWithMissingExpiresIn() throws IOException {
    final String json = "{\"request_uri\":\"urn:par:123\"}";
    final PushedAuthorizationResponse resp = PushedAuthorizationResponse.parse(json);
    assertThat(resp.getRequestUri()).isEqualTo("urn:par:123");
    assertThat(resp.getExpiresIn()).isNull();
  }

  /** Vérifie que l'absence de request_uri lève une exception. */
  @Test
  public void shouldHandleMissingRequestUri() {
    final String json = "{\"expires_in\":3600}";
    assertThrows(OAuthException.class, () -> PushedAuthorizationResponse.parse(json));
  }

  /** Vérifie que l'analyse d'une réponse vide lève une exception. */
  @Test
  public void shouldHandleEmptyParResponse() {
    assertThrows(IllegalArgumentException.class, () -> PushedAuthorizationResponse.parse(""));
  }

  /** Vérifie que l'analyse d'un JSON invalide lève une exception. */
  @Test
  public void shouldHandleInvalidJsonInParResponse() {
    assertThrows(IOException.class, () -> PushedAuthorizationResponse.parse("{invalid}"));
  }

  /** Vérifie la gestion d'un délai d'expiration immédiat (0). */
  @Test
  public void shouldHandleImmediateExpiration() throws IOException {
    final String json = "{\"request_uri\":\"urn:par:123\", \"expires_in\":0}";
    final PushedAuthorizationResponse resp = PushedAuthorizationResponse.parse(json);
    assertThat(resp.getExpiresIn()).isEqualTo(0L);
  }
}
