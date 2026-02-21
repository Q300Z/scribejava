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
package com.github.scribejava.core.oauth;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.httpclient.jdk.JDKHttpClient;
import com.github.scribejava.core.httpclient.jdk.JDKHttpClientConfig;
import com.github.scribejava.core.model.OAuth2AccessTokenErrorResponse;
import java.io.IOException;
import java.net.SocketTimeoutException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OAuth20ProtocolFailureTest {

  private MockWebServer server;
  private OAuth20Service service;

  @BeforeEach
  public void setUp() throws IOException {
    server = new MockWebServer();
    server.start();

    final DefaultApi20 api =
        new DefaultApi20() {
          @Override
          public String getAccessTokenEndpoint() {
            return server.url("/token").toString();
          }

          @Override
          public String getAuthorizationBaseUrl() {
            return server.url("/auth").toString();
          }
        };

    final JDKHttpClientConfig config = JDKHttpClientConfig.defaultConfig();
    config.setConnectTimeout(500);
    config.setReadTimeout(500);

    service =
        new OAuth20Service(
            api,
            "api-key",
            "api-secret",
            "callback",
            null,
            "code",
            null,
            null,
            config,
            new JDKHttpClient(config));
  }

  @AfterEach
  public void tearDown() throws IOException {
    server.shutdown();
  }

  @Test
  public void shouldFailOnOAuthErrorResponse() {
    server.enqueue(
        new MockResponse()
            .setResponseCode(400)
            .setBody("{\"error\":\"invalid_grant\", \"error_description\":\"code expired\"}"));
    assertThrows(OAuth2AccessTokenErrorResponse.class, () -> service.getAccessToken("some-code"));
  }

  @Test
  public void shouldFailOnEmptyCode() {
    // null code throws IllegalArgumentException from Preconditions
    assertThrows(IllegalArgumentException.class, () -> service.getAccessToken((String) null));

    // empty code is NOT checked by Preconditions in OAuth20Service, so it hits the network.
    assertThrows(SocketTimeoutException.class, () -> service.getAccessToken(""));
  }

  @Test
  public void shouldFailOnEmptyRefreshToken() {
    assertThrows(IllegalArgumentException.class, () -> service.refreshAccessToken(null));
    assertThrows(IllegalArgumentException.class, () -> service.refreshAccessToken(""));
  }
}
