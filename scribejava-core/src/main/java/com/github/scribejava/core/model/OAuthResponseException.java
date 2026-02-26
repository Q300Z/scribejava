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
import com.github.scribejava.core.oauth2.OAuth2Error;
import com.github.scribejava.core.utils.JsonUtils;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

/** Exception levée lorsqu'une réponse d'erreur est reçue du serveur OAuth. */
public class OAuthResponseException extends OAuthException {

  private static final long serialVersionUID = 1309424849700276816L;

  private final transient Response response;
  private transient JsonObject errorDetails;

  /**
   * Constructeur à partir d'une réponse brute.
   *
   * @param rawResponse La réponse HTTP d'erreur.
   * @throws IOException en cas d'erreur lors de la lecture du corps de la réponse.
   */
  public OAuthResponseException(Response rawResponse) throws IOException {
    super(rawResponse.getBody());
    this.response = rawResponse;
  }

  /**
   * Retourne la réponse HTTP associée à cette exception.
   *
   * @return La réponse {@link Response}.
   */
  public Response getResponse() {
    return response;
  }

  /**
   * Retourne les détails de l'erreur sous forme d'objet JSON typé.
   *
   * @return Optional JsonObject
   */
  public synchronized Optional<JsonObject> getErrorDetails() {
    if (errorDetails == null) {
      try {
        final String body = response.getBody();
        if (body != null && body.trim().startsWith("{")) {
          errorDetails = new JsonObject(JsonUtils.parse(body));
        }
      } catch (Exception e) {
        // Échec silencieux, on ne renvoie rien
      }
    }
    return Optional.ofNullable(errorDetails);
  }

  /**
   * Retourne le code d'erreur standardisé.
   *
   * @return Optional OAuth2Error
   */
  public Optional<OAuth2Error> getOAuth2Error() {
    return getErrorDetails().map(details -> details.getString("error")).map(OAuth2Error::parseFrom);
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 29 * hash + Objects.hashCode(response);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final OAuthResponseException other = (OAuthResponseException) obj;
    return Objects.equals(this.response, other.response);
  }
}
