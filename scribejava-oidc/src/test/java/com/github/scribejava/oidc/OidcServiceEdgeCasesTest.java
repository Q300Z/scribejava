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

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.httpclient.jdk.JDKHttpClient;
import com.github.scribejava.core.model.OAuth2AccessToken;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OidcServiceEdgeCasesTest {

  private MockWebServer server;
  private OidcService service;

  @BeforeEach
  public void setUp() throws IOException {
    server = new MockWebServer();
    server.start();

    final OidcProviderMetadata metadata =
        new OidcProviderMetadata(
            server.url("/").toString(),
            server.url("/auth").toString(),
            server.url("/token").toString(),
            server.url("/jwks").toString(),
            null,
            null,
            null,
            server.url("/userinfo").toString(),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);

    final DefaultOidcApi20 api = new DefaultOidcApi20() {};
    api.setMetadata(metadata);

    service =
        new OidcService(
            api,
            "client-id",
            "secret",
            "callback",
            null,
            "code",
            null,
            null,
            null,
            new JDKHttpClient(),
            null);
  }

  @AfterEach
  public void tearDown() throws IOException {
    server.shutdown();
  }

  @Test
  public void shouldHandleNon200UserInfoResponse() {
    server.enqueue(new MockResponse().setResponseCode(401).setBody("Unauthorized"));
    assertThrows(
        ExecutionException.class,
        () -> service.getUserInfoAsync(new OAuth2AccessToken("token")).get());
  }

  @Test
  public void shouldHandleMalformedUserInfoJson() {
    server.enqueue(new MockResponse().setResponseCode(200).setBody("not a json"));
    assertThrows(
        ExecutionException.class,
        () -> service.getUserInfoAsync(new OAuth2AccessToken("token")).get());
  }

  @Test
  public void shouldThrowExceptionIfUserInfoEndpointMissing() {
    final DefaultOidcApi20 api = new DefaultOidcApi20() {};
    // Metadata with NO userinfo_endpoint
    api.setMetadata(
        new OidcProviderMetadata(
            "iss", "auth", "token", "jwks", null, null, null, null, null, null, null, null, null,
            null, null, null));

    final OidcService incompleteService =
        new OidcService(
            api, "id", "secret", "cb", null, "code", null, null, null, new JDKHttpClient(), null);
    assertThrows(
        OAuthException.class,
        () -> incompleteService.getUserInfoAsync(new OAuth2AccessToken("token")));
  }
}
