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

import com.github.scribejava.core.oauth2.OAuth2Error;
import java.io.IOException;
import java.net.URI;

/**
 * Representing <a href="https://tools.ietf.org/html/rfc6749#section-5.2">"5.2. Error Response"</a>
 */
public class OAuth2AccessTokenErrorResponse extends OAuthResponseException {

  private static final long serialVersionUID = 2309424849700276816L;

  private final OAuth2Error error;
  private final String errorDescription;
  private final URI errorUri;

  public OAuth2AccessTokenErrorResponse(
      OAuth2Error error, String errorDescription, URI errorUri, Response rawResponse)
      throws IOException {
    super(rawResponse);
    this.error = error;
    this.errorDescription = errorDescription;
    this.errorUri = errorUri;
  }

  public OAuth2Error getError() {
    return error;
  }

  public String getErrorDescription() {
    return errorDescription;
  }

  public URI getErrorUri() {
    return errorUri;
  }
}
