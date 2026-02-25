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
package com.github.scribejava.oidc.model;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;

/**
 * Représente un Nonce (Number used once) conforme à OpenID Connect 1.0.
 *
 * <p>Utilisé pour lier une session client à un jeton d'identité afin de prévenir les attaques par
 * rejeu. Cette classe est immuable et utilise SecureRandom pour la génération.
 */
public class OidcNonce implements Serializable {

  private static final long serialVersionUID = 1L;
  private static final SecureRandom RANDOM = new SecureRandom();
  private static final int MIN_LENGTH = 16;

  private final String value;

  /**
   * @param value La valeur brute du nonce.
   * @throws IllegalArgumentException si la valeur est nulle, vide ou trop courte (moins de 16
   *     chars).
   */
  public OidcNonce(String value) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException("Nonce value cannot be null or empty");
    }
    if (value.length() < MIN_LENGTH) {
      throw new IllegalArgumentException("Nonce value must be at least 16 characters long");
    }
    this.value = value;
  }

  /**
   * Génère un nonce aléatoire sécurisé de 32 octets (URL-safe Base64).
   *
   * @return Une nouvelle instance de {@link OidcNonce}.
   */
  public static OidcNonce generate() {
    byte[] bytes = new byte[32];
    RANDOM.nextBytes(bytes);
    return new OidcNonce(Base64.getUrlEncoder().withoutPadding().encodeToString(bytes));
  }

  /**
   * @return La valeur textuelle du nonce.
   */
  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OidcNonce oidcNonce = (OidcNonce) o;
    return Objects.equals(value, oidcNonce.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }
}
