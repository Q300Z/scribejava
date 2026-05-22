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
import static org.mockito.Mockito.*;

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuth2Authorization;
import com.github.scribejava.core.model.OAuthRequest;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests des méthodes utilitaires de {@link OAuth20Service}. */
public class OAuth20ServiceHelpersTest {

  private OAuth20Service service;
  private HttpClient mockHttpClient;

  /** Initialisation du service de test. */
  @BeforeEach
  public void setUp() {
    mockHttpClient = mock(HttpClient.class);
    final DefaultApi20 api =
        new DefaultApi20() {
          @Override
          public String getAccessTokenEndpoint() {
            return "http://test.com/token";
          }

          @Override
          public String getAuthorizationBaseUrl() {
            return "https://test.com/auth";
          }

          @Override
          public String getRevokeTokenEndpoint() {
            return "http://test.com/revoke";
          }
        };
    service =
        new OAuth20Service(
            api, "api-key", "api-secret", "callback", "scope", "code", null, null, null, mockHttpClient);
  }

  /** Vérifie l'extraction des paramètres d'autorisation depuis une URL. */
  @Test
  public void shouldExtractAuthorization() {
    final String url = "http://callback.com?code=auth_code&state=auth_state";
    final OAuth2Authorization auth = service.extractAuthorization(url);
    assertThat(auth.getCode()).isEqualTo("auth_code");
    assertThat(auth.getState()).isEqualTo("auth_state");
  }

  /** Vérifie l'extraction avec présence d'un fragment d'URL. */
  @Test
  public void shouldExtractAuthorizationWithFragment() {
    final String url = "http://callback.com?code=auth_code&state=auth_state#fragment";
    final OAuth2Authorization auth = service.extractAuthorization(url);
    assertThat(auth.getCode()).isEqualTo("auth_code");
    assertThat(auth.getState()).isEqualTo("auth_state");
  }

  /** Vérifie la récupération du type de réponse. */
  @Test
  public void shouldGetResponseType() {
    assertThat(service.getResponseType()).isEqualTo("code");
  }

  /** Vérifie la récupération de la portée par défaut. */
  @Test
  public void shouldGetDefaultScope() {
    assertThat(service.getDefaultScope()).isEqualTo("scope");
  }

  /** Vérifie la récupération de la version. */
  @Test
  public void shouldGetVersion() {
    assertThat(service.getVersion()).isEqualTo("2.0");
  }

  @Test
  public void testDeprecatedGetAccessTokenCode() throws Exception {
    OAuth20Service spyService = spy(service);
    OAuth2AccessToken mockToken = mock(OAuth2AccessToken.class);
    doReturn(CompletableFuture.completedFuture(mockToken))
        .when(spyService).sendAccessTokenRequestAsync(any(), any());

    OAuth2AccessToken result = spyService.getAccessToken("auth_code");
    assertThat(result).isSameAs(mockToken);
  }

  @Test
  public void testDeprecatedGetAccessTokenParams() throws Exception {
    OAuth20Service spyService = spy(service);
    OAuth2AccessToken mockToken = mock(OAuth2AccessToken.class);
    doReturn(CompletableFuture.completedFuture(mockToken))
        .when(spyService).sendAccessTokenRequestAsync(any(), any());

    AccessTokenRequestParams params = new AccessTokenRequestParams("auth_code");
    OAuth2AccessToken result = spyService.getAccessToken(params);
    assertThat(result).isSameAs(mockToken);
  }

  @Test
  public void testDeprecatedGetAccessTokenCodeCallback() throws Exception {
    OAuth20Service spyService = spy(service);
    OAuth2AccessToken mockToken = mock(OAuth2AccessToken.class);
    CompletableFuture<OAuth2AccessToken> future = CompletableFuture.completedFuture(mockToken);
    doReturn(future)
        .when(spyService).sendAccessTokenRequestAsync(any(), any());

    CompletableFuture<OAuth2AccessToken> result = spyService.getAccessToken("auth_code", null);
    assertThat(result.get()).isSameAs(mockToken);
  }

  @Test
  public void testDeprecatedGetAccessTokenClientCredentialsGrantAsync() throws Exception {
    OAuth20Service spyService = spy(service);
    OAuth2AccessToken mockToken = mock(OAuth2AccessToken.class);
    CompletableFuture<OAuth2AccessToken> future = CompletableFuture.completedFuture(mockToken);
    doReturn(future)
        .when(spyService).sendAccessTokenRequestAsync(any(), any());

    CompletableFuture<OAuth2AccessToken> result = spyService.getAccessTokenClientCredentialsGrantAsync();
    assertThat(result.get()).isSameAs(mockToken);
  }

