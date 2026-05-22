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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.model.OAuthAsyncRequestCallback;
import com.github.scribejava.core.model.OAuthConstants;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.PushedAuthorizationResponse;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.oauth2.clientauthentication.ClientAuthentication;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OAuth20PushedAuthHandlerTest {

  private OAuth20Service service;
  private DefaultApi20 api;
  private OAuth20PushedAuthHandler handler;
  private ClientAuthentication clientAuth;

  @BeforeEach
  public void setUp() {
    service = mock(OAuth20Service.class);
    api = mock(DefaultApi20.class);
    clientAuth = mock(ClientAuthentication.class);
    when(service.getApi()).thenReturn(api);
    when(api.getClientAuthentication()).thenReturn(clientAuth);
    when(service.getAuthorizationRequestConverter()).thenReturn(params -> params);
    when(service.getApiKey()).thenReturn("api-key");
    when(service.getApiSecret()).thenReturn("api-secret");
    handler = new OAuth20PushedAuthHandler(service);
  }

  @Test
  public void shouldCreatePushedAuthorizationRequest() {
    when(api.getPushedAuthorizationRequestEndpoint()).thenReturn("https://test.com/par");

    final Map<String, String> additional = new HashMap<>();
    additional.put("custom_param", "custom_val");

    final OAuthRequest request =
        handler.createPushedAuthorizationRequest(
            "code", "api-key", "https://callback.com", "scope1", "state1", additional);

    assertThat(request.getCompleteUrl()).isEqualTo("https://test.com/par");
    assertThat(request.getBodyParams().asMap().get(OAuthConstants.RESPONSE_TYPE)).isEqualTo("code");
    assertThat(request.getBodyParams().asMap().get(OAuthConstants.CLIENT_ID)).isEqualTo("api-key");
    assertThat(request.getBodyParams().asMap().get(OAuthConstants.REDIRECT_URI))
        .isEqualTo("https://callback.com");
    assertThat(request.getBodyParams().asMap().get(OAuthConstants.SCOPE)).isEqualTo("scope1");
    assertThat(request.getBodyParams().asMap().get(OAuthConstants.STATE)).isEqualTo("state1");
    assertThat(request.getBodyParams().asMap().get("custom_param")).isEqualTo("custom_val");

    verify(clientAuth).addClientAuthentication(request, "api-key", "api-secret");
  }

  @Test
  public void shouldFailExceptionallyWhenParNotSupported() {
    when(api.getPushedAuthorizationRequestEndpoint()).thenReturn(null);

    final CompletableFuture<PushedAuthorizationResponse> future =
        handler.pushAuthorizationRequestAsync(
            "code", "api-key", "callback", "scope", "state", Collections.emptyMap(), null);

    assertThat(future).isCompletedExceptionally();
    assertThatExceptionOfType(ExecutionException.class)
        .isThrownBy(future::get)
        .withCauseInstanceOf(UnsupportedOperationException.class)
        .withMessageContaining("doesn't support Pushed Authorization Requests");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void shouldExecuteAndCacheOnCacheMissAndSucceed() throws Exception {
    when(api.getPushedAuthorizationRequestEndpoint()).thenReturn("https://test.com/par");

    final Response mockResponse = mock(Response.class);
    when(mockResponse.getCode()).thenReturn(201);
    when(mockResponse.getBody()).thenReturn("{\"request_uri\":\"urn:123\", \"expires_in\":60}");

    // Mock service.execute which takes OAuthRequest, callback, and converter
    when(service.execute(any(OAuthRequest.class), any(), any()))
        .thenAnswer(
            invocation -> {
              final OAuthAsyncRequestCallback<PushedAuthorizationResponse> cb =
                  invocation.getArgument(1);
              final OAuthRequest.ResponseConverter<PushedAuthorizationResponse> converter =
                  invocation.getArgument(2);
              final PushedAuthorizationResponse result = converter.convert(mockResponse);
              if (cb != null) {
                cb.onCompleted(result);
              }
              return CompletableFuture.completedFuture(result);
            });

    final OAuthAsyncRequestCallback<PushedAuthorizationResponse> callback =
        mock(OAuthAsyncRequestCallback.class);

    final PushedAuthorizationResponse parResp1 =
        handler
            .pushAuthorizationRequestAsync(
                "code", "api-key", "callback", "scope", "state", Collections.emptyMap(), callback)
            .get();

    assertThat(parResp1.getRequestUri()).isEqualTo("urn:123");
    assertThat(parResp1.getExpiresIn()).isEqualTo(60);

    verify(callback).onCompleted(parResp1);
    // Verified service.execute was called once
    verify(service, times(1)).execute(any(), any(), any());

    // Call again to verify cache hit (service.execute should not be called again)
    final PushedAuthorizationResponse parResp2 =
        handler
            .pushAuthorizationRequestAsync(
                "code", "api-key", "callback", "scope", "state", Collections.emptyMap(), callback)
            .get();

    assertThat(parResp2).isSameAs(parResp1);
    verify(service, times(1)).execute(any(), any(), any());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void shouldThrowOnNon200Or201Response() {
    when(api.getPushedAuthorizationRequestEndpoint()).thenReturn("https://test.com/par");

    final Response mockResponse = mock(Response.class);
    when(mockResponse.getCode()).thenReturn(400);
    try {
      when(mockResponse.getBody()).thenReturn("Error details");
    } catch (IOException e) {
      // Ignored
    }

    when(service.execute(any(OAuthRequest.class), any(), any()))
        .thenAnswer(
            invocation -> {
              final OAuthRequest.ResponseConverter<PushedAuthorizationResponse> converter =
                  invocation.getArgument(2);
              final CompletableFuture<PushedAuthorizationResponse> future =
                  new CompletableFuture<>();
              try {
                converter.convert(mockResponse);
                future.complete(null);
              } catch (Exception e) {
                future.completeExceptionally(e);
              }
              return future;
            });

    final CompletableFuture<PushedAuthorizationResponse> future =
        handler.pushAuthorizationRequestAsync(
            "code", "api-key", "callback", "scope", "state", Collections.emptyMap(), null);

    assertThat(future).isCompletedExceptionally();
    assertThatExceptionOfType(ExecutionException.class)
        .isThrownBy(future::get)
        .withCauseInstanceOf(OAuthException.class)
        .withMessageContaining(
            "Failed to push authorization request. Status: 400, Body: Error details");
  }
}
