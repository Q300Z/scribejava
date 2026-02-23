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
package com.github.scribejava.core.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.scribejava.core.exceptions.OAuthProtocolException;
import com.github.scribejava.core.exceptions.OAuthRateLimitException;
import com.github.scribejava.core.model.OAuthResponseException;
import com.github.scribejava.core.model.Response;
import java.io.IOException;
import org.junit.jupiter.api.Test;

public class CoreUtilitiesRobustnessTest {

  @Test
  public void shouldTestPreconditions() {
    // checkNotNull
    assertThatThrownBy(() -> Preconditions.checkNotNull(null, "custom error"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("custom error");

    Preconditions.checkNotNull(new Object(), "ok");

    // checkEmptyString
    assertThatThrownBy(() -> Preconditions.checkEmptyString(null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Received an invalid parameter");

    assertThatThrownBy(() -> Preconditions.checkEmptyString("", "empty"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("empty");

    assertThatThrownBy(() -> Preconditions.checkEmptyString("   ", "whitespace"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("whitespace");

    Preconditions.checkEmptyString("valid", "ok");
  }

  @Test
  public void shouldTestHasTextUtility() {
    assertThat(Preconditions.hasText(null)).isFalse();
    assertThat(Preconditions.hasText("")).isFalse();
    assertThat(Preconditions.hasText("  \n  ")).isFalse();
    assertThat(Preconditions.hasText(" a ")).isTrue();
  }

  @Test
  public void shouldTestExceptionHierarchy() throws IOException {
    final Response mockRateLimitResponse = mock(Response.class);
    when(mockRateLimitResponse.getCode()).thenReturn(429);
    when(mockRateLimitResponse.getBody()).thenReturn("slow down");

    final OAuthRateLimitException rateLimit = new OAuthRateLimitException(mockRateLimitResponse);
    assertThat(rateLimit.getMessage()).contains("Rate limit exceeded").contains("slow down");

    final Response mockResponse = mock(Response.class);
    when(mockResponse.getBody()).thenReturn("error body");

    final OAuthResponseException responseEx = new OAuthResponseException(mockResponse);
    assertThat(responseEx.getResponse()).isEqualTo(mockResponse);
    assertThat(responseEx.getMessage()).isEqualTo("error body");

    final OAuthProtocolException protocol =
        new OAuthProtocolException("Malformed protocol", new RuntimeException("cause"));
    assertThat(protocol.getMessage()).isEqualTo("Malformed protocol");
    assertThat(protocol.getCause()).isInstanceOf(RuntimeException.class);
  }
}
