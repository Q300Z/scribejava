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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuth2AccessTokenErrorResponse;
import com.github.scribejava.core.model.OAuthConstants;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.oauth2.OAuth2Error;
import com.github.scribejava.core.utils.Preconditions;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;

/**
 * Implémentation JSON par défaut de {@link TokenExtractor} pour OAuth 2.0.
 *
 * @see <a href="http://tools.ietf.org/html/rfc6749#section-5.1">RFC 6749, Section 5.1 (Successful
 *     Response)</a>
 */
public class OAuth2AccessTokenJsonExtractor extends AbstractJsonExtractor<OAuth2AccessToken> {

  /** Constructeur protégé. */
  protected OAuth2AccessTokenJsonExtractor() {}

  /**
   * Retourne l'instance unique (singleton) de l'extracteur.
   *
   * @return L'instance de {@link OAuth2AccessTokenJsonExtractor}.
   */
  public static OAuth2AccessTokenJsonExtractor instance() {
    return InstanceHolder.INSTANCE;
  }

  /**
   * Extrait le jeton d'accès de la réponse HTTP.
   *
   * @param response La réponse du serveur d'autorisation.
   * @return Un objet {@link OAuth2AccessToken}.
   * @throws IOException en cas d'erreur de lecture ou si le code de statut n'est pas 200.
   */
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
   * Analyse la réponse d'erreur au format JSON et lève une exception dédiée.
   *
   * @param response La réponse contenant l'erreur.
   * @throws IOException toujours, sous forme de {@link OAuth2AccessTokenErrorResponse}.
   * @see <a href="http://tools.ietf.org/html/rfc6749#section-5.2">RFC 6749, Section 5.2 (Error
   *     Response)</a>
   */
  public void generateError(Response response) throws IOException {
    final String responseBody = response.getBody();
    final JsonNode responseBodyJson;
    try {
      responseBodyJson = OBJECT_MAPPER.readTree(responseBody);
    } catch (JsonProcessingException ex) {
      throw new OAuth2AccessTokenErrorResponse(null, null, null, response);
    }

    final URI errorUri =
        Optional.ofNullable(responseBodyJson.get("error_uri"))
            .map(JsonNode::asText)
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
        Optional.ofNullable(responseBodyJson.get("error"))
            .map(JsonNode::asText)
            .map(
                error -> {
                  try {
                    return OAuth2Error.parseFrom(error);
                  } catch (IllegalArgumentException iaE) {
                    return null;
                  }
                })
            .orElse(null);

    final String errorDescription =
        Optional.ofNullable(responseBodyJson.get("error_description"))
            .map(JsonNode::asText)
            .orElse(null);

    throw new OAuth2AccessTokenErrorResponse(errorCode, errorDescription, errorUri, response);
  }

  @Override
  protected OAuth2AccessToken createToken(String rawResponse) throws IOException {
    final JsonNode response = OBJECT_MAPPER.readTree(rawResponse);

    final Integer expiresIn =
        Optional.ofNullable(response.get("expires_in")).map(JsonNode::asInt).orElse(null);
    final String refreshToken =
        Optional.ofNullable(response.get(OAuthConstants.REFRESH_TOKEN))
            .map(JsonNode::asText)
            .orElse(null);
    final String scope =
        Optional.ofNullable(response.get(OAuthConstants.SCOPE)).map(JsonNode::asText).orElse(null);
    final String tokenType =
        Optional.ofNullable(response.get("token_type")).map(JsonNode::asText).orElse(null);
    final String accessToken =
        extractRequiredParameter(response, OAuthConstants.ACCESS_TOKEN, rawResponse).asText();

    return createToken(
        accessToken, tokenType, expiresIn, refreshToken, scope, response, rawResponse);
  }

  protected OAuth2AccessToken createToken(
      String accessToken,
      String tokenType,
      Integer expiresIn,
      String refreshToken,
      String scope,
      JsonNode response,
      String rawResponse) {
    return new OAuth2AccessToken(
        accessToken, tokenType, expiresIn, refreshToken, scope, rawResponse);
  }

  private static class InstanceHolder {

    private static final OAuth2AccessTokenJsonExtractor INSTANCE =
        new OAuth2AccessTokenJsonExtractor();
  }
}
