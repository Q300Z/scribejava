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
package com.github.scribejava.core.httpclient.jdk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.scribejava.core.httpclient.multipart.ByteArrayBodyPartPayload;
import com.github.scribejava.core.httpclient.multipart.MultipartPayload;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import java.io.IOException;
import java.util.Collections;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests étendus pour le client HTTP JDK, incluant les payloads chaîne et multipart. */
public class JDKHttpClientExtendedTest {

  private MockWebServer server;
  private JDKHttpClient client;

  /**
   * Initialisation du serveur et du client.
   *
   * @throws IOException en cas d'erreur.
   */
  @BeforeEach
  public void setUp() throws IOException {
    server = new MockWebServer();
    server.start();
    client = new JDKHttpClient();
  }

  /**
   * Arrêt du serveur.
   *
   * @throws IOException en cas d'erreur.
   */
  @AfterEach
  public void tearDown() throws IOException {
    server.shutdown();
  }

  /** Vérifie l'exécution d'une requête avec un corps de message sous forme de chaîne. */
  @Test
  public void shouldExecuteWithPayloadAsString() throws Exception {
    server.enqueue(new MockResponse().setResponseCode(200).setBody("OK"));
    final Response resp =
        client.execute(
            "UA", Collections.emptyMap(), Verb.POST, server.url("/").toString(), "payload-string");
    assertThat(resp.getCode()).isEqualTo(200);
    assertThat(server.takeRequest().getBody().readUtf8()).isEqualTo("payload-string");
  }

  /** Vérifie l'exécution asynchrone d'une requête avec un corps de message sous forme de chaîne. */
  @Test
  public void shouldExecuteAsyncWithPayloadAsString() throws Exception {
    server.enqueue(new MockResponse().setResponseCode(200).setBody("OK"));
    final String result =
        client
            .executeAsync(
                "UA",
                Collections.emptyMap(),
                Verb.POST,
                server.url("/").toString(),
                "payload-string",
                null,
                response -> response.getBody())
            .get();
    assertThat(result).isEqualTo("OK");
  }

  /** Vérifie l'exécution d'une requête Multipart. */
  @Test
  public void shouldExecuteWithMultipartPayload() throws Exception {
    server.enqueue(new MockResponse().setResponseCode(200));
    final MultipartPayload multipart = new MultipartPayload();
    multipart.addBodyPart(new ByteArrayBodyPartPayload("content1".getBytes()));

    final Response resp =
        client.execute(
            "UA", Collections.emptyMap(), Verb.POST, server.url("/").toString(), multipart);
    assertThat(resp.getCode()).isEqualTo(200);
  }

  /** Vérifie que l'envoi de fichiers n'est pas encore supporté en synchrone. */
  @Test
  public void shouldThrowExceptionOnFileSync() {
    assertThrows(
        UnsupportedOperationException.class,
        () ->
            client.execute(
                "UA",
                Collections.emptyMap(),
                Verb.POST,
                server.url("/").toString(),
                new java.io.File("dummy")));
  }

  /** Vérifie que l'envoi de fichiers n'est pas encore supporté en asynchrone. */
  @Test
  public void shouldThrowExceptionOnFileAsync() {
    assertThrows(
        UnsupportedOperationException.class,
        () ->
            client.executeAsync(
                "UA",
                Collections.emptyMap(),
                Verb.POST,
                server.url("/").toString(),
                new java.io.File("dummy"),
                null,
                null));
  }
}
