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

import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.Response;
import java.io.IOException;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

/**
 * Tests unitaires pour {@link OAuth2AccessTokenExtractor}.
 *
 * <p>Vérifie l'extraction des jetons OAuth 2.0 à partir de réponses encodées (format non-JSON).
 */
public class OAuth2AccessTokenExtractorTest {

  private OAuth2AccessTokenExtractor extractor;

  private static Response ok(String body) {
    return new Response(
        200, /* message */ null, /* headers */ Collections.<String, String>emptyMap(), body);
  }

  private static Response error(String body) {
    return new Response(
        400, /* message */ null, /* headers */ Collections.<String, String>emptyMap(), body);
  }

  /** Initialisation de l'extracteur. */
  @Before
  public void setUp() {
    extractor = OAuth2AccessTokenExtractor.instance();
  }

  /** Vérifie l'extraction du jeton depuis une réponse OAuth standard. */
  @Test
  public void shouldExtractTokenFromOAuthStandardResponse() throws IOException {
    final String responseBody =
        "access_token=166942940015970|2.2ltzWXYNDjCtg5ZDVVJJeg__.3600.1295816400-548517159"
            + "|RsXNdKrpxg8L6QNLWcs2TVTmcaE";
    final OAuth2AccessToken extracted;
    try (Response response = ok(responseBody)) {
      extracted = extractor.extract(response);
    }
    assertEquals(
        "166942940015970|2.2ltzWXYNDjCtg5ZDVVJJeg__.3600.1295816400-548517159|RsXNdKrpxg8L6QNLWcs2TVTmcaE",
        extracted.getAccessToken());
  }

  /** Vérifie l'extraction incluant le paramètre d'expiration. */
  @Test
  public void shouldExtractTokenFromResponseWithExpiresParam() throws IOException {
    final String responseBody =
        "access_token=166942940015970|2.2ltzWXYNDjCtg5ZDVVJJeg__.3600.1295816400-548517159"
            + "|RsXNdKrpxg8L6QNLWcs2TVTmcaE&expires_in=5108";
    final OAuth2AccessToken extracted;
    try (Response response = ok(responseBody)) {
      extracted = extractor.extract(response);
    }
    assertEquals(
        "166942940015970|2.2ltzWXYNDjCtg5ZDVVJJeg__.3600.1295816400-548517159|RsXNdKrpxg8L6QNLWcs2TVTmcaE",
        extracted.getAccessToken());
    assertEquals(Integer.valueOf(5108), extracted.getExpiresIn());
  }

  /** Vérifie l'extraction avec expiration, type de jeton et jeton de renouvellement. */
  @Test
  public void shouldExtractTokenFromResponseWithExpiresAndRefreshParam() throws IOException {
    final String responseBody =
        "access_token=166942940015970|2.2ltzWXYNDjCtg5ZDVVJJeg__.3600.1295816400-548517159"
            + "|RsXNdKrpxg8L6QNLWcs2TVTmcaE&expires_in=5108&token_type=bearer&refresh_token=166942940015970";
    final OAuth2AccessToken extracted;
    try (Response response = ok(responseBody)) {
      extracted = extractor.extract(response);
    }
    assertEquals(
        "166942940015970|2.2ltzWXYNDjCtg5ZDVVJJeg__.3600.1295816400-548517159|RsXNdKrpxg8L6QNLWcs2TVTmcaE",
        extracted.getAccessToken());
    assertEquals(Integer.valueOf(5108), extracted.getExpiresIn());
    assertEquals("bearer", extracted.getTokenType());
    assertEquals("166942940015970", extracted.getRefreshToken());
  }

  /** Vérifie l'extraction même en présence de nombreux paramètres inattendus. */
  @Test
  public void shouldExtractTokenFromResponseWithManyParameters() throws IOException {
    final String responseBody = "access_token=foo1234&other_stuff=yeah_we_have_this_too&number=42";
    final OAuth2AccessToken extracted;
    try (Response response = ok(responseBody)) {
      extracted = extractor.extract(response);
    }
    assertEquals("foo1234", extracted.getAccessToken());
  }

  /** Vérifie la levée d'exception pour une réponse d'erreur HTTP. */
  @Test
  public void shouldThrowExceptionIfErrorResponse() throws IOException {
    final String responseBody = "";
    try (Response response = error(responseBody)) {
      assertThrows(
          OAuthException.class,
          new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
              extractor.extract(response);
            }
          });
    }
  }

  /** Vérifie la levée d'exception si le paramètre access_token est absent. */
  @Test
  public void shouldThrowExceptionIfTokenIsAbsent() throws IOException {
    final String responseBody = "&expires=5108";
    try (Response response = ok(responseBody)) {
      assertThrows(
          OAuthException.class,
          new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
              extractor.extract(response);
            }
          });
    }
  }

  /** Vérifie le rejet d'une réponse nulle. */
  @Test
  public void shouldThrowExceptionIfResponseIsNull() throws IOException {
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

  /** Vérifie le rejet d'une réponse contenant une chaîne vide. */
  @Test
  public void shouldThrowExceptionIfResponseIsEmptyString() throws IOException {
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
}
