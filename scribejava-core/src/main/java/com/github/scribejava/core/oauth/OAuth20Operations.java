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
package com.github.scribejava.core.oauth;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth2.grant.OAuth20Grant;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/** Définit les opérations synchrones pour OAuth 2.0 (Interface Segregation Principle). */
public interface OAuth20Operations {

  /**
   * Récupère un jeton d'accès de manière synchrone.
   *
   * @param grant La concession (grant) à utiliser.
   * @return Le jeton d'accès.
   * @throws IOException Erreur réseau.
   * @throws InterruptedException Interruption.
   * @throws ExecutionException Échec de la requête.
   */
  OAuth2AccessToken getAccessToken(OAuth20Grant grant)
      throws IOException, InterruptedException, ExecutionException;

  /**
   * Rafraîchit un jeton d'accès de manière synchrone.
   *
   * @param refreshToken Le jeton de rafraîchissement.
   * @return Le nouveau jeton d'accès.
   * @throws IOException Erreur réseau.
   * @throws InterruptedException Interruption.
   * @throws ExecutionException Échec de la requête.
   */
  OAuth2AccessToken refreshAccessToken(String refreshToken)
      throws IOException, InterruptedException, ExecutionException;
}
