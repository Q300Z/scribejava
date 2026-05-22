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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.github.scribejava.core.exceptions.OAuthNetworkException;
import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;

/** Tests complets pour la classe abstraite {@link OAuthService}. */
public class OAuthServiceComprehensiveTest {

  private static class TestOAuthService extends OAuthService {
    TestOAuthService(
        String apiKey,
        String apiSecret,
        String callback,
        java.io.OutputStream debugStream,
        String userAgent,
        com.github.scribejava.core.httpclient.HttpClientConfig httpClientConfig,
        HttpClient httpClient) {
      super(apiKey, apiSecret, callback, debugStream, userAgent, httpClientConfig, httpClient);
    }
  }

  @Test
  public void testSimpleProperties() throws Exception {
    try (TestOAuthService service =
        new TestOAuthService("key", "secret", "callback", null, "UA", null, null)) {
      assertThat(service.getApiKey()).isEqualTo("key");
      assertThat(service.getApiSecret()).isEqualTo("secret");
      assertThat(service.getCallback()).isEqualTo("callback");
      assertThat(service.isDebug()).isFalse();
    }
  }

  @Test
  public void testDebugLoggingAndIsDebug() {
    final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    final TestOAuthService service =
        new TestOAuthService("key", "secret", "callback", bos, "UA", null, null);
    assertThat(service.isDebug()).isTrue();
    service.log("Hello %s", "world");
    service.log("Simple message");
    final String output = bos.toString();
    assertThat(output).contains("Hello world");
    assertThat(output).contains("Simple message");

    service.log("Pattern", (Object[]) null);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testInterceptorsAndLogging() throws Exception {
    final HttpClient mockClient = mock(HttpClient.class);
    final OAuthRequest request = new OAuthRequest(Verb.GET, "http://test.com");
    final Response mockResponse = mock(Response.class);

    when(mockClient.execute(anyString(), any(), any(), anyString(), any(byte[].class)))
        .thenReturn(mockResponse);

    final TestOAuthService service =
        new TestOAuthService("key", "secret", "callback", null, "UA", null, mockClient);

    final OAuthRequestInterceptor interceptor = mock(OAuthRequestInterceptor.class);
    service.execute(request);
    verify(interceptor, times(0)).intercept(request);

    final java.lang.reflect.Field interceptorsField =
        OAuthService.class.getDeclaredField("interceptors");
    interceptorsField.setAccessible(true);
    final java.util.List<OAuthRequestInterceptor> list =
        (java.util.List<OAuthRequestInterceptor>) interceptorsField.get(service);
    list.add(interceptor);

    service.execute(request);
    verify(interceptor, times(1)).intercept(request);
  }

  @Test
  public void testRetryPolicySuccessAndFail() throws Exception {
    final HttpClient mockClient = mock(HttpClient.class);
    final OAuthRequest request = new OAuthRequest(Verb.GET, "http://test.com");
    final Response mockResponse = mock(Response.class);

    when(mockClient.execute(anyString(), any(), any(), anyString(), any(byte[].class)))
        .thenThrow(new IOException("Network error"))
        .thenReturn(mockResponse);

    final TestOAuthService service =
        new TestOAuthService("key", "secret", "callback", null, "UA", null, mockClient);

    final OAuthRetryPolicy retryPolicy = mock(OAuthRetryPolicy.class);
    when(retryPolicy.getMaxAttempts()).thenReturn(3);
    when(retryPolicy.getDelayMs()).thenReturn(10L);
    service.setRetryPolicy(retryPolicy);

    final Response response = service.execute(request);
    assertThat(response).isSameAs(mockResponse);
    verify(retryPolicy, times(1)).getDelayMs();

    reset(mockClient);
    when(mockClient.execute(anyString(), any(), any(), anyString(), any(byte[].class)))
        .thenThrow(new IOException("Permanent error"));

    org.junit.jupiter.api.Assertions.assertThrows(
        OAuthNetworkException.class, () -> service.execute(request));
  }

  @Test
  public void testRateLimitChecking() throws Exception {
    final HttpClient mockClient = mock(HttpClient.class);
    final OAuthRequest request = new OAuthRequest(Verb.GET, "http://test.com");
    final Response mockResponse = mock(Response.class);

    when(mockClient.execute(anyString(), any(), any(), anyString(), any(byte[].class)))
        .thenReturn(mockResponse);
    when(mockResponse.getHeader("X-RateLimit-Remaining")).thenReturn("5");
    when(mockResponse.getHeader("X-RateLimit-Reset")).thenReturn("12345678");

    final TestOAuthService service =
        new TestOAuthService("key", "secret", "callback", null, "UA", null, mockClient);

    final RateLimitListener rateLimitListener = mock(RateLimitListener.class);
    service.setRateLimitListener(rateLimitListener);

    service.execute(request);
    verify(rateLimitListener, times(1)).onRateLimit(eq(5), eq(12345678L), any());
  }

  @Test
  public void testDifferentPayloadTypesSync() throws Exception {
    final HttpClient mockClient = mock(HttpClient.class);
    final Response mockResponse = mock(Response.class);

    final TestOAuthService service =
        new TestOAuthService("key", "secret", "callback", null, "UA", null, mockClient);

    // File Payload
    final OAuthRequest request1 = new OAuthRequest(Verb.GET, "http://test.com");
    final File mockFile = mock(File.class);
    final java.lang.reflect.Field fileField = OAuthRequest.class.getDeclaredField("filePayload");
    fileField.setAccessible(true);
    fileField.set(request1, mockFile);
    when(mockClient.execute(anyString(), any(), any(), anyString(), any(File.class)))
        .thenReturn(mockResponse);
    service.execute(request1);
    verify(mockClient).execute(anyString(), any(), any(), anyString(), eq(mockFile));

    // String Payload
    final OAuthRequest request2 = new OAuthRequest(Verb.GET, "http://test.com");
    request2.setPayload("my-string");
    when(mockClient.execute(anyString(), any(), any(), anyString(), anyString()))
        .thenReturn(mockResponse);
    service.execute(request2);
    verify(mockClient).execute(anyString(), any(), any(), anyString(), eq("my-string"));

    // Multipart Payload
    final OAuthRequest request3 = new OAuthRequest(Verb.GET, "http://test.com");
    final com.github.scribejava.core.httpclient.multipart.MultipartPayload multipartPayload =
        mock(com.github.scribejava.core.httpclient.multipart.MultipartPayload.class);
    final java.lang.reflect.Field multipartField =
        OAuthRequest.class.getDeclaredField("multipartPayload");
    multipartField.setAccessible(true);
    multipartField.set(request3, multipartPayload);
    when(mockClient.execute(
            anyString(),
            any(),
            any(),
            anyString(),
            any(com.github.scribejava.core.httpclient.multipart.MultipartPayload.class)))
        .thenReturn(mockResponse);
    service.execute(request3);
    verify(mockClient).execute(anyString(), any(), any(), anyString(), eq(multipartPayload));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testDifferentPayloadTypesAsync() throws Exception {
    final HttpClient mockClient = mock(HttpClient.class);
    final Response mockResponse = mock(Response.class);
    final CompletableFuture<Response> future = CompletableFuture.completedFuture(mockResponse);

    final TestOAuthService service =
        new TestOAuthService("key", "secret", "callback", null, "UA", null, mockClient);

    // File Payload
    final OAuthRequest request1 = new OAuthRequest(Verb.GET, "http://test.com");
    final File mockFile = mock(File.class);
    final java.lang.reflect.Field fileField = OAuthRequest.class.getDeclaredField("filePayload");
    fileField.setAccessible(true);
    fileField.set(request1, mockFile);
    when(mockClient.executeAsync(
            anyString(), any(), any(), anyString(), any(File.class), any(), any()))
        .thenReturn((CompletableFuture) future);
    service.execute(request1, null).get();
    verify(mockClient)
        .executeAsync(anyString(), any(), any(), anyString(), eq(mockFile), any(), any());

    // String Payload
    final OAuthRequest request2 = new OAuthRequest(Verb.GET, "http://test.com");
    request2.setPayload("my-string");
    when(mockClient.executeAsync(anyString(), any(), any(), anyString(), anyString(), any(), any()))
        .thenReturn((CompletableFuture) future);
    service.execute(request2, null).get();
    verify(mockClient)
        .executeAsync(anyString(), any(), any(), anyString(), eq("my-string"), any(), any());

    // Multipart Payload
    final OAuthRequest request3 = new OAuthRequest(Verb.GET, "http://test.com");
    final com.github.scribejava.core.httpclient.multipart.MultipartPayload multipartPayload =
        mock(com.github.scribejava.core.httpclient.multipart.MultipartPayload.class);
    final java.lang.reflect.Field multipartField =
        OAuthRequest.class.getDeclaredField("multipartPayload");
    multipartField.setAccessible(true);
    multipartField.set(request3, multipartPayload);
    when(mockClient.executeAsync(
            anyString(),
            any(),
            any(),
            anyString(),
            any(com.github.scribejava.core.httpclient.multipart.MultipartPayload.class),
            any(),
            any()))
        .thenReturn((CompletableFuture) future);
    service.execute(request3, null).get();
    verify(mockClient)
        .executeAsync(anyString(), any(), any(), anyString(), eq(multipartPayload), any(), any());
  }
}
