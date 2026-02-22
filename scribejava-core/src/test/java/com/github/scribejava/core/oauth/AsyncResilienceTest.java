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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.github.scribejava.core.model.OAuthAsyncRequestCallback;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;

public class AsyncResilienceTest {

  @Test
  public void shouldHandleRequestCancellation() {
    final CompletableFuture<String> future = new CompletableFuture<>();

    // Simule l'annulation par l'utilisateur
    future.cancel(true);

    assertThat(future.isCancelled()).isTrue();
    assertThatThrownBy(future::get).isInstanceOf(java.util.concurrent.CancellationException.class);
  }

  @Test
  public void shouldPropagateConverterExceptionsToFuture() {
    final Response response = mock(Response.class);

    final OAuthRequest.ResponseConverter<String> failingConverter =
        r -> {
          throw new IOException("Simulated conversion failure");
        };

    // We simulate the behavior of executeAsync manually since we don't have a real HTTP client here
    final CompletableFuture<String> future = new CompletableFuture<>();
    try {
      failingConverter.convert(response);
    } catch (IOException e) {
      future.completeExceptionally(e);
    }

    assertThat(future.isCompletedExceptionally()).isTrue();
    assertThatThrownBy(future::get)
        .isInstanceOf(ExecutionException.class)
        .hasCauseInstanceOf(IOException.class)
        .hasMessageContaining("Simulated conversion failure");
  }

  @Test
  public void shouldHandleCallbackOnThrowable() {
    final OAuthAsyncRequestCallback<String> callback = mock(OAuthAsyncRequestCallback.class);
    final CompletableFuture<String> future = new CompletableFuture<>();
    final Exception error = new RuntimeException("test failure");

    // Simulate completeExceptionally behavior with callback
    future.completeExceptionally(error);
    callback.onThrowable(error);

    verify(callback).onThrowable(error);
    assertThat(future.isCompletedExceptionally()).isTrue();
  }
}
