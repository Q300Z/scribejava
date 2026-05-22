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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Tests unitaires pour {@link DefaultOAuthLogger}. */
class DefaultOAuthLoggerTest {

  /** Vérifie le masquage des champs sensibles dans le corps d'une réponse JSON. */
  @Test
  public void shouldMaskSensitiveJsonFieldsInResponse() {
    final DefaultOAuthLogger logger = new DefaultOAuthLogger();

    final String rawJson =
        "{\n"
            + "  \"access_token\": \"secret_access_token_123\",\n"
            + "  \"refresh_token\": \"secret_refresh_token_456\",\n"
            + "  \"id_token\": \"secret_id_token_789\",\n"
            + "  \"code\": \"secret_code_000\",\n"
            + "  \"client_secret\": \"my_app_secret\",\n"
            + "  \"password\": \"my_super_secret_pwd\",\n"
            + "  \"username\": \"johndoe\"\n"
            + "}";

    final String sanitized = logger.sanitizeResponseBody(rawJson);

    assertTrue(
        sanitized.contains("\"access_token\" : \"[MASKED]\"")
            || sanitized.contains("\"access_token\": \"[MASKED]\""));
    assertTrue(sanitized.contains("\"refresh_token\": \"[MASKED]\""));
    assertTrue(sanitized.contains("\"id_token\": \"[MASKED]\""));
    assertTrue(sanitized.contains("\"code\": \"[MASKED]\""));
    assertTrue(sanitized.contains("\"client_secret\": \"[MASKED]\""));
    assertTrue(sanitized.contains("\"password\": \"[MASKED]\""));
    assertTrue(sanitized.contains("\"username\": \"johndoe\""));
    assertFalse(sanitized.contains("secret_access_token"));
  }

  /** Vérifie le masquage des paramètres de formulaire encodés dans l'URL. */
  @Test
  public void shouldMaskSensitiveUrlEncodedParamsInResponse() {
    final DefaultOAuthLogger logger = new DefaultOAuthLogger();

    final String rawPayload = "access_token=secret_123&refresh_token=secret_456&username=johndoe";
    final String sanitized = logger.sanitizeResponseBody(rawPayload);

    assertEquals("access_token=[MASKED]&refresh_token=[MASKED]&username=johndoe", sanitized);
  }

  /** Vérifie le bon formatage et masquage d'une requête HTTP vers l'OutputStream. */
  @Test
  public void shouldLogRequestCorrectlyToOutputStream() {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final DefaultOAuthLogger logger = new DefaultOAuthLogger(baos);

    final OAuthRequest request = new OAuthRequest(Verb.POST, "http://example.com/oauth/token");
    request.addHeader("Authorization", "Bearer sensitive_token");
    request.addBodyParameter("client_secret", "super_secret");
    request.addBodyParameter("grant_type", "client_credentials");

    logger.logRequest(request);

    final String logOutput = baos.toString();
    assertTrue(logOutput.contains("[ScribeJava] ---> HTTP REQUEST"));
    assertTrue(logOutput.contains("POST http://example.com/oauth/token"));
    assertTrue(logOutput.contains("Authorization=Bearer [REDACTED]"));
    assertTrue(logOutput.contains("client_secret=[REDACTED]"));
    assertTrue(logOutput.contains("grant_type=client_credentials"));
    assertTrue(logOutput.contains("cURL:"));
    assertTrue(logOutput.contains("[ScribeJava] ---> END HTTP REQUEST"));
  }

  /**
   * Vérifie le bon formatage et masquage d'une réponse HTTP vers l'OutputStream.
   *
   * @throws IOException en cas d'erreur de lecture.
   */
  @Test
  public void shouldLogResponseCorrectlyToOutputStream() throws IOException {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final DefaultOAuthLogger logger = new DefaultOAuthLogger(baos);

    final Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");

    final String body = "{\"access_token\":\"secret_token_123\",\"token_type\":\"Bearer\"}";
    final Response response = new Response(200, "OK", headers, body);

    logger.logResponse(response);

    final String logOutput = baos.toString();
    assertTrue(logOutput.contains("[ScribeJava] <--- HTTP RESPONSE"));
    assertTrue(logOutput.contains("Status Code: 200"));
    assertTrue(logOutput.contains("Body:"));
    assertTrue(logOutput.contains("\"access_token\":\"[MASKED]\""));
    assertTrue(logOutput.contains("\"token_type\":\"Bearer\""));
    assertTrue(logOutput.contains("[ScribeJava] <--- END HTTP RESPONSE"));
  }

  /** Vérifie la résilience du logger face à des entrées nulles. */
  @Test
  void shouldBeResilientToNullInputs() {
    final DefaultOAuthLogger logger = new DefaultOAuthLogger();
    assertDoesNotThrow(() -> logger.logRequest(null));
    assertDoesNotThrow(() -> logger.logResponse(null));
    assertEquals("", logger.sanitizeResponseBody(null));
  }

  /** Vérifie le comportement du logger avec une réponse dont le corps est nul. */
  @Test
  void shouldLogResponseWithNullBody() {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final DefaultOAuthLogger logger = new DefaultOAuthLogger(baos);
    final Response response = new Response(200, "OK", new HashMap<>(), (String) null);

    logger.logResponse(response);

    final String logOutput = baos.toString();
    assertTrue(logOutput.contains("Status Code: 200"));
    assertFalse(logOutput.contains("Body:"));
  }

  /** Vérifie que le logger attrape et journalise proprement les IOException du corps. */
  @Test
  void shouldHandleIOExceptionWhenReadingResponseBody() throws IOException {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final DefaultOAuthLogger logger = new DefaultOAuthLogger(baos);

    final Response mockResponse = mock(Response.class);
    when(mockResponse.getCode()).thenReturn(500);
    when(mockResponse.getBody()).thenThrow(new IOException("Simulated network failure"));

    logger.logResponse(mockResponse);

    final String logOutput = baos.toString();
    assertTrue(logOutput.contains("Status Code: 500"));
    assertTrue(
        logOutput.contains("Body: [Error reading response body: Simulated network failure]"));
  }
}
