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
package com.github.scribejava.core.pkce;

import java.util.HashMap;
import java.util.Map;

/**
 * Used to hold code_challenge, code_challenge_method and code_verifier for
 * https://tools.ietf.org/html/rfc7636
 */
public class PKCE {

  public static final String PKCE_CODE_CHALLENGE_METHOD_PARAM = "code_challenge_method";
  public static final String PKCE_CODE_CHALLENGE_PARAM = "code_challenge";
  public static final String PKCE_CODE_VERIFIER_PARAM = "code_verifier";

  private String codeChallenge;
  private PKCECodeChallengeMethod codeChallengeMethod = PKCECodeChallengeMethod.S256;
  private String codeVerifier;

  /**
   * Retourne le code challenge.
   *
   * @return Le code challenge.
   */
  public String getCodeChallenge() {
    return codeChallenge;
  }

  /**
   * Définit le code challenge.
   *
   * @param codeChallenge Le code challenge.
   */
  public void setCodeChallenge(String codeChallenge) {
    this.codeChallenge = codeChallenge;
  }

  /**
   * Retourne la méthode de challenge utilisée.
   *
   * @return La {@link PKCECodeChallengeMethod}.
   */
  public PKCECodeChallengeMethod getCodeChallengeMethod() {
    return codeChallengeMethod;
  }

  /**
   * Définit la méthode de challenge.
   *
   * @param codeChallengeMethod La méthode.
   */
  public void setCodeChallengeMethod(PKCECodeChallengeMethod codeChallengeMethod) {
    this.codeChallengeMethod = codeChallengeMethod;
  }

  /**
   * Retourne le code verifier.
   *
   * @return Le code verifier.
   */
  public String getCodeVerifier() {
    return codeVerifier;
  }

  /**
   * Définit le code verifier.
   *
   * @param codeVerifier Le code verifier.
   */
  public void setCodeVerifier(String codeVerifier) {
    this.codeVerifier = codeVerifier;
  }

  /**
   * Retourne les paramètres à ajouter à l'URL d'autorisation pour PKCE.
   *
   * @return Une map contenant code_challenge et code_challenge_method.
   */
  public Map<String, String> getAuthorizationUrlParams() {
    final Map<String, String> params = new HashMap<>();
    params.put(PKCE_CODE_CHALLENGE_PARAM, codeChallenge);
    params.put(PKCE_CODE_CHALLENGE_METHOD_PARAM, codeChallengeMethod.name());
    return params;
  }
}
