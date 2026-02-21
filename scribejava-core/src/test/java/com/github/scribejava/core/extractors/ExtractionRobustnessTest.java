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
package com.github.scribejava.core.extractors;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.scribejava.core.model.OAuth2AccessTokenErrorResponse;
import com.github.scribejava.core.model.Response;
import java.io.IOException;
import org.junit.jupiter.api.Test;

public class ExtractionRobustnessTest {

  @Test
  public void shouldThrowErrorResponseOnMalformedJson() throws IOException {
    final OAuth2AccessTokenJsonExtractor extractor = OAuth2AccessTokenJsonExtractor.instance();
    final Response response = mock(Response.class);
    when(response.getBody()).thenReturn("{ invalid json : }");

    // ScribeJava v9 encapsulates parsing errors in OAuth2AccessTokenErrorResponse
    assertThatThrownBy(() -> extractor.extract(response))
        .isInstanceOf(OAuth2AccessTokenErrorResponse.class);
  }

  @Test
  public void shouldThrowErrorResponseOnMissingRequiredField() throws IOException {
    final OAuth2AccessTokenJsonExtractor extractor = OAuth2AccessTokenJsonExtractor.instance();
    final Response response = mock(Response.class);
    when(response.getBody()).thenReturn("{\"expires_in\": 3600}");

    assertThatThrownBy(() -> extractor.extract(response))
        .isInstanceOf(OAuth2AccessTokenErrorResponse.class);
  }

  @Test
  public void shouldThrowIllegalArgumentExceptionOnEmptyResponse() throws IOException {
    final OAuth2AccessTokenJsonExtractor extractor = OAuth2AccessTokenJsonExtractor.instance();
    final Response response = mock(Response.class);
    when(response.getBody()).thenReturn("");

    // Triggered by Preconditions.checkEmptyString
    assertThatThrownBy(() -> extractor.extract(response))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(
            "Response body is incorrect. Can't extract a token from an empty string");
  }

  @Test
  public void shouldHandleDeviceAuthMalformedJson() throws IOException {
    final DeviceAuthorizationJsonExtractor extractor = DeviceAuthorizationJsonExtractor.instance();
    final Response response = mock(Response.class);
    when(response.getBody()).thenReturn("not a json");

    // Device auth still uses OAuth2AccessTokenErrorResponse or similar for consistency
    assertThatThrownBy(() -> extractor.extract(response))
        .isInstanceOf(OAuth2AccessTokenErrorResponse.class);
  }
}
