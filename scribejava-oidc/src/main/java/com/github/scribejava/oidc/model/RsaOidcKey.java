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

import java.security.PublicKey;

/** Implémentation RSA d'une clé OIDC. */
public class RsaOidcKey implements OidcKey {
  private final String kid;
  private final String alg;
  private final PublicKey publicKey;

  /**
   * @param kid id
   * @param alg alg
   * @param publicKey key
   */
  public RsaOidcKey(String kid, String alg, PublicKey publicKey) {
    this.kid = kid;
    this.alg = alg;
    this.publicKey = publicKey;
  }

  @Override
  public String getKid() {
    return kid;
  }

  @Override
  public String getAlg() {
    return alg;
  }

  @Override
  public PublicKey getPublicKey() {
    return publicKey;
  }
}
