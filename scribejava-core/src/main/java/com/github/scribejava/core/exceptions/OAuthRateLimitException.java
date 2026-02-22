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
package com.github.scribejava.core.exceptions;

import com.github.scribejava.core.model.OAuthResponseException;
import com.github.scribejava.core.model.Response;
import java.io.IOException;

/**
 * Exception levée lorsque le serveur d'API retourne une erreur de limitation de débit (Rate Limit).
 *
 * <p>Correspond généralement au code de statut HTTP 429 (Too Many Requests).
 *
 * @see <a href="https://tools.ietf.org/html/rfc6585#section-4">RFC 6585, Section 4 (429 Too Many
 *     Requests)</a>
 */
public class OAuthRateLimitException extends OAuthResponseException {

  private static final long serialVersionUID = 1L;

  /**
   * Constructeur.
   *
   * @param response La réponse HTTP contenant l'erreur de limitation.
   * @throws IOException en cas d'erreur de lecture du corps de la réponse.
   */
  public OAuthRateLimitException(Response response) throws IOException {
    super(response);
  }

  /**
   * Retourne un message détaillé incluant le code de statut et le corps de la réponse.
   *
   * @return Le message d'erreur formaté.
   */
  @Override
  public String getMessage() {
    try {
      return "Rate limit exceeded. Status: "
          + getResponse().getCode()
          + ", Body: "
          + getResponse().getBody();
    } catch (IOException e) {
      return "Rate limit exceeded. Status: " + getResponse().getCode() + " (could not read body)";
    }
  }
}
