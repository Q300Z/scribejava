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
package com.github.scribejava.core.model;

import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.utils.JsonUtils;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

/**
 * Pushed Authorization Response
 *
 * @see <a href="https://tools.ietf.org/html/rfc9126#section-2.2">rfc9126</a>
 */
public class PushedAuthorizationResponse implements Serializable {

  private static final long serialVersionUID = 1L;

  private final String requestUri;
  private final Integer expiresIn;

  public PushedAuthorizationResponse(String requestUri, Integer expiresIn) {
    this.requestUri = requestUri;
    this.expiresIn = expiresIn;
  }

  public static PushedAuthorizationResponse parse(String body) throws IOException {
    final Map<String, Object> response = JsonUtils.parse(body);

    final String requestUri = (String) response.get("request_uri");
    if (requestUri == null) {
      throw new OAuthException(
          "Response body is incorrect. Can't extract a 'request_uri' from this: '" + body + "'");
    }

    final Object expiresInObj = response.get("expires_in");
    Integer expiresIn = null;
    if (expiresInObj instanceof Number) {
      expiresIn = ((Number) expiresInObj).intValue();
    } else if (expiresInObj instanceof String) {
      try {
        expiresIn = Integer.parseInt((String) expiresInObj);
      } catch (NumberFormatException e) {
        expiresIn = null;
      }
    }

    return new PushedAuthorizationResponse(requestUri, expiresIn);
  }

  public String getRequestUri() {
    return requestUri;
  }

  public Integer getExpiresIn() {
    return expiresIn;
  }

  @Override
  public String toString() {
    return "PushedAuthorizationResponse{"
        + "'requestUri'='"
        + requestUri
        + "', 'expiresIn'='"
        + expiresIn
        + "'}";
  }
}
