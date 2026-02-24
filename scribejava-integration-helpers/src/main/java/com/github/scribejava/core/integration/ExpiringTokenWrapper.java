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
import java.time.Instant;
import java.util.Objects;

/**
 * Encapsule un jeton OAuth 2.0 avec sa date d'expiration calculée lors de la réception. Permet
 * d'anticiper le rafraîchissement avant que le serveur ne rejette la requête.
 */
public class ExpiringTokenWrapper {

  private final OAuth2AccessToken token;
  private final Instant receivedAt;
  private final Instant expirationInstant;

  /**
   * @param token token
   */
  public ExpiringTokenWrapper(OAuth2AccessToken token) {
    this(token, Instant.now());
  }

  /**
   * @param token token
   * @param receivedAt receivedAt
   */
  public ExpiringTokenWrapper(OAuth2AccessToken token, Instant receivedAt) {
    this.token = Objects.requireNonNull(token);
    this.receivedAt = Objects.requireNonNull(receivedAt);
    this.expirationInstant =
        token.getExpiresIn() != null ? receivedAt.plusSeconds(token.getExpiresIn()) : Instant.MAX;
  }

  /**
   * @return token
   */
  public OAuth2AccessToken getToken() {
    return token;
  }

  /**
   * @return receivedAt
   */
  public Instant getReceivedAt() {
    return receivedAt;
  }

  /**
   * @return expirationInstant
   */
  public Instant getExpirationInstant() {
    return expirationInstant;
  }

  /**
   * @return isExpired
   */
  public boolean isExpired() {
    return isExpired(Instant.now());
  }

  /**
   * @param at at
   * @return isExpired
   */
  public boolean isExpired(Instant at) {
    return at.isAfter(expirationInstant);
  }

  /**
   * Vérifie si le jeton est expiré ou va l'être dans les prochaines secondes (buffer).
   *
   * @param bufferInSeconds Marge de sécurité en secondes.
   * @return true si expiré avec buffer.
   */
  public boolean isExpiredWithBuffer(int bufferInSeconds) {
    return isExpiredWithBuffer(Instant.now(), bufferInSeconds);
  }

  public boolean isExpiredWithBuffer(Instant at, int bufferInSeconds) {
    return at.plusSeconds(bufferInSeconds).isAfter(expirationInstant);
  }
}
