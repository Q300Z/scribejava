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
package com.github.scribejava.oauth1.model;

/** Représente un jeton d'accès (Access Token) OAuth 1.0a. */
public class OAuth1AccessToken extends OAuth1Token {

  /**
   * Constructeur simple.
   *
   * @param token La valeur du jeton.
   * @param tokenSecret Le secret du jeton.
   */
  public OAuth1AccessToken(String token, String tokenSecret) {
    this(token, tokenSecret, null);
  }

  /**
   * Constructeur complet.
   *
   * @param token La valeur du jeton.
   * @param tokenSecret Le secret du jeton.
   * @param rawResponse La réponse brute.
   */
  public OAuth1AccessToken(String token, String tokenSecret, String rawResponse) {
    super(token, tokenSecret, rawResponse);
  }

  /** @return true si le jeton est vide ou nul. */
  public boolean isEmpty() {
    return getToken() == null || getToken().isEmpty();
  }
}
