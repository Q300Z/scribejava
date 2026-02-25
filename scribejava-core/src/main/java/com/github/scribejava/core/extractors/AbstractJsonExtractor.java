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

import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Token;
import com.github.scribejava.core.utils.Preconditions;
import java.io.IOException;
import java.util.Map;

/**
 * Base abstraite pour tous les extracteurs de données au format JSON natif.
 *
 * @param <T> Type de jeton extrait.
 */
public abstract class AbstractJsonExtractor<T extends Token> implements TokenExtractor<T> {

  @Override
  public T extract(Response response) throws IOException {
    final String body = response.getBody();
    Preconditions.checkEmptyString(
        body, "Response body is incorrect. Can't extract a token from an empty string");
    return createToken(body);
  }

  protected abstract T createToken(String body) throws IOException;

  /**
   * Extrait un paramètre obligatoire d'une Map JSON.
   *
   * @param responseMap La map JSON à analyser.
   * @param parameterName Le nom du paramètre attendu.
   * @param rawResponse La réponse brute (utilisée pour le message d'erreur).
   * @return La valeur.
   * @throws OAuthException si le paramètre est absent ou nul.
   */
  protected static Object extractRequiredParameter(
      Map<String, Object> responseMap, String parameterName, String rawResponse)
      throws OAuthException {
    final Object value = responseMap.get(parameterName);

    if (value == null) {
      throw new OAuthException(
          "Response body is incorrect. Can't extract a '"
              + parameterName
              + "' from this: '"
              + rawResponse
              + "'",
          null);
    }

    return value;
  }
}
