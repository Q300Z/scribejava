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
package com.github.scribejava.core.model;

import com.github.scribejava.core.utils.Preconditions;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Représente un jeton d'accès (Access Token) OAuth 2.0.
 *
 * @see <a href="http://tools.ietf.org/html/rfc6749#section-5.1">RFC 6749, Section 5.1 (Successful
 *     Response)</a>
 */
public class OAuth2AccessToken extends Token {

  private static final long serialVersionUID = 8901381135476613449L;

  private final String accessToken;
  private final String tokenType;
  private final Integer expiresIn;
  private final String refreshToken;
  private final String scope;
  private final Instant receivedAt;

  /**
   * Constructeur simple.
   *
   * @param accessToken Le jeton d'accès.
   */
  public OAuth2AccessToken(String accessToken) {
    this(accessToken, null);
  }

  /**
   * Constructeur avec réponse brute.
   *
   * @param accessToken Le jeton d'accès.
   * @param rawResponse La réponse HTTP brute du serveur.
   */
  public OAuth2AccessToken(String accessToken, String rawResponse) {
    this(accessToken, null, null, null, null, rawResponse);
  }

  /**
   * Constructeur complet.
   *
   * @param accessToken Le jeton d'accès.
   * @param tokenType Le type de jeton.
   * @param expiresIn Durée de validité.
   * @param refreshToken Jeton de renouvellement.
   * @param scope Portée.
   * @param rawResponse Réponse brute.
   */
  public OAuth2AccessToken(
      String accessToken,
      String tokenType,
      Integer expiresIn,
      String refreshToken,
      String scope,
      String rawResponse) {
    super(rawResponse);
    Preconditions.checkNotNull(accessToken, "access_token can't be null");
    this.accessToken = accessToken;
    this.tokenType = tokenType;
    this.expiresIn = expiresIn;
    this.refreshToken = refreshToken;
    this.scope = scope;
    this.receivedAt = Instant.now();
  }

  /**
   * @return Le jeton d'accès.
   */
  public String getAccessToken() {
    return accessToken;
  }

  /**
   * @return Le type de jeton.
   */
  public String getTokenType() {
    return tokenType;
  }

  /**
   * @return La durée de validité en secondes.
   */
  public Integer getExpiresIn() {
    return expiresIn;
  }

  /**
   * @return Le jeton de renouvellement.
   */
  public String getRefreshToken() {
    return refreshToken;
  }

  /**
   * @return La portée du jeton.
   */
  public String getScope() {
    return scope;
  }

  /**
   * Calcule la date d'expiration absolue.
   *
   * @return Optional Instant
   */
  public Optional<Instant> getExpiresAt() {
    if (expiresIn == null) {
      return Optional.empty();
    }
    return Optional.of(receivedAt.plusSeconds(expiresIn));
  }

  /**
   * Vérifie si le jeton est expiré.
   *
   * @return true si expiré.
   */
  public boolean isExpired() {
    return getExpiresAt().map(at -> Instant.now().isAfter(at)).orElse(false);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 41 * hash + Objects.hashCode(accessToken);
    hash = 41 * hash + Objects.hashCode(tokenType);
    hash = 41 * hash + Objects.hashCode(expiresIn);
    hash = 41 * hash + Objects.hashCode(refreshToken);
    hash = 41 * hash + Objects.hashCode(scope);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final OAuth2AccessToken other = (OAuth2AccessToken) obj;
    return Objects.equals(accessToken, other.getAccessToken())
        && Objects.equals(tokenType, other.getTokenType())
        && Objects.equals(refreshToken, other.getRefreshToken())
        && Objects.equals(scope, other.getScope())
        && Objects.equals(expiresIn, other.getExpiresIn());
  }
}
