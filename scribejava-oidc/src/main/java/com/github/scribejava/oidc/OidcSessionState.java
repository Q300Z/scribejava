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
package com.github.scribejava.oidc;

import com.github.scribejava.oidc.model.OidcNonce;
import java.io.Serializable;

/**
 * Represents the state of an OIDC session containing the state, the nonce, and the PKCE code
 * verifier.
 */
public class OidcSessionState implements Serializable {
  private static final long serialVersionUID = 1L;

  private final String state;
  private final OidcNonce nonce;
  private final String codeVerifier;

  /**
   * Constructs an OidcSessionState instance.
   *
   * @param state the state value used for CSRF protection
   * @param nonce the OIDC nonce used to prevent replay attacks
   * @param codeVerifier the PKCE code verifier value
   */
  public OidcSessionState(String state, OidcNonce nonce, String codeVerifier) {
    this.state = state;
    this.nonce = nonce;
    this.codeVerifier = codeVerifier;
  }

  /**
   * Gets the state value.
   *
   * @return the state value
   */
  public String getState() {
    return state;
  }

  /**
   * Gets the OIDC nonce.
   *
   * @return the {@link OidcNonce}
   */
  public OidcNonce getNonce() {
    return nonce;
  }

  /**
   * Gets the PKCE code verifier.
   *
   * @return the PKCE code verifier string
   */
  public String getCodeVerifier() {
    return codeVerifier;
  }
}
