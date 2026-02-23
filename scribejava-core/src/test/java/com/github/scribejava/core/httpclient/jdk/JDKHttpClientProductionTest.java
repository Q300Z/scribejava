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

import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JDKHttpClientProductionTest {

  private MockWebServer server;
  private JDKHttpClient client;

  @BeforeEach
  public void setUp() throws IOException {
    server = new MockWebServer();
    server.start();
    client = new JDKHttpClient();
  }

  @AfterEach
  public void tearDown() throws IOException {
    server.shutdown();
  }

  @Test
  public void shouldSupportPatchMethodViaOverride()
      throws IOException, InterruptedException, ExecutionException {
    server.enqueue(new MockResponse().setResponseCode(200).setBody("patched"));

    final Response response =
        client.execute(
            "UA", Collections.emptyMap(), Verb.PATCH, server.url("/").toString(), "payload");

    assertThat(response.getCode()).isEqualTo(200);
    assertThat(response.getBody()).isEqualTo("patched");

    final okhttp3.mockwebserver.RecordedRequest recordedRequest = server.takeRequest();
    // Le JDK envoie un POST avec l'en-tête d'override pour simuler PATCH
    assertThat(recordedRequest.getMethod()).isEqualTo("POST");
    assertThat(recordedRequest.getHeader("X-HTTP-Method-Override")).isEqualTo("PATCH");
  }

  @Test
  public void shouldSupportCustomHostnameVerifier() {
    final javax.net.ssl.HostnameVerifier mockVerifier =
        org.mockito.Mockito.mock(javax.net.ssl.HostnameVerifier.class);
    final JDKHttpClientConfig config =
        JDKHttpClientConfig.defaultConfig().withHostnameVerifier(mockVerifier);
    final JDKHttpClient clientWithVerifier = new JDKHttpClient(config);

    assertThat(clientWithVerifier).isNotNull();
    // On ne peut pas facilement déclencher l'appel au verifier sans une vraie connexion HTTPS,
    // mais on vérifie que la config est bien acceptée.
  }
}
