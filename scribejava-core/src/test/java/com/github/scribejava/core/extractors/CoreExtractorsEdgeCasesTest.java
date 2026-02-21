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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.github.scribejava.core.model.DeviceAuthorization;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuth2AccessTokenErrorResponse;
import com.github.scribejava.core.model.Response;
import java.io.IOException;
import java.util.Collections;
import org.junit.Test;

public class CoreExtractorsEdgeCasesTest {

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectEmptyResponseInAccessTokenExtractor() throws IOException {
    final Response response = new Response(200, "OK", Collections.emptyMap(), "");
    OAuth2AccessTokenJsonExtractor.instance().extract(response);
  }

  @Test
  public void shouldExtractAccessTokenWithMissingOptionalParams() throws IOException {
    final String json = "{\"access_token\":\"token123\"}";
    final Response response = new Response(200, "OK", Collections.emptyMap(), json);
    final OAuth2AccessToken token = OAuth2AccessTokenJsonExtractor.instance().extract(response);
    assertEquals("token123", token.getAccessToken());
    assertNull(token.getExpiresIn());
  }

  @Test(expected = OAuth2AccessTokenErrorResponse.class)
  public void shouldHandleOAuth2ErrorResponse() throws IOException {
    final String json = "{\"error\":\"invalid_request\", \"error_description\":\"bad stuff\"}";
    final Response response = new Response(400, "Bad Request", Collections.emptyMap(), json);
    OAuth2AccessTokenJsonExtractor.instance().extract(response);
  }

  @Test
  public void shouldExtractDeviceAuthorization() throws IOException {
    final String json =
        "{"
            + "\"device_code\":\"dc123\","
            + "\"user_code\":\"uc456\","
            + "\"verification_uri\":\"https://uri\","
            + "\"expires_in\":600,"
            + "\"interval\":5"
            + "}";
    final Response response = new Response(200, "OK", Collections.emptyMap(), json);
    final DeviceAuthorization auth = DeviceAuthorizationJsonExtractor.instance().extract(response);
    assertEquals("dc123", auth.getDeviceCode());
    assertEquals(5, auth.getIntervalSeconds());
  }

  @Test(expected = com.github.scribejava.core.exceptions.OAuthException.class)
  public void shouldThrowExceptionWhenRequiredParamMissingInDeviceAuth() throws IOException {
    final String json = "{\"device_code\":\"dc123\"}";
    final Response response = new Response(200, "OK", Collections.emptyMap(), json);
    DeviceAuthorizationJsonExtractor.instance().extract(response);
  }
}