  @Test
  public void testDeprecatedGetAccessTokenPasswordGrantAsync() throws Exception {
    OAuth20Service spyService = spy(service);
    OAuth2AccessToken mockToken = mock(OAuth2AccessToken.class);
    CompletableFuture<OAuth2AccessToken> future = CompletableFuture.completedFuture(mockToken);
    doReturn(future)
        .when(spyService).sendAccessTokenRequestAsync(any(), any());

    CompletableFuture<OAuth2AccessToken> result = spyService.getAccessTokenPasswordGrantAsync("user", "pass");
    assertThat(result.get()).isSameAs(mockToken);
  }

  @Test
  public void testDeprecatedGetAccessTokenPasswordGrant() throws Exception {
    OAuth20Service spyService = spy(service);
    OAuth2AccessToken mockToken = mock(OAuth2AccessToken.class);
    CompletableFuture<OAuth2AccessToken> future = CompletableFuture.completedFuture(mockToken);
    doReturn(future)
        .when(spyService).sendAccessTokenRequestAsync(any(), any());

    OAuth2AccessToken result = spyService.getAccessTokenPasswordGrant("user", "pass");
    assertThat(result).isSameAs(mockToken);
  }

  @Test
  public void testDeprecatedGetAccessTokenClientCredentialsGrant() throws Exception {
    OAuth20Service spyService = spy(service);
    OAuth2AccessToken mockToken = mock(OAuth2AccessToken.class);
    CompletableFuture<OAuth2AccessToken> future = CompletableFuture.completedFuture(mockToken);
    doReturn(future)
        .when(spyService).sendAccessTokenRequestAsync(any(), any());

    OAuth2AccessToken result = spyService.getAccessTokenClientCredentialsGrant();
    assertThat(result).isSameAs(mockToken);
  }

  @Test
  public void testDeprecatedRefreshAccessTokenCallback() throws Exception {
    OAuth20Service spyService = spy(service);
    OAuth2AccessToken mockToken = mock(OAuth2AccessToken.class);
    CompletableFuture<OAuth2AccessToken> future = CompletableFuture.completedFuture(mockToken);
    doReturn(future)
        .when(spyService).refreshAccessTokenAsync(anyString(), any(), any());

    CompletableFuture<OAuth2AccessToken> result = spyService.refreshAccessToken("refresh", null);
    assertThat(result.get()).isSameAs(mockToken);
  }

  @Test
  public void testDeprecatedRevokeToken() throws Exception {
    OAuth20Service spyService = spy(service);
    doNothing().when(spyService).revokeToken(anyString(), any());
    spyService.revokeToken("token");
    verify(spyService).revokeToken("token", null);
  }

  @Test
  public void testDeprecatedRevokeTokenAsync() throws Exception {
    CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
    doReturn(future)
        .when(mockHttpClient)
        .executeAsync(any(), any(), any(), any(), any(byte[].class), any(), any());

    CompletableFuture<Void> result = service.revokeTokenAsync("token");
    assertThat(result.get()).isNull();
  }

  @Test
  public void testHandleExecutionExceptionIOException() {
    OAuth20Service spyService = spy(service);
    CompletableFuture<OAuth2AccessToken> future = new CompletableFuture<>();
    future.completeExceptionally(new IOException("Network error"));
    doReturn(future).when(spyService).sendAccessTokenRequestAsync(any(), any());

    org.junit.jupiter.api.Assertions.assertThrows(IOException.class, () -> spyService.getAccessToken("auth_code"));
  }

  @Test
  public void testHandleExecutionExceptionRuntimeException() {
    OAuth20Service spyService = spy(service);
    CompletableFuture<OAuth2AccessToken> future = new CompletableFuture<>();
    future.completeExceptionally(new IllegalArgumentException("Invalid state"));
    doReturn(future).when(spyService).sendAccessTokenRequestAsync(any(), any());

    org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> spyService.getAccessToken("auth_code"));
  }

  @Test
  public void testHandleExecutionExceptionOther() {
    OAuth20Service spyService = spy(service);
    CompletableFuture<OAuth2AccessToken> future = new CompletableFuture<>();
    Exception checkException = new Exception("Check exception");
    future.completeExceptionally(checkException);
    doReturn(future).when(spyService).sendAccessTokenRequestAsync(any(), any());

    org.junit.jupiter.api.Assertions.assertThrows(ExecutionException.class, () -> spyService.getAccessToken("auth_code"));
  }

  @Test
  public void testCreateGrantRequests() {
    OAuthRequest request1 = service.createAccessTokenPasswordGrantRequest();
    assertThat(request1.getBodyParams().asMap().get("grant_type")).isEqualTo("password");
    assertThat(request1.getBodyParams().asMap().get("username")).isEqualTo("user");
    assertThat(request1.getBodyParams().asMap().get("password")).isEqualTo("pass");
    assertThat(request1.getBodyParams().asMap().get("scope")).isEqualTo("scope1");

    OAuthRequest request2 = service.createAccessTokenClientCredentialsGrantRequest();
    assertThat(request2.getBodyParams().asMap().get("grant_type")).isEqualTo("client_credentials");
    assertThat(request2.getBodyParams().asMap().get("scope")).isEqualTo("scope2");
  }
}
