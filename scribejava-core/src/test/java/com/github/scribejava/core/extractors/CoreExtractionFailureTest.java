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

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.model.Response;
import java.util.Collections;
import org.junit.jupiter.api.Test;

public class CoreExtractionFailureTest {

  @Test
  public void shouldFailOnNon200Response() {
    final Response response = new Response(400, "Bad Request", Collections.emptyMap(), "{}");
    assertThrows(
        OAuthException.class, () -> OAuth2AccessTokenExtractor.instance().extract(response));
    assertThrows(
        OAuthException.class, () -> OAuth2AccessTokenJsonExtractor.instance().extract(response));
  }

  @Test
  public void shouldFailOnEmptyBody() {
    final Response response = new Response(200, "OK", Collections.emptyMap(), "");
    assertThrows(
        IllegalArgumentException.class,
        () -> OAuth2AccessTokenExtractor.instance().extract(response));
  }

  @Test
  public void shouldFailOnMissingAccessToken() {
    final Response response = new Response(200, "OK", Collections.emptyMap(), "expires_in=3600");
    assertThrows(
        OAuthException.class, () -> OAuth2AccessTokenExtractor.instance().extract(response));
  }

  @Test
  public void shouldFailOnInvalidJson() {
    final Response response = new Response(200, "OK", Collections.emptyMap(), "{not-json}");
    assertThrows(
        Exception.class, () -> OAuth2AccessTokenJsonExtractor.instance().extract(response));
  }
}
