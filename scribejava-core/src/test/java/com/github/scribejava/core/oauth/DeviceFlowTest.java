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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.httpclient.jdk.JDKHttpClient;
import com.github.scribejava.core.model.DeviceAuthorization;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuth2AccessTokenErrorResponse;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DeviceFlowTest {

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

          @Override
          public String getDeviceAuthorizationEndpoint() {
            return server.url("/device").toString();
          }
        };

    service =
        new OAuth20Service(
            api,
            "api-key",
            "api-secret",
            "callback",
            "scope",
            "code",
            null,
            null,
            null,
            new JDKHttpClient());
  }

  @AfterEach
  public void tearDown() throws IOException {
    server.shutdown();
  }

  @Test
  public void shouldExtractDeviceAuthorization() throws Exception {
    final String body =
        "{\"device_code\":\"dev123\", \"user_code\":\"user456\", "
            + "\"verification_uri\":\"http://v.com\", \"expires_in\":600}";
    server.enqueue(new MockResponse().setBody(body).setResponseCode(200));

    final DeviceAuthorization auth = service.getDeviceAuthorizationCodes();
    assertThat(auth.getDeviceCode()).isEqualTo("dev123");
    assertThat(auth.getUserCode()).isEqualTo("user456");
    assertThat(auth.getVerificationUri()).isEqualTo("http://v.com");
    assertThat(auth.getExpiresInSeconds()).isEqualTo(600);
  }

  @Test
  public void shouldPollForToken() throws Exception {
    final DeviceAuthorization auth =
        new DeviceAuthorization("dev123", "user456", "http://v.com", 600);
    auth.setIntervalSeconds(0);

    // First attempt: pending
    server.enqueue(
        new MockResponse()
            .setResponseCode(400)
            .setBody(
                "{\"error\":\"authorization_pending\", \"error_description\":\"still waiting\"}"));
    // Second attempt: slow_down
    server.enqueue(new MockResponse().setResponseCode(400).setBody("{\"error\":\"slow_down\"}"));
    // Third attempt: success
    server.enqueue(new MockResponse().setResponseCode(200).setBody("{\"access_token\":\"at789\"}"));

    final OAuth2AccessToken token = service.pollAccessTokenDeviceAuthorizationGrant(auth);
    assertThat(token.getAccessToken()).isEqualTo("at789");
    assertThat(server.getRequestCount()).isEqualTo(3);
  }

  @Test
  public void shouldThrowExceptionOnTerminalErrorDuringPolling() throws Exception {
    final DeviceAuthorization auth =
        new DeviceAuthorization("dev123", "user456", "http://v.com", 600);
    auth.setIntervalSeconds(1);

    server.enqueue(
        new MockResponse().setResponseCode(400).setBody("{\"error\":\"access_denied\"}"));

    assertThrows(
        OAuth2AccessTokenErrorResponse.class,
        () -> service.pollAccessTokenDeviceAuthorizationGrant(auth));
  }
}
