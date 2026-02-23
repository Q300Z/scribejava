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
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.Response;
import java.io.IOException;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/** Tests de robustesse pour la gestion des erreurs JSON lors de l'extraction des jetons. */
public class JsonErrorTest {

  /** Vérifie que les réponses qui ne sont pas des objets JSON lèvent une exception. */
  @Test
  public void shouldHandleNonJsonObject() {
    final Response response = new Response(200, "OK", Collections.emptyMap(), "\"not an object\"");
    assertThrows(
        OAuthException.class, () -> OAuth2AccessTokenJsonExtractor.instance().extract(response));
  }

  /** Vérifie la levée d'exception si le jeton d'accès est manquant. */
  @Test
  public void shouldHandleMissingRequiredAccessToken() {
    final Response response =
        new Response(200, "OK", Collections.emptyMap(), "{\"expires_in\":3600}");
    final OAuthException ex =
        assertThrows(
            OAuthException.class,
            () -> OAuth2AccessTokenJsonExtractor.instance().extract(response));
    assertThat(ex.getMessage()).contains("access_token");
  }

  /** Vérifie la gestion d'une syntaxe JSON invalide. */
  @Test
  public void shouldHandleInvalidJsonSyntax() {
    final Response response = new Response(200, "OK", Collections.emptyMap(), "{invalid}");
    // Jackson throws JsonProcessingException which is an IOException
    assertThrows(
        IOException.class, () -> OAuth2AccessTokenJsonExtractor.instance().extract(response));
  }

  /** Vérifie que les types inversés (ex: nombre en chaîne) sont gérés avec souplesse. */
  @Test
  public void shouldHandleInvertedTypes() throws IOException {
    // expires_in as string "3600" should be handled by JsonNode.asInt()
    final String json = "{\"access_token\":\"at123\", \"expires_in\":\"3600\"}";
    final Response response = new Response(200, "OK", Collections.emptyMap(), json);
    final OAuth2AccessToken token = OAuth2AccessTokenJsonExtractor.instance().extract(response);
    assertThat(token.getAccessToken()).isEqualTo("at123");
    assertThat(token.getExpiresIn()).isEqualTo(3600);
  }

  /** Vérifie que la valeur nulle pour un paramètre obligatoire est rejetée. */
  @Test
  public void shouldHandleNullRequiredParameter() {
    final Response response =
        new Response(200, "OK", Collections.emptyMap(), "{\"access_token\":null}");
    assertThrows(
        OAuthException.class, () -> OAuth2AccessTokenJsonExtractor.instance().extract(response));
  }

  /** Vérifie la gestion d'une réponse totalement vide. */
  @Test
  public void shouldHandleEmptyResponse() {
    final Response response = new Response(200, "OK", Collections.emptyMap(), "");
    assertThrows(
        IllegalArgumentException.class,
        () -> OAuth2AccessTokenJsonExtractor.instance().extract(response));
  }

  /** Vérifie que la réception d'un tableau JSON au lieu d'un objet lève une exception. */
  @Test
  public void shouldHandleArrayInsteadOfObject() {
    final Response response =
        new Response(200, "OK", Collections.emptyMap(), "[{\"access_token\":\"at123\"}]");
    assertThrows(
        OAuthException.class, () -> OAuth2AccessTokenJsonExtractor.instance().extract(response));
  }

  /** Vérifie la gestion d'une valeur de durée de validité extrêmement longue. */
  @Test
  public void shouldHandleExtremeLongValueForExpiresIn() throws IOException {
    // Jackson will parse 999999999999999 as a LongNode, asInt() will return its int value
    // (overflow)
    final String json = "{\"access_token\":\"at123\", \"expires_in\":999999999999999}";
    final Response response = new Response(200, "OK", Collections.emptyMap(), json);
    final OAuth2AccessToken token = OAuth2AccessTokenJsonExtractor.instance().extract(response);
    assertThat(token.getAccessToken()).isEqualTo("at123");
    // 999999999999999 in binary is ...
    // asInt() on LongNode returns (int) longValue
    assertThat(token.getExpiresIn()).isNotNull();
  }

  /** Vérifie la gestion d'une valeur non numérique pour la durée de validité. */
  @Test
  public void shouldHandleNonNumericExpiresIn() throws IOException {
    final String json = "{\"access_token\":\"at123\", \"expires_in\":\"not-a-number\"}";
    final Response response = new Response(200, "OK", Collections.emptyMap(), json);
    final OAuth2AccessToken token = OAuth2AccessTokenJsonExtractor.instance().extract(response);
    assertThat(token.getAccessToken()).isEqualTo("at123");
    // asInt() returns 0 for non-numeric strings
    assertThat(token.getExpiresIn()).isEqualTo(0);
  }

  /** Vérifie la gestion d'un objet imbriqué là où un entier est attendu. */
  @Test
  public void shouldHandleNestedObjectForExpiresIn() throws IOException {
    final String json = "{\"access_token\":\"at123\", \"expires_in\":{\"nested\":3600}}";
    final Response response = new Response(200, "OK", Collections.emptyMap(), json);
    final OAuth2AccessToken token = OAuth2AccessTokenJsonExtractor.instance().extract(response);
    assertThat(token.getAccessToken()).isEqualTo("at123");
    // asInt() returns 0 for objects
    assertThat(token.getExpiresIn()).isEqualTo(0);
  }
}
