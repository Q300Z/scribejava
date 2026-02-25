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

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuth2AccessTokenErrorResponse;
import com.github.scribejava.core.model.OAuthConstants;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.oauth2.OAuth2Error;
import com.github.scribejava.core.utils.JsonUtils;
import com.github.scribejava.core.utils.Preconditions;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Optional;

/** Implémentation JSON native robuste de {@link TokenExtractor} pour OAuth 2.0. */
public class OAuth2AccessTokenJsonExtractor extends AbstractJsonExtractor<OAuth2AccessToken> {

  protected OAuth2AccessTokenJsonExtractor() {}

  public static OAuth2AccessTokenJsonExtractor instance() {
    return InstanceHolder.INSTANCE;
  }

  @Override
  public OAuth2AccessToken extract(Response response) throws IOException {
    final String body = response.getBody();
    Preconditions.checkEmptyString(
        body, "Response body is incorrect. Can't extract a token from an empty string");

    if (response.getCode() != 200) {
      generateError(response);
    }
    return createToken(body);
  }

  /**
   * Analyse la réponse d'erreur.
   *
   * @param response réponse
   * @throws IOException erreur
   */
  public void generateError(Response response) throws IOException {
    final String responseBody = response.getBody();
    final Map<String, Object> responseMap = JsonUtils.parse(responseBody);

    final URI errorUri =
        Optional.ofNullable(getAsString(responseMap.get("error_uri")))
            .map(
                uri -> {
                  try {
                    return URI.create(uri);
                  } catch (IllegalArgumentException iae) {
                    return null;
                  }
                })
            .orElse(null);

    final OAuth2Error errorCode =
        Optional.ofNullable(getAsString(responseMap.get("error")))
            .map(
                error -> {
                  try {
                    return OAuth2Error.parseFrom(error);
                  } catch (IllegalArgumentException iaE) {
                    return null;
                  }
                })
            .orElse(null);

    final String errorDescription = getAsString(responseMap.get("error_description"));

    throw new OAuth2AccessTokenErrorResponse(errorCode, errorDescription, errorUri, response);
  }

  @Override
  protected OAuth2AccessToken createToken(String rawResponse) throws IOException {
    final Map<String, Object> response = JsonUtils.parse(rawResponse);

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

    final String refreshToken = getAsString(response.get(OAuthConstants.REFRESH_TOKEN));
    final String scope = getAsString(response.get(OAuthConstants.SCOPE));
    final String tokenType = getAsString(response.get("token_type"));
    final Object accessTokenObj =
        extractRequiredParameter(response, OAuthConstants.ACCESS_TOKEN, rawResponse);
    final String accessToken =
        accessTokenObj instanceof String ? (String) accessTokenObj : String.valueOf(accessTokenObj);

    return createToken(
        accessToken, tokenType, expiresIn, refreshToken, scope, response, rawResponse);
  }

  protected OAuth2AccessToken createToken(
      String accessToken,
      String tokenType,
      Integer expiresIn,
      String refreshToken,
      String scope,
      Map<String, Object> response,
      String rawResponse) {
    return new OAuth2AccessToken(
        accessToken, tokenType, expiresIn, refreshToken, scope, rawResponse);
  }

  private String getAsString(Object obj) {
    return obj instanceof String ? (String) obj : null;
  }

  private static class InstanceHolder {

    private static final OAuth2AccessTokenJsonExtractor INSTANCE =
        new OAuth2AccessTokenJsonExtractor();
  }
}
