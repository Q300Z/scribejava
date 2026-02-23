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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuth2AccessTokenErrorResponse;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.oauth2.OAuth2Error;
import java.io.IOException;
import java.util.Collections;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

/** Tests de l'extracteur JSON des jetons d'accès OAuth 2.0. */
public class OAuth2AccessTokenJsonExtractorTest {

  private final OAuth2AccessTokenJsonExtractor extractor =
      OAuth2AccessTokenJsonExtractor.instance();

  private static Response ok(String body) {
    return new Response(
        200, /* message */ null, /* headers */ Collections.<String, String>emptyMap(), body);
  }

  private static Response error(String body) {
    return new Response(
        400, /* message */ null, /* headers */ Collections.<String, String>emptyMap(), body);
  }

  /** Vérifie l'analyse correcte d'une réponse JSON simple. */
  @Test
  public void shouldParseResponse() throws IOException {
    final String responseBody =
        "{ \"access_token\":\"I0122HHJKLEM21F3WLPYHDKGKZULAUO4SGMV3ABKFTDT3T3X\", "
            + "\"token_type\":\"example\"}";
    final OAuth2AccessToken token;
    try (Response response = ok(responseBody)) {
      token = extractor.extract(response);
    }
    assertEquals("I0122HHJKLEM21F3WLPYHDKGKZULAUO4SGMV3ABKFTDT3T3X", token.getAccessToken());
  }

  /** Vérifie l'analyse de la portée (scope) et du jeton de rafraîchissement. */
  @Test
  public void shouldParseScopeFromResponse() throws IOException {
    final String responseBody =
        "{ \"access_token\":\"I0122HHJKLEM21F3WLPYHDKGKZULAUO4SGMV3ABKFTDT3T4X\", "
            + "\"token_type\":\"example\","
            + "\"scope\":\"s1\"}";
    final OAuth2AccessToken token;
    try (Response response = ok(responseBody)) {
      token = extractor.extract(response);
    }
    assertEquals("I0122HHJKLEM21F3WLPYHDKGKZULAUO4SGMV3ABKFTDT3T4X", token.getAccessToken());
    assertEquals("s1", token.getScope());

    final String responseBody2 =
        "{ \"access_token\":\"I0122HHJKLEM21F3WLPYHDKGKZULAUO4SGMV3ABKFTDT3T5X\", "
            + "\"token_type\":\"example\","
            + "\"scope\":\"s1 s2\"}";
    final OAuth2AccessToken token2;
    try (Response response = ok(responseBody2)) {
      token2 = extractor.extract(response);
    }
    assertEquals("I0122HHJKLEM21F3WLPYHDKGKZULAUO4SGMV3ABKFTDT3T5X", token2.getAccessToken());
    assertEquals("s1 s2", token2.getScope());

    final String responseBody3 =
        "{ \"access_token\":\"I0122HHJKLEM21F3WLPYHDKGKZULAUO4SGMV3ABKFTDT3T6X\", "
            + "\"token_type\":\"example\","
            + "\"scope\":\"s3 s4\", "
            + "\"refresh_token\":\"refresh_token1\"}";
    final OAuth2AccessToken token3;
    try (Response response = ok(responseBody3)) {
      token3 = extractor.extract(response);
    }
    assertEquals("I0122HHJKLEM21F3WLPYHDKGKZULAUO4SGMV3ABKFTDT3T6X", token3.getAccessToken());
    assertEquals("s3 s4", token3.getScope());
    assertEquals("refresh_token1", token3.getRefreshToken());
  }

  /** Vérifie que le passage de paramètres nuls lève une exception. */
  @Test
  public void shouldThrowExceptionIfForNullParameters() throws IOException {
    try (Response response = ok(null)) {
      assertThrows(
          IllegalArgumentException.class,
          new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
              extractor.extract(response);
            }
          });
    }
  }

  /** Vérifie que le passage d'une chaîne vide lève une exception. */
  @Test
  public void shouldThrowExceptionIfForEmptyStrings() throws IOException {
    final String responseBody = "";
    try (Response response = ok(responseBody)) {
      assertThrows(
          IllegalArgumentException.class,
          new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
              extractor.extract(response);
            }
          });
    }
  }

  /** Vérifie la gestion d'une réponse d'erreur JSON standard. */
  @Test
  public void shouldThrowExceptionIfResponseIsError() throws IOException {
    final String responseBody =
        "{"
            + "\"error_description\":\"unknown, invalid, or expired refresh token\","
            + "\"error\":\"invalid_grant\""
            + "}";
    try (Response response = error(responseBody)) {
      final OAuth2AccessTokenErrorResponse oaer =
          assertThrows(
              OAuth2AccessTokenErrorResponse.class,
              new ThrowingRunnable() {
                @Override
                public void run() throws Throwable {
                  extractor.extract(response);
                }
              });
      assertEquals(OAuth2Error.INVALID_GRANT, oaer.getError());
      assertEquals("unknown, invalid, or expired refresh token", oaer.getErrorDescription());
    }
  }

  /** Vérifie la gestion des caractères échappés en JSON. */
  @Test
  public void testEscapedJsonInResponse() throws IOException {
    final String responseBody =
        "{ \"access_token\":\"I0122HKLEM2\\/MV3ABKFTDT3T5X\"," + "\"token_type\":\"example\"}";
    final OAuth2AccessToken token;
    try (Response response = ok(responseBody)) {
      token = extractor.extract(response);
    }
    assertEquals("I0122HKLEM2/MV3ABKFTDT3T5X", token.getAccessToken());
  }
}
