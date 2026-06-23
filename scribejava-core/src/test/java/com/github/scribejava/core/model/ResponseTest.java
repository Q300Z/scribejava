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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;
import org.junit.jupiter.api.Test;

/** Tests pour la classe Response. */
public class ResponseTest {

  /**
   * Vérifie le fonctionnement des accesseurs de base, des en-têtes et de la liaison avec la
   * requête.
   *
   * @throws IOException en cas d'erreur E/S.
   */
  @Test
  public void testBasicGettersAndSetters() throws IOException {
    final Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");
    final Response response = new Response(200, "OK", headers, "test body");

    assertThat(response.getCode()).isEqualTo(200);
    assertThat(response.getHeader("Content-Type")).isEqualTo("application/json");
    assertThat(response.getBody()).isEqualTo("test body");
    assertThat(response.getStream()).isNull();

    final OAuthRequest request = new OAuthRequest(Verb.GET, "https://example.com");
    response.setRequest(request);
    assertThat(response.getRequest()).isSameAs(request);

    final String str = response.toString();
    assertThat(str).contains("200");
    assertThat(str).contains("OK");
    assertThat(str).contains("test body");
    assertThat(str).contains("Content-Type");
  }

  /**
   * Vérifie le traitement d'un flux de données standard.
   *
   * @throws IOException en cas d'erreur E/S.
   */
  @Test
  public void testInputStreamStandard() throws IOException {
    final byte[] data = "hello world".getBytes();
    final InputStream is = new ByteArrayInputStream(data);
    final Response response = new Response(200, "OK", Collections.emptyMap(), is);

    assertThat(response.getStream()).isSameAs(is);
    assertThat(response.getBody()).isEqualTo("hello world");
  }

  /**
   * Vérifie le traitement d'un flux de données compressé avec GZIP.
   *
   * @throws IOException en cas d'erreur E/S.
   */
  @Test
  public void testInputStreamGzip() throws IOException {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (GZIPOutputStream gzos = new GZIPOutputStream(baos)) {
      gzos.write("gzip content".getBytes());
    }
    final byte[] compressed = baos.toByteArray();
    final InputStream is = new ByteArrayInputStream(compressed);

    final Map<String, String> headers = new HashMap<>();
    headers.put("Content-Encoding", "gzip");

    final Response response = new Response(200, "OK", headers, is);
    assertThat(response.getBody()).isEqualTo("gzip content");
  }

  /**
   * Vérifie que getBody retourne null si le flux est null et aucun corps n'est défini.
   *
   * @throws IOException en cas d'erreur E/S.
   */
  @Test
  public void testNullStreamReturnsNull() throws IOException {
    final Response response = new Response(200, "OK", Collections.emptyMap(), (InputStream) null);
    assertThat(response.getBody()).isNull();
  }

  /**
   * Vérifie la libération correcte de multiples ressources closeables et la résilience aux
   * exceptions lors de la fermeture.
   *
   * @throws IOException en cas d'erreur E/S.
   */
  @Test
  public void testCloseablesAndExceptions() throws IOException {
    final boolean[] closed = new boolean[2];
    final Closeable closeable1 =
        new Closeable() {
          @Override
          public void close() throws IOException {
            closed[0] = true;
          }
        };
    final Closeable closeable2 =
        new Closeable() {
          @Override
          public void close() throws IOException {
            closed[1] = true;
            throw new IOException("simulated exception on close");
          }
        };

    final Response response =
        new Response(200, "OK", Collections.emptyMap(), null, closeable1, null, closeable2);

    response.close();
    assertThat(closed[0]).isTrue();
    assertThat(closed[1]).isTrue();

    // Secondary close call should be a no-op and not re-invoke closeables
    closed[0] = false;
    closed[1] = false;
    response.close();
    assertThat(closed[0]).isFalse();
    assertThat(closed[1]).isFalse();
  }

  @Test
  public void testStreamReadableAfterGetBody() throws IOException {
    final byte[] data = "hello world".getBytes();
    final InputStream is = new ByteArrayInputStream(data);
    final Response response = new Response(200, "OK", Collections.emptyMap(), is);

    assertThat(response.getBody()).isEqualTo("hello world");

    InputStream responseStream = response.getStream();
    assertThat(responseStream).isNotNull();

    final byte[] buffer = new byte[100];
    int len = responseStream.read(buffer);
    assertThat(new String(buffer, 0, len)).isEqualTo("hello world");
  }
}
