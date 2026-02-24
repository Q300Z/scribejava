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

import com.github.scribejava.core.pkce.PKCE;
import java.io.Serializable;

/**
 * Regroupe les données transitoires nécessaires pour sécuriser et terminer un flux d'autorisation.
 * Cet objet doit être stocké en session (ou cookie sécurisé) avant la redirection vers l'IDP.
 */
public class AuthSessionContext implements Serializable {

  private final String state;
  private final String nonce;
  private final PKCE pkce;

  /**
   * @param state state
   * @param nonce nonce (OIDC)
   * @param pkce pkce (RFC 7636)
   */
  public AuthSessionContext(String state, String nonce, PKCE pkce) {
    this.state = state;
    this.nonce = nonce;
    this.pkce = pkce;
  }

  /**
   * @return state
   */
  public String getState() {
    return state;
  }

  /**
   * @return nonce
   */
  public String getNonce() {
    return nonce;
  }

  /**
   * @return pkce
   */
  public PKCE getPkce() {
    return pkce;
  }
}
