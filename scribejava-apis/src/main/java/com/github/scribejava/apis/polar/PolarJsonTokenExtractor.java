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
package com.github.scribejava.apis.polar;

import com.github.scribejava.core.extractors.OAuth2AccessTokenJsonExtractor;
import com.github.scribejava.core.model.OAuth2AccessTokenErrorResponse;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.oauth2.OAuth2Error;
import com.github.scribejava.core.utils.JsonUtils;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/** Extracteur JSON pour les jetons Polar. */
public class PolarJsonTokenExtractor extends OAuth2AccessTokenJsonExtractor {

  protected PolarJsonTokenExtractor() {}

  public static PolarJsonTokenExtractor instance() {
    return InstanceHolder.INSTANCE;
  }

  @Override
  protected PolarOAuth2AccessToken createToken(
      String accessToken,
      String tokenType,
      Integer expiresIn,
      String refreshToken,
      String scope,
      Map<String, Object> response,
      String rawResponse) {
    return new PolarOAuth2AccessToken(
        accessToken,
        tokenType,
        expiresIn,
        refreshToken,
        scope,
        (String) response.get("x_user_id"),
        rawResponse);
  }

  @Override
  public void generateError(Response response) throws IOException {
    final String body = response.getBody();
    final Map<String, Object> responseMap = JsonUtils.parse(body);
    final Object errors = responseMap.get("errors");

    Map<String, Object> errorNode = null;
    if (errors instanceof List && !((List<?>) errors).isEmpty()) {
      errorNode = (Map<String, Object>) ((List<?>) errors).get(0);
    }

    if (errorNode == null) {
      throw new OAuth2AccessTokenErrorResponse(null, null, null, response);
    }

    final String errorType = (String) errorNode.get("errorType");
    OAuth2Error errorCode;
    try {
      errorCode = OAuth2Error.parseFrom(errorType);
    } catch (IllegalArgumentException iaE) {
      errorCode = null;
    }

    throw new OAuth2AccessTokenErrorResponse(
        errorCode, (String) errorNode.get("message"), null, response);
  }

  private static class InstanceHolder {
    private static final PolarJsonTokenExtractor INSTANCE = new PolarJsonTokenExtractor();
  }
}
