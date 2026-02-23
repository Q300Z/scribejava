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
package com.github.scribejava.core.oauth2.grant;

import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.oauth.OAuth20Service;

/**
 * Interface for OAuth 2.0 Grant types. Strategy pattern to decouple grant logic from
 * OAuth20Service.
 */
public interface OAuth20Grant {

  /**
   * Crée la requête HTTP permettant d'échanger une concession d'autorisation (grant) contre un
   * jeton d'accès.
   *
   * <p>Cette méthode implémente le patron de conception "Strategy" pour découpler la logique
   * spécifique à chaque type de concession du service OAuth principal.
   *
   * @param service L'instance du service OAuth 2.0 utilisée pour configurer la requête.
   * @return Une instance de {@link OAuthRequest} configurée selon le type de concession.
   * @see <a href="https://tools.ietf.org/html/rfc6749#section-1.3">RFC 6749, Section 1.3
   *     (Authorization Grant)</a>
   */
  OAuthRequest createRequest(OAuth20Service service);
}
