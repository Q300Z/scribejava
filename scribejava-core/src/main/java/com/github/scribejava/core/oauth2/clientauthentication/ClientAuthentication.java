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
package com.github.scribejava.core.oauth2.clientauthentication;

import com.github.scribejava.core.model.OAuthRequest;

/**
 * Represents<br>
 * 2.3. Client Authentication<br>
 * https://tools.ietf.org/html/rfc6749#section-2.3 <br>
 * just implement this interface to implement "2.3.2. Other Authentication Methods"<br>
 * https://tools.ietf.org/html/rfc6749#section-2.3.2
 */
public interface ClientAuthentication {

  /**
   * Ajoute les informations d'authentification du client à la requête.
   *
   * @param request La requête à laquelle ajouter l'authentification.
   * @param apiKey La clé API (Client ID).
   * @param apiSecret Le secret API (Client Secret).
   * @see <a href="https://tools.ietf.org/html/rfc6749#section-2.3">RFC 6749, Section 2.3 (Client
   *     Authentication)</a>
   */
  void addClientAuthentication(OAuthRequest request, String apiKey, String apiSecret);
}
