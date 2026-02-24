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
package com.github.scribejava.core.httpclient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.github.scribejava.core.httpclient.multipart.MultipartPayload;
import com.github.scribejava.core.model.OAuthAsyncRequestCallback;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;

/** Test du client HTTP asynchrone de base. */
class AbstractAsyncOnlyHttpClientTest {

  /**
   * @throws InterruptedException InterruptedException
   * @throws ExecutionException ExecutionException
   * @throws IOException IOException
   */
  @Test
  void shouldBlockOnAsyncExecute() throws InterruptedException, ExecutionException, IOException {
    Response mockResponse = mock(Response.class);
    TestAsyncClient client = new TestAsyncClient(mockResponse);

    Response response = client.execute("ua", Collections.emptyMap(), Verb.GET, "url", "body");

    assertThat(response).isEqualTo(mockResponse);
  }

  private static class TestAsyncClient extends AbstractAsyncOnlyHttpClient {
    private final Response response;

    TestAsyncClient(Response response) {
      this.response = response;
    }

    @Override
    public void close() throws IOException {}

    @Override
    public <T> CompletableFuture<T> executeAsync(
        String userAgent,
        Map<String, String> headers,
        Verb httpVerb,
        String completeUrl,
        byte[] bodyContents,
        OAuthAsyncRequestCallback<T> callback,
        OAuthRequest.ResponseConverter<T> converter) {
      return (CompletableFuture<T>) CompletableFuture.completedFuture(response);
    }

    @Override
    public <T> CompletableFuture<T> executeAsync(
        String userAgent,
        Map<String, String> headers,
        Verb httpVerb,
        String completeUrl,
        MultipartPayload bodyContents,
        OAuthAsyncRequestCallback<T> callback,
        OAuthRequest.ResponseConverter<T> converter) {
      return (CompletableFuture<T>) CompletableFuture.completedFuture(response);
    }

    @Override
    public <T> CompletableFuture<T> executeAsync(
        String userAgent,
        Map<String, String> headers,
        Verb httpVerb,
        String completeUrl,
        String bodyContents,
        OAuthAsyncRequestCallback<T> callback,
        OAuthRequest.ResponseConverter<T> converter) {
      return (CompletableFuture<T>) CompletableFuture.completedFuture(response);
    }

    @Override
    public <T> CompletableFuture<T> executeAsync(
        String userAgent,
        Map<String, String> headers,
        Verb httpVerb,
        String completeUrl,
        File bodyContents,
        OAuthAsyncRequestCallback<T> callback,
        OAuthRequest.ResponseConverter<T> converter) {
      return (CompletableFuture<T>) CompletableFuture.completedFuture(response);
    }
  }
}
