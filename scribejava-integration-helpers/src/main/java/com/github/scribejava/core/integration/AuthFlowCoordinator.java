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
package com.github.scribejava.core.integration;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.oauth2.grant.AuthorizationCodeGrant;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * Coordonne la fin du flux d'autorisation (Callback). Gère la validation CSRF, l'échange du code et
 * la persistance initiale.
 *
 * @param <K> Type de la clé d'identification.
 */
public class AuthFlowCoordinator<K> {

  private final OAuth20Service oauthService;
  private final TokenRepository<K, ExpiringTokenWrapper> repository;
  private AuthEventListener<K> listener;

  /**
   * @param oauthService service
   * @param repository repository
   */
  public AuthFlowCoordinator(
      OAuth20Service oauthService, TokenRepository<K, ExpiringTokenWrapper> repository) {
    this.oauthService = Objects.requireNonNull(oauthService);
    this.repository = Objects.requireNonNull(repository);
  }

  /**
   * @param listener listener
   */
  public void setListener(AuthEventListener<K> listener) {
    this.listener = listener;
  }

  /**
   * Termine le flux d'autorisation.
   *
   * @param key Clé de l'utilisateur pour le stockage.
   * @param code Code reçu du serveur.
   * @param receivedState State reçu du serveur.
   * @param expectedState State attendu (stocké précédemment en session).
   * @return Le résultat de l'authentification.
   * @throws SecurityException Si le state est invalide (Attaque CSRF).
   * @throws IOException En cas d'erreur réseau lors de l'échange.
   * @throws InterruptedException InterruptedException
   * @throws ExecutionException ExecutionException
   */
  public AuthResult finishAuthorization(
      K key, String code, String receivedState, String expectedState)
      throws IOException, InterruptedException, ExecutionException {
    try {
      validateState(receivedState, expectedState);
    } catch (SecurityException e) {
      if (listener != null) {
        listener.onCsrfDetected(key, receivedState, expectedState);
      }
      throw e;
    }

    final OAuth2AccessToken token = oauthService.getAccessToken(new AuthorizationCodeGrant(code));
    repository.save(key, new ExpiringTokenWrapper(token));

    return new AuthResult(token);
  }

  private void validateState(String received, String expected) {
    if (expected == null || !expected.equals(received)) {
      throw new SecurityException("CSRF Detected! Invalid state parameter.");
    }
  }
}
