package com.github.scribejava.core.oauth;

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth2.clientauthentication.HttpBasicAuthenticationScheme;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AsyncErrorHandlingTest {

    @Mock
    private HttpClient httpClient;
    @Mock
    private DefaultApi20 api;

    private OAuth20Service service;

    @BeforeEach
    public void setUp() {
        when(api.getAccessTokenVerb()).thenReturn(Verb.POST);
        when(api.getAccessTokenEndpoint()).thenReturn("http://example.com/token");
        when(api.getClientAuthentication()).thenReturn(HttpBasicAuthenticationScheme.instance());
        service = new OAuth20Service(api, "api-key", "api-secret", "callback", "scope", "code", null, null, null,
                httpClient);
    }

    @Test
    public void shouldHandleNetworkIOExceptionAsync() {
        final IOException networkError = new IOException("Connection Reset");
        final CompletableFuture<OAuth2AccessToken> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(networkError);

        doReturn(failedFuture).when(httpClient).executeAsync(any(), any(), eq(Verb.POST),
                eq("http://example.com/token"), any(byte[].class), any(), any());

        final CompletableFuture<OAuth2AccessToken> resultFuture = service.getAccessTokenAsync("code123");

        final ExecutionException ex = assertThrows(ExecutionException.class, resultFuture::get);
        assertThat(ex.getCause()).isSameAs(networkError);
    }

    @Test
    public void shouldPropagateExceptionInExceptionallyBlock() {
        final IOException networkError = new IOException("Timeout");
        final CompletableFuture<OAuth2AccessToken> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(networkError);

        doReturn(failedFuture).when(httpClient).executeAsync(any(), any(), eq(Verb.POST),
                eq("http://example.com/token"), any(byte[].class), any(), any());

        final CompletableFuture<String> handledFuture = service.getAccessTokenAsync("code123")
                .thenApply(OAuth2AccessToken::getAccessToken)
                .exceptionally(ex -> {
                    assertThat(ex).isInstanceOf(CompletionException.class);
                    assertThat(ex.getCause()).isSameAs(networkError);
                    return "error-handled";
                });

        assertThat(handledFuture.join()).isEqualTo("error-handled");
    }

    @Test
    public void shouldHandleExtractorErrorAsync() throws IOException {
        final IOException extractorError = new IOException("Invalid Token");

        final CompletableFuture<OAuth2AccessToken> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(extractorError);

        doReturn(failedFuture).when(httpClient).executeAsync(any(), any(), eq(Verb.POST),
                eq("http://example.com/token"), any(byte[].class), any(), any());

        final CompletableFuture<OAuth2AccessToken> resultFuture = service.getAccessTokenAsync("code123");

        final ExecutionException ex = assertThrows(ExecutionException.class, resultFuture::get);
        assertThat(ex.getCause()).isSameAs(extractorError);
    }
}
