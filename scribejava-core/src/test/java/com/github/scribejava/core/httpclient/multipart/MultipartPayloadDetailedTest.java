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
package com.github.scribejava.core.httpclient.multipart;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.scribejava.core.httpclient.HttpClient;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Tests détaillés pour la gestion des payloads Multipart complexes. */
public class MultipartPayloadDetailedTest {

  /** Vérifie la génération correcte d'un corps Multipart contenant une partie Multipart imbriquée. */
  @Test
  public void shouldHandleNestedMultipart() throws IOException {
    final String outerBoundary = "outer-boundary";
    final String innerBoundary = "inner-boundary";

    final MultipartPayload outer = new MultipartPayload(outerBoundary);
    final MultipartPayload inner = new MultipartPayload(innerBoundary);

    inner.addBodyPart(new ByteArrayBodyPartPayload("inner-data".getBytes()));
    outer.addBodyPart(inner);
    outer.setPreamble("outer-preamble");
    outer.setEpilogue("outer-epilogue");

    final ByteArrayOutputStream os = MultipartUtils.getPayload(outer);
    final String result = os.toString();

    assertThat(result).contains("outer-preamble");
    assertThat(result).contains("--outer-boundary");
    assertThat(result).contains("--inner-boundary");
    assertThat(result).contains("inner-data");
    assertThat(result).contains("--inner-boundary--");
    assertThat(result).contains("--outer-boundary--");
    assertThat(result).contains("outer-epilogue");
  }

  /**
   * Vérifie que l'utilisation du même séparateur pour le parent et l'enfant lève une exception.
   */
  @Test
  public void shouldThrowWhenBoundariesAreSame() {
    final String boundary = "same-boundary";
    final MultipartPayload outer = new MultipartPayload(boundary);
    final MultipartPayload inner = new MultipartPayload(boundary);

    assertThatThrownBy(() -> outer.addBodyPart(inner))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("is the same for parent MultipartPayload and child");
  }

  /** Vérifie la composition correcte des en-têtes Content-Type. */
  @Test
  public void shouldComposeHeadersCorrectively() {
    final Map<String, String> customHeaders = new HashMap<>();
    customHeaders.put("X-Custom", "Value");

    final MultipartPayload payload = new MultipartPayload("mixed", "my-boundary", customHeaders);

    assertThat(payload.getHeaders().get(HttpClient.CONTENT_TYPE))
        .isEqualTo("multipart/mixed; boundary=\"my-boundary\"");
    assertThat(payload.getHeaders().get("X-Custom")).isEqualTo("Value");
  }

  /** Vérifie le rejet des séparateurs conflictuels entre le constructeur et les en-têtes. */
  @Test
  public void shouldThrowWhenConflictingBoundariesInHeaders() {
    final Map<String, String> headers = new HashMap<>();
    headers.put(HttpClient.CONTENT_TYPE, "multipart/form-data; boundary=\"other-boundary\"");

    assertThatThrownBy(() -> new MultipartPayload("my-boundary", headers))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Different boundaries was passed in constructors");
  }

  /** Vérifie l'extraction automatique du séparateur depuis l'en-tête Content-Type. */
  @Test
  public void shouldParseBoundaryFromHeader() {
    final Map<String, String> headers = new HashMap<>();
    headers.put(HttpClient.CONTENT_TYPE, "multipart/form-data; boundary=\"parsed-boundary\"");

    final MultipartPayload payload = new MultipartPayload(headers);
    assertThat(payload.getBoundary()).isEqualTo("parsed-boundary");
  }

  /** Vérifie le rejet d'un séparateur ayant une syntaxe invalide. */
  @Test
  public void shouldHandleInvalidBoundarySyntax() {
    assertThatThrownBy(() -> new MultipartPayload("invalid boundary with spaces at end "))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
