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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.utils.Preconditions;
import java.io.IOException;

/** Represents the response from a Pushed Authorization Request (PAR) endpoint. */
public class PushedAuthorizationResponse {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final String requestUri;
  private final Long expiresIn;
  private final String rawResponse;

  public PushedAuthorizationResponse(String requestUri, Long expiresIn, String rawResponse) {
    Preconditions.checkEmptyString(requestUri, "request_uri cannot be null or empty");
    this.requestUri = requestUri;
    this.expiresIn = expiresIn;
    this.rawResponse = rawResponse;
  }

  public static PushedAuthorizationResponse parse(String responseBody) throws IOException {
    Preconditions.checkEmptyString(
        responseBody, "Response body is incorrect. Can't parse an empty string.");

    final JsonNode body = OBJECT_MAPPER.readTree(responseBody);
    final JsonNode requestUri = body.get("request_uri");
    final JsonNode expiresIn = body.get("expires_in");

    if (requestUri == null || requestUri.isNull()) {
      throw new OAuthException(
          "Response body is incorrect. Missing 'request_uri' parameter. Raw response: "
              + responseBody);
    }

    return new PushedAuthorizationResponse(
        requestUri.asText(),
        expiresIn == null || expiresIn.isNull() ? null : expiresIn.asLong(),
        responseBody);
  }

  public String getRequestUri() {
    return requestUri;
  }

  public Long getExpiresIn() {
    return expiresIn;
  }

  public String getRawResponse() {
    return rawResponse;
  }
}
