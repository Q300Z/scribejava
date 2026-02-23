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
import com.github.scribejava.core.model.OAuthAsyncRequestCallback;
import com.github.scribejava.core.oauth2.grant.OAuth20Grant;
import java.util.concurrent.CompletableFuture;

/** Définit les opérations asynchrones pour OAuth 2.0 (Interface Segregation Principle). */
public interface OAuth20AsyncOperations {

  /**
   * Récupère un jeton d'accès de manière asynchrone.
   *
   * @param grant La concession (grant) à utiliser.
   * @return Future résolvant vers le jeton d'accès.
   */
  CompletableFuture<OAuth2AccessToken> getAccessTokenAsync(OAuth20Grant grant);

  /**
   * Récupère un jeton d'accès de manière asynchrone avec callback.
   *
   * @param grant La concession à utiliser.
   * @param callback Le callback de résultat.
   * @return Future résolvant vers le jeton d'accès.
   */
  CompletableFuture<OAuth2AccessToken> getAccessTokenAsync(
      OAuth20Grant grant, OAuthAsyncRequestCallback<OAuth2AccessToken> callback);

  /**
   * Rafraîchit un jeton d'accès de manière asynchrone.
   *
   * @param refreshToken Le jeton de rafraîchissement.
   * @return Future résolvant vers le nouveau jeton d'accès.
   */
  CompletableFuture<OAuth2AccessToken> refreshAccessTokenAsync(String refreshToken);

  /**
   * Révoque un jeton de manière asynchrone.
   *
   * @param token Le jeton à révoquer.
   * @param tokenTypeHint Indice sur le type de jeton.
   * @return Future.
   */
  CompletableFuture<Void> revokeTokenAsync(
      String token, com.github.scribejava.core.revoke.TokenTypeHint tokenTypeHint);
}
