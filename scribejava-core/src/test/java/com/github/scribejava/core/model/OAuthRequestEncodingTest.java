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

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

public class OAuthRequestEncodingTest {

  @Test
  public void shouldHandleUtf8Parameters() {
    final OAuthRequest request = new OAuthRequest(Verb.POST, "http://example.com");
    request.addBodyParameter("name", "ScribeJava ✨");
    request.setCharset(StandardCharsets.UTF_8.name());

    final byte[] payload = request.getByteArrayPayload();
    final String body = new String(payload, StandardCharsets.UTF_8);

    // ScribeJava encodes spaces as %20, not +
    assertThat(body).contains("name=ScribeJava%20%E2%9C%A8");
  }

  @Test
  public void shouldHandleParametersAsUtf8EvenIfCharsetIsDifferent()
      throws UnsupportedEncodingException {
    final OAuthRequest request = new OAuthRequest(Verb.POST, "http://example.com");
    request.addBodyParameter("city", "Montréal");
    // ScribeJava's OAuthEncoder currently enforces UTF-8 for parameter encoding
    request.setCharset(StandardCharsets.ISO_8859_1.name());

    final byte[] payload = request.getByteArrayPayload();
    final String body = new String(payload, StandardCharsets.ISO_8859_1);

    // 'é' encoded in UTF-8 then kept as ASCII-safe percent encoding
    assertThat(body).contains("city=Montr%C3%A9al");
  }

  @Test
  public void shouldSanitizeUrlsCorrectly() {
    assertThat(new OAuthRequest(Verb.GET, "http://example.com:80/path").getSanitizedUrl())
        .isEqualTo("http://example.com/path");
    assertThat(new OAuthRequest(Verb.GET, "https://example.com:443/path").getSanitizedUrl())
        .isEqualTo("https://example.com/path");
    assertThat(new OAuthRequest(Verb.GET, "http://example.com/path?query=1").getSanitizedUrl())
        .isEqualTo("http://example.com/path");
  }

  @Test
  public void shouldRespectDefaultCharset() {
    final OAuthRequest request = new OAuthRequest(Verb.POST, "http://example.com");
    assertThat(request.getCharset()).isNotNull();
  }
}
