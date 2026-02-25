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

import org.junit.jupiter.api.Test;

/** Tests DX : Debugging sécurisé avec redaction des secrets. */
public class OAuthRequestDebugTest {

  @Test
  public void shouldRedactSecretsInDebugString() {
    final OAuthRequest request = new OAuthRequest(Verb.POST, "https://server.com/token");
    request.addParameter("client_id", "my-id");
    request.addParameter("client_secret", "SUPER_SECRET_KEY");
    request.addHeader("Authorization", "Bearer MY_SECRET_TOKEN");

    final String debug = request.toDebugString();

    assertThat(debug).contains("client_id=my-id");
    assertThat(debug).contains("client_secret=[REDACTED]");
    assertThat(debug).contains("Authorization=Bearer [REDACTED]");
    assertThat(debug).doesNotContain("SUPER_SECRET_KEY");
    assertThat(debug).doesNotContain("MY_SECRET_TOKEN");
  }
}
