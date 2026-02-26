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
package com.github.scribejava.core.extractors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.github.scribejava.core.model.DeviceAuthorization;
import com.github.scribejava.core.model.JsonBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuth2AccessTokenErrorResponse;
import com.github.scribejava.core.model.Response;
import java.io.IOException;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/** Tests des cas limites pour les extracteurs de jetons cœur (Core). */
public class CoreExtractorsEdgeCasesTest {

  /** Vérifie que le rejet d'une réponse vide par l'extracteur de jeton d'accès. */
  @Test
  public void shouldRejectEmptyResponseInAccessTokenExtractor() {
    final Response response = new Response(200, "OK", Collections.emptyMap(), "");
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> OAuth2AccessTokenJsonExtractor.instance().extract(response));
  }

  /** Vérifie l'extraction d'un jeton d'accès quand des paramètres optionnels manquent. */
  @Test
  public void shouldExtractAccessTokenWithMissingOptionalParams() throws IOException {
    final String json = new JsonBuilder().add("access_token", "token123").build();
    final Response response = new Response(200, "OK", Collections.emptyMap(), json);
    final OAuth2AccessToken token = OAuth2AccessTokenJsonExtractor.instance().extract(response);
    assertThat(token.getAccessToken()).isEqualTo("token123");
    assertThat(token.getExpiresIn()).isNull();
  }

  /** Vérifie la gestion d'une réponse d'erreur OAuth 2.0. */
  @Test
  public void shouldHandleOAuth2ErrorResponse() {
    final String json =
        new JsonBuilder()
            .add("error", "invalid_request")
            .add("error_description", "bad stuff")
            .build();
    final Response response = new Response(400, "Bad Request", Collections.emptyMap(), json);
    assertThatExceptionOfType(OAuth2AccessTokenErrorResponse.class)
        .isThrownBy(() -> OAuth2AccessTokenJsonExtractor.instance().extract(response));
  }

  /** Vérifie l'extraction correcte d'une autorisation d'appareil. */
  @Test
  public void shouldExtractDeviceAuthorization() throws IOException {
    final String json =
        new JsonBuilder()
            .add("device_code", "dc123")
            .add("user_code", "uc456")
            .add("verification_uri", "https://uri")
            .add("expires_in", 600)
            .add("interval", 5)
            .build();
    final Response response = new Response(200, "OK", Collections.emptyMap(), json);
    final DeviceAuthorization auth = DeviceAuthorizationJsonExtractor.instance().extract(response);
    assertThat(auth.getDeviceCode()).isEqualTo("dc123");
    assertThat(auth.getIntervalSeconds()).isEqualTo(5);
  }

  /**
   * Vérifie que l'absence de paramètres obligatoires dans l'autorisation appareil lève une erreur.
   */
  @Test
  public void shouldThrowExceptionWhenRequiredParamMissingInDeviceAuth() {
    final String json = new JsonBuilder().add("device_code", "dc123").build();
    final Response response = new Response(200, "OK", Collections.emptyMap(), json);
    assertThatExceptionOfType(com.github.scribejava.core.exceptions.OAuthException.class)
        .isThrownBy(() -> DeviceAuthorizationJsonExtractor.instance().extract(response));
  }
}
