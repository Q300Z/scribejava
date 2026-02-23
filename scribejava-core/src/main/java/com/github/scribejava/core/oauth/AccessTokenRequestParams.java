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

import java.util.Map;

/**
 * Paramètres pour une requête d'obtention de jeton d'accès (Access Token).
 *
 * <p>Regroupe le code d'autorisation, le vérificateur PKCE et les éventuels paramètres
 * supplémentaires. Cette classe n'est pas thread-safe.
 */
public class AccessTokenRequestParams {

  private final String code;
  private String pkceCodeVerifier;
  private String scope;
  private Map<String, String> extraParameters;

  /**
   * Constructeur.
   *
   * @param code Le code d'autorisation reçu.
   */
  public AccessTokenRequestParams(String code) {
    this.code = code;
  }

  /**
   * @return Le code d'autorisation.
   */
  public String getCode() {
    return code;
  }

  public String getPkceCodeVerifier() {
    return pkceCodeVerifier;
  }

  public AccessTokenRequestParams pkceCodeVerifier(String pkceCodeVerifier) {
    this.pkceCodeVerifier = pkceCodeVerifier;
    return this;
  }

  public String getScope() {
    return scope;
  }

  public AccessTokenRequestParams scope(String scope) {
    this.scope = scope;
    return this;
  }

  public Map<String, String> getExtraParameters() {
    return extraParameters;
  }

  public AccessTokenRequestParams extraParameters(Map<String, String> extraParameters) {
    this.extraParameters = extraParameters;
    return this;
  }
}
