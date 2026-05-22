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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.scribejava.core.model.Response;
import org.junit.jupiter.api.Test;

public class OAuthRetryPolicyTest {

  @Test
  public void testRetryPolicyProperties() {
    final OAuthRetryPolicy policy = new OAuthRetryPolicy(5, 123L);
    assertThat(policy.getMaxAttempts()).isEqualTo(5);
    assertThat(policy.getDelayMs()).isEqualTo(123L);
  }

  @Test
  public void testShouldRetryOnCodes() {
    final OAuthRetryPolicy policy = new OAuthRetryPolicy(3, 10L);

    final Response mockResponse = mock(Response.class);

    // 429 Rate limit should retry
    when(mockResponse.getCode()).thenReturn(429);
    assertThat(policy.shouldRetry(mockResponse)).isTrue();

    // 500 Server error should retry
    when(mockResponse.getCode()).thenReturn(500);
    assertThat(policy.shouldRetry(mockResponse)).isTrue();

    // 503 Server error should retry
    when(mockResponse.getCode()).thenReturn(503);
    assertThat(policy.shouldRetry(mockResponse)).isTrue();

    // 599 Server error should retry
    when(mockResponse.getCode()).thenReturn(599);
    assertThat(policy.shouldRetry(mockResponse)).isTrue();

    // 200 OK should not retry
    when(mockResponse.getCode()).thenReturn(200);
    assertThat(policy.shouldRetry(mockResponse)).isFalse();

    // 400 Bad request should not retry
    when(mockResponse.getCode()).thenReturn(400);
    assertThat(policy.shouldRetry(mockResponse)).isFalse();

    // 401 Unauthorized should not retry
    when(mockResponse.getCode()).thenReturn(401);
    assertThat(policy.shouldRetry(mockResponse)).isFalse();

    // 600 Out of bounds should not retry
    when(mockResponse.getCode()).thenReturn(600);
    assertThat(policy.shouldRetry(mockResponse)).isFalse();
  }
}
