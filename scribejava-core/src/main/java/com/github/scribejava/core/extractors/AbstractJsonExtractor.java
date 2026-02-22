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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.core.exceptions.OAuthException;

/**
 * Base abstraite pour tous les extracteurs de données au format JSON.
 *
 * <p>Utilise la bibliothèque Jackson pour analyser les réponses HTTP et extraire les informations
 * nécessaires.
 */
public abstract class AbstractJsonExtractor {

  /** Instance partagée du mappeur d'objets Jackson. */
  protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  /**
   * Extrait un paramètre obligatoire d'un nœud JSON.
   *
   * @param errorNode Le nœud JSON à analyser.
   * @param parameterName Le nom du paramètre attendu.
   * @param rawResponse La réponse brute (utilisée pour le message d'erreur).
   * @return Le {@link JsonNode} correspondant au paramètre.
   * @throws OAuthException si le paramètre est absent ou nul.
   */
  protected static JsonNode extractRequiredParameter(
      JsonNode errorNode, String parameterName, String rawResponse) throws OAuthException {
    final JsonNode value = errorNode.get(parameterName);

    if (value == null || value.isNull()) {
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
