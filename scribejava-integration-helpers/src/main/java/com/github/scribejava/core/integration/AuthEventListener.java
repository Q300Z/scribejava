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

/**
 * Interface pour l'écoute des événements liés à l'authentification. Utile pour l'audit, le
 * monitoring et le logging applicatif.
 *
 * @param <K> Type de la clé d'identification de l'utilisateur.
 */
public interface AuthEventListener<K> {

  /**
   * Appelé lorsqu'un jeton a été rafraîchi avec succès.
   *
   * @param key clé de l'utilisateur.
   * @param newToken le nouveau wrapper de jeton.
   */
  void onTokenRefreshed(K key, ExpiringTokenWrapper newToken);

  /**
   * Appelé lorsqu'un rafraîchissement de jeton a échoué.
   *
   * @param key clé de l'utilisateur.
   * @param e l'exception rencontrée.
   */
  void onRefreshFailed(K key, Exception e);

  /**
   * Appelé lorsqu'une tentative de CSRF (state invalide) est détectée.
   *
   * @param key clé de l'utilisateur (si disponible).
   * @param receivedState state reçu.
   * @param expectedState state attendu.
   */
  void onCsrfDetected(K key, String receivedState, String expectedState);
}
