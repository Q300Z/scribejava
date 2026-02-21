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

  public String getCodeChallenge() {
    return codeChallenge;
  }

  public void setCodeChallenge(String codeChallenge) {
    this.codeChallenge = codeChallenge;
  }

  public PKCECodeChallengeMethod getCodeChallengeMethod() {
    return codeChallengeMethod;
  }

  public void setCodeChallengeMethod(PKCECodeChallengeMethod codeChallengeMethod) {
    this.codeChallengeMethod = codeChallengeMethod;
  }

  public String getCodeVerifier() {
    return codeVerifier;
  }

  public void setCodeVerifier(String codeVerifier) {
    this.codeVerifier = codeVerifier;
  }

  public Map<String, String> getAuthorizationUrlParams() {
    final Map<String, String> params = new HashMap<>();
    params.put(PKCE_CODE_CHALLENGE_PARAM, codeChallenge);
    params.put(PKCE_CODE_CHALLENGE_METHOD_PARAM, codeChallengeMethod.name());
    return params;
  }
}
