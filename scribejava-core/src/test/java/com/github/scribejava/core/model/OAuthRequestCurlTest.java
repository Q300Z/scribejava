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

/** Tests DX : Génération de commande cURL. */
public class OAuthRequestCurlTest {

  @Test
  public void shouldGenerateCurlCommand() {
    final OAuthRequest request = new OAuthRequest(Verb.POST, "https://server.com/token");
    request.addHeader("Accept", "application/json");
    request.addParameter("client_id", "my-id");
    request.addParameter("client_secret", "secret");

    // Par défaut, redact=true pour la sécurité
    final String curl = request.toCurlCommand();

    assertThat(curl).startsWith("curl -X POST");
    assertThat(curl).contains("'https://server.com/token'");
    assertThat(curl).contains("-H 'Accept: application/json'");
    assertThat(curl).contains("--data 'client_id=my-id&client_secret=[REDACTED]'");
  }

  @Test
  public void shouldGenerateCurlCommandWithRealSecrets() {
    final OAuthRequest request = new OAuthRequest(Verb.GET, "https://server.com/api");
    request.addHeader("Authorization", "Bearer my-token");

    final String curl = request.toCurlCommand(false); // redact=false

    assertThat(curl).contains("-H 'Authorization: Bearer my-token'");
    assertThat(curl).doesNotContain("[REDACTED]");
  }
}
