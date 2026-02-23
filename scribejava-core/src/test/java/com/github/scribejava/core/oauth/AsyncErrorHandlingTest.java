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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth2.clientauthentication.HttpBasicAuthenticationScheme;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Tests de la gestion des erreurs asynchrones. */
@ExtendWith(MockitoExtension.class)
public class AsyncErrorHandlingTest {

  @Mock private HttpClient httpClient;
  @Mock private DefaultApi20 api;

  private OAuth20Service service;

  /** Initialisation des mocks. */
  @BeforeEach
  public void setUp() {
    when(api.getAccessTokenVerb()).thenReturn(Verb.POST);
    when(api.getAccessTokenEndpoint()).thenReturn("http://example.com/token");
    when(api.getClientAuthentication()).thenReturn(HttpBasicAuthenticationScheme.instance());
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
            httpClient);
  }

  /** Vérifie la gestion d'une exception d'E/S réseau lors d'un appel asynchrone. */
  @Test
  public void shouldHandleNetworkIOExceptionAsync() {
    final IOException networkError = new IOException("Connection Reset");
    final CompletableFuture<OAuth2AccessToken> failedFuture = new CompletableFuture<>();
    failedFuture.completeExceptionally(networkError);

    doReturn(failedFuture)
        .when(httpClient)
        .executeAsync(
            any(),
            any(),
            eq(Verb.POST),
            eq("http://example.com/token"),
            any(byte[].class),
            any(),
            any());

    final CompletableFuture<OAuth2AccessToken> resultFuture =
        service.getAccessTokenAsync("code123");

    final ExecutionException ex = assertThrows(ExecutionException.class, resultFuture::get);
    assertThat(ex.getCause()).isSameAs(networkError);
  }

  /** Vérifie la propagation des exceptions dans le bloc 'exceptionally' du futur. */
  @Test
  public void shouldPropagateExceptionInExceptionallyBlock() {
    final IOException networkError = new IOException("Timeout");
    final CompletableFuture<OAuth2AccessToken> failedFuture = new CompletableFuture<>();
    failedFuture.completeExceptionally(networkError);

    doReturn(failedFuture)
        .when(httpClient)
        .executeAsync(
            any(),
            any(),
            eq(Verb.POST),
            eq("http://example.com/token"),
            any(byte[].class),
            any(),
            any());

    final CompletableFuture<String> handledFuture =
        service
            .getAccessTokenAsync("code123")
            .thenApply(OAuth2AccessToken::getAccessToken)
            .exceptionally(
                ex -> {
                  assertThat(ex).isInstanceOf(CompletionException.class);
                  assertThat(ex.getCause()).isSameAs(networkError);
                  return "error-handled";
                });

    assertThat(handledFuture.join()).isEqualTo("error-handled");
  }

  /** Vérifie la gestion d'une erreur d'extraction lors d'un appel asynchrone. */
  @Test
  public void shouldHandleExtractorErrorAsync() {
    final IOException extractorError = new IOException("Invalid Token");

    final CompletableFuture<OAuth2AccessToken> failedFuture = new CompletableFuture<>();
    failedFuture.completeExceptionally(extractorError);

    doReturn(failedFuture)
        .when(httpClient)
        .executeAsync(
            any(),
            any(),
            eq(Verb.POST),
            eq("http://example.com/token"),
            any(byte[].class),
            any(),
            any());

    final CompletableFuture<OAuth2AccessToken> resultFuture =
        service.getAccessTokenAsync("code123");

    final ExecutionException ex = assertThrows(ExecutionException.class, resultFuture::get);
    assertThat(ex.getCause()).isSameAs(extractorError);
  }
}
