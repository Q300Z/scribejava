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
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthAsyncRequestCallback;
import com.github.scribejava.core.model.PushedAuthorizationResponse;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests des fonctionnalités asynchrones de {@link OAuth20Service}. */
public class OAuth20ServiceAsyncTest {

  private MockWebServer server;
  private OAuth20Service service;

  /**
   * Initialisation du serveur de simulation et du service.
   *
   * @throws IOException en cas d'erreur.
   */
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
          public String getRevokeTokenEndpoint() {
            return server.url("/revoke").toString();
          }

          @Override
          public String getPushedAuthorizationRequestEndpoint() {
            return server.url("/par").toString();
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

  /**
   * Arrêt du serveur.
   *
   * @throws IOException en cas d'erreur.
   */
  @AfterEach
  public void tearDown() throws IOException {
    server.shutdown();
  }

  /** Vérifie l'obtention asynchrone d'un jeton d'accès. */
  @Test
  public void shouldGetAccessTokenAsync() throws Exception {
    server.enqueue(new MockResponse().setBody("{\"access_token\":\"at123\"}").setResponseCode(200));
    final OAuth2AccessToken token =
        service
            .getAccessToken("code123", (OAuthAsyncRequestCallback<OAuth2AccessToken>) null)
            .get();
    assertThat(token.getAccessToken()).isEqualTo("at123");
  }

  /** Vérifie le renouvellement asynchrone d'un jeton d'accès. */
  @Test
  public void shouldRefreshAccessTokenAsync() throws Exception {
    server.enqueue(new MockResponse().setBody("{\"access_token\":\"at456\"}").setResponseCode(200));
    final OAuth2AccessToken token =
        service
            .refreshAccessToken("rt123", (OAuthAsyncRequestCallback<OAuth2AccessToken>) null)
            .get();
    assertThat(token.getAccessToken()).isEqualTo("at456");
  }

  /** Vérifie la révocation asynchrone d'un jeton. */
  @Test
  public void shouldRevokeTokenAsync() throws Exception {
    server.enqueue(new MockResponse().setResponseCode(200));
    service.revokeTokenAsync("at123").get();
  }

  /** Vérifie l'envoi asynchrone d'une requête PAR. */
  @Test
  public void shouldPushAuthorizationRequestAsync() throws Exception {
    server.enqueue(
        new MockResponse()
            .setBody("{\"request_uri\":\"urn:par:123\", \"expires_in\":90}")
            .setResponseCode(201));
    final PushedAuthorizationResponse resp =
        service
            .pushAuthorizationRequestAsync("code", "api-key", "callback", "scope", "state", null)
            .get();
    assertThat(resp.getRequestUri()).isEqualTo("urn:par:123");
  }

  /** Vérifie la gestion d'erreur lors d'une requête asynchrone. */
  @Test
  public void shouldHandleErrorInAsyncRequest() throws Exception {
    server.enqueue(new MockResponse().setResponseCode(500).setBody("Internal Server Error"));
    assertThrows(
        ExecutionException.class,
        () ->
            service
                .getAccessToken("code123", (OAuthAsyncRequestCallback<OAuth2AccessToken>) null)
                .get());
  }
}
