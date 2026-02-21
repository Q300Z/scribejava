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

import java.io.File;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

public class OAuthRequestPayloadTest {

  @Test
  public void shouldHandleStringPayload() {
    final OAuthRequest request = new OAuthRequest(Verb.POST, "http://example.com");
    final String payload = "{\"key\":\"value\"}";
    request.setPayload(payload);

    assertThat(request.getStringPayload()).isEqualTo(payload);
    // getByteArrayPayload() returns body parameters, not the string payload
    assertThat(request.getByteArrayPayload()).isEmpty();
  }

  @Test
  public void shouldHandleSpecialCharactersInStringPayload() {
    final OAuthRequest request = new OAuthRequest(Verb.POST, "http://example.com");
    final String payload = "{\"key\":\"valué ❤️\"}";
    request.setPayload(payload);
    request.setCharset(StandardCharsets.UTF_8.name());

    assertThat(request.getStringPayload()).isEqualTo(payload);
    assertThat(request.getByteArrayPayload()).isEmpty();
  }

  @Test
  public void shouldHandleByteArrayPayload() {
    final OAuthRequest request = new OAuthRequest(Verb.POST, "http://example.com");
    final byte[] payload = {0x01, 0x02, 0x03, 0x04};
    request.setPayload(payload);

    assertThat(request.getStringPayload()).isNull();
    assertThat(request.getByteArrayPayload()).isEqualTo(payload);
  }

  @Test
  public void shouldHandleFilePayload() {
    final OAuthRequest request = new OAuthRequest(Verb.POST, "http://example.com");
    final File file = new File("dummy-file");
    request.setPayload(file);

    assertThat(request.getFilePayload()).isEqualTo(file);
    assertThat(request.getStringPayload()).isNull();
    // getByteArrayPayload() should still work if body parameters are added
    request.addBodyParameter("key", "value");
    assertThat(new String(request.getByteArrayPayload(), StandardCharsets.UTF_8))
        .isEqualTo("key=value");
  }

  @Test
  public void shouldHandleNoPayloadForGet() {
    final OAuthRequest request = new OAuthRequest(Verb.GET, "http://example.com");
    assertThat(request.getStringPayload()).isNull();
    assertThat(request.getByteArrayPayload()).isEmpty();
  }

  @Test
  public void shouldHandleNoPayloadForDelete() {
    final OAuthRequest request = new OAuthRequest(Verb.DELETE, "http://example.com");
    assertThat(request.getStringPayload()).isNull();
    assertThat(request.getByteArrayPayload()).isEmpty();
  }

  @Test
  public void shouldResetPayloadWhenNewOneIsSet() {
    final OAuthRequest request = new OAuthRequest(Verb.POST, "http://example.com");
    request.setPayload("string-payload");
    assertThat(request.getStringPayload()).isNotNull();

    request.setPayload(new byte[] {0x01});
    assertThat(request.getStringPayload()).isNull();
    assertThat(request.getByteArrayPayload()).containsExactly(0x01);

    request.setPayload(new File("file"));
    assertThat(request.getByteArrayPayload()).isEmpty();
    assertThat(request.getFilePayload()).isNotNull();
  }
}
