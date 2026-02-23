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
package com.github.scribejava.core.revoke;

import com.github.scribejava.core.extractors.OAuth2AccessTokenJsonExtractor;
import com.github.scribejava.core.model.Response;
import java.io.IOException;

/**
 * Convertisseur pour la réponse de révocation de jeton OAuth 2.0.
 *
 * @see <a href="https://tools.ietf.org/html/rfc7009#section-2.2">RFC 7009, Section 2.2 (Revocation
 *     Response)</a>
 */
public class OAuth2RevokeTokenResponseConverter {

  /**
   * Vérifie la réponse de révocation.
   *
   * @param response La réponse HTTP.
   * @return null si succès.
   * @throws IOException si le serveur retourne une erreur.
   */
  public Void convert(Response response) throws IOException {
    if (response.getCode() != 200) {
      OAuth2AccessTokenJsonExtractor.instance().generateError(response);
    }
    return null;
  }
}
