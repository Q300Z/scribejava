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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.scribejava.core.model.DeviceAuthorization;
import com.github.scribejava.core.model.Response;
import java.io.IOException;
import org.junit.jupiter.api.Test;

/** Tests pour l'extracteur Device Flow (TDD). */
public class DeviceAuthorizationExtractorTest {

  @Test
  public void shouldExtractDeviceAuthorization() throws IOException {
    final String body =
        "{\"device_code\":\"dev123\", \"user_code\":\"user456\", "
            + "\"verification_uri\":\"https://server.com/verify\", \"expires_in\":1800, \"interval\":5}";

    final Response response = mock(Response.class);
    when(response.getBody()).thenReturn(body);
    when(response.getCode()).thenReturn(200);

    final DeviceAuthorization result =
        DeviceAuthorizationJsonExtractor.instance().extract(response);

    assertThat(result.getDeviceCode()).isEqualTo("dev123");
    assertThat(result.getUserCode()).isEqualTo("user456");
    assertThat(result.getVerificationUri()).isEqualTo("https://server.com/verify");
    assertThat(result.getExpiresInSeconds()).isEqualTo(1800);
    assertThat(result.getIntervalSeconds()).isEqualTo(5);
  }

  @Test
  public void shouldHandleMixedTypesAndMissingFields() throws IOException {
    // expires_in en String, pas d'interval
    final String body =
        "{\"device_code\":\"dev123\", \"user_code\":\"user456\", "
            + "\"verification_uri\":\"https://server.com/verify\", \"expires_in\":\"1800\"}";

    final Response response = mock(Response.class);
    when(response.getBody()).thenReturn(body);
    when(response.getCode()).thenReturn(200);

    final DeviceAuthorization result =
        DeviceAuthorizationJsonExtractor.instance().extract(response);

    assertThat(result.getExpiresInSeconds()).isEqualTo(1800);
    assertThat(result.getIntervalSeconds()).isEqualTo(5); // Default value is 5
  }
}
