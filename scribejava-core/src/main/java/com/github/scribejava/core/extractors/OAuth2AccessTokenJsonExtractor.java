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

import com.github.scribejava.core.model.JsonObject;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuth2AccessTokenErrorResponse;
import com.github.scribejava.core.model.OAuthConstants;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.oauth2.OAuth2Error;
import com.github.scribejava.core.utils.JsonUtils;
import com.github.scribejava.core.utils.Preconditions;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;

/** Implémentation JSON native robuste de {@link TokenExtractor} pour OAuth 2.0. */
public class OAuth2AccessTokenJsonExtractor extends AbstractJsonExtractor<OAuth2AccessToken> {

  protected OAuth2AccessTokenJsonExtractor() {}

  /**
   * @return L'instance singleton.
   */
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
    final JsonObject json = new JsonObject(JsonUtils.parse(responseBody));

    final URI errorUri =
        Optional.ofNullable(json.getString("error_uri"))
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
        Optional.ofNullable(json.getString("error"))
            .map(
                error -> {
                  try {
                    return OAuth2Error.parseFrom(error);
                  } catch (IllegalArgumentException iaE) {
                    return null;
                  }
                })
            .orElse(null);

    final String errorDescription = json.getString("error_description");

    throw new OAuth2AccessTokenErrorResponse(errorCode, errorDescription, errorUri, response);
  }

  @Override
  protected OAuth2AccessToken createToken(String rawResponse) throws IOException {
    final JsonObject json = new JsonObject(JsonUtils.parse(rawResponse));

    final Long expiresIn = json.getLong("expires_in");
    final String refreshToken = json.getString(OAuthConstants.REFRESH_TOKEN);
    final String scope = json.getString(OAuthConstants.SCOPE);
    final String tokenType = json.getString("token_type");

    final String accessToken = json.getString(OAuthConstants.ACCESS_TOKEN);
    if (accessToken == null) {
      throw new IOException("Missing required parameter: " + OAuthConstants.ACCESS_TOKEN);
    }

    return createToken(
        accessToken,
        tokenType,
        expiresIn != null ? expiresIn.intValue() : null,
        refreshToken,
        scope,
        json,
        rawResponse);
  }

  /**
   * Méthode de création de jeton personnalisable.
   *
   * @param accessToken jeton d'accès
   * @param tokenType type
   * @param expiresIn expiration
   * @param refreshToken jeton de rafraîchissement
   * @param scope portée
   * @param json objet JSON complet
   * @param rawResponse réponse brute
   * @return Le jeton OAuth 2.0.
   */
  protected OAuth2AccessToken createToken(
      String accessToken,
      String tokenType,
      Integer expiresIn,
      String refreshToken,
      String scope,
      JsonObject json,
      String rawResponse) {
    return new OAuth2AccessToken(
        accessToken, tokenType, expiresIn, refreshToken, scope, rawResponse);
  }

  private static class InstanceHolder {

    private static final OAuth2AccessTokenJsonExtractor INSTANCE =
        new OAuth2AccessTokenJsonExtractor();
  }
}
