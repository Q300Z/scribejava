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
package com.github.scribejava.httpclient.okhttp;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.scribejava.core.model.Verb;
import java.io.IOException;
import java.util.Collections;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OkHttpHttpClientIntegrationTest {

  private MockWebServer server;
  private OkHttpHttpClient client;

  @BeforeEach
  public void setUp() throws IOException {
    server = new MockWebServer();
    server.start();
    client = new OkHttpHttpClient();
  }

  @AfterEach
  public void tearDown() throws IOException {
    server.shutdown();
  }

  @Test
  public void shouldExecuteAsync() throws Exception {
    server.enqueue(new MockResponse().setResponseCode(200).setBody("OK"));
    final String result =
        client
            .executeAsync(
                "UA",
                Collections.emptyMap(),
                Verb.GET,
                server.url("/").toString(),
                (byte[]) null,
                null,
                response -> response.getBody())
            .get();
    assertThat(result).isEqualTo("OK");
  }

  @Test
  public void shouldCancelAsyncRequest() throws Exception {
    server.enqueue(
        new MockResponse().setBody("OK").setBodyDelay(5, java.util.concurrent.TimeUnit.SECONDS));
    final java.util.concurrent.CompletableFuture<String> future =
        client.executeAsync(
            "UA",
            Collections.emptyMap(),
            Verb.GET,
            server.url("/").toString(),
            (byte[]) null,
            null,
            response -> response.getBody());

    future.cancel(true);
    assertThat(future.isCancelled()).isTrue();
  }
}
