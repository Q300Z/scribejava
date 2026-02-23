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
package com.github.scribejava.oidc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.scribejava.core.httpclient.jdk.JDKHttpClient;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OidcRegistrationServiceTest {

  private MockWebServer server;
  private OidcRegistrationService registrationService;

  @BeforeEach
  public void setUp() throws IOException {
    server = new MockWebServer();
    server.start();
    registrationService = new OidcRegistrationService(new JDKHttpClient(), "UA");
  }

  @AfterEach
  public void tearDown() throws IOException {
    server.shutdown();
  }

  @Test
  public void shouldRegisterClient() throws Exception {
    final String json = "{\"client_id\":\"client123\", \"client_secret\":\"secret456\"}";
    server.enqueue(new MockResponse().setBody(json).setResponseCode(201));

    final JsonNode result =
        registrationService
            .registerClientAsync(
                server.url("/register").toString(),
                Arrays.asList("http://callback.com"),
                "My App",
                "client_secret_post")
            .get();

    assertThat(result.get("client_id").asText()).isEqualTo("client123");
    assertThat(result.get("client_secret").asText()).isEqualTo("secret456");
  }

  @Test
  public void shouldHandleRegistrationError() {
    server.enqueue(
        new MockResponse().setResponseCode(400).setBody("{\"error\":\"invalid_redirect_uri\"}"));

    assertThrows(
        ExecutionException.class,
        () ->
            registrationService
                .registerClientAsync(
                    server.url("/register").toString(), Collections.emptyList(), "Bad App", null)
                .get());
  }
}
