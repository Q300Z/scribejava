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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;

public class OidcRegistrationRobustnessTest {

  @Test
  public void shouldThrowOnRegistrationError()
      throws InterruptedException, ExecutionException, IOException {
    final HttpClient mockClient = mock(HttpClient.class);
    final OidcRegistrationService service = new OidcRegistrationService(mockClient, "UA");

    final Response errorResponse = mock(Response.class);
    when(errorResponse.getCode()).thenReturn(400);
    when(errorResponse.getBody()).thenReturn("{\"error\":\"invalid_redirect_uri\"}");

    // Setup async mock to return error response
    when(mockClient.executeAsync(
            anyString(),
            any(Map.class),
            any(Verb.class),
            anyString(),
            anyString(),
            isNull(),
            any()))
        .thenAnswer(
            invocation -> {
              final com.github.scribejava.core.model.OAuthRequest.ResponseConverter<Object>
                  converter = invocation.getArgument(6);
              try {
                converter.convert(errorResponse);
                final CompletableFuture<Object> future = new CompletableFuture<>();
                future.complete(null);
                return future;
              } catch (OAuthException e) {
                final CompletableFuture<Object> future = new CompletableFuture<>();
                future.completeExceptionally(e);
                return future;
              }
            });

    final CompletableFuture<?> future =
        service.registerClientAsync(
            "http://reg", Collections.singletonList("http://callback"), "client", null);

    assertThatThrownBy(future::get)
        .isInstanceOf(ExecutionException.class)
        .hasCauseInstanceOf(OAuthException.class)
        .hasMessageContaining("Client registration failed. Status: 400");
  }
}
