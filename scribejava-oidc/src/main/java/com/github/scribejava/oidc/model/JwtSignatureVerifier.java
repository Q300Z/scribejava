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
import java.security.Signature;

/**
 * Validateur de signature pour les JSON Web Tokens.
 *
 * <p>Utilise exclusivement les API natives de sécurité Java pour éviter les dépendances externes.
 */
public class JwtSignatureVerifier {

  /**
   * Vérifie une signature RS256.
   *
   * @param content Le contenu signé (header.payload).
   * @param signature La signature brute.
   * @param publicKey La clé publique de l'émetteur.
   * @return true si la signature est valide.
   */
  public boolean verifyRS256(String content, byte[] signature, PublicKey publicKey) {
    try {
      final Signature sig = Signature.getInstance("SHA256withRSA");
      sig.initVerify(publicKey);
      sig.update(content.getBytes());
      return sig.verify(signature);
    } catch (Exception e) {
      return false;
    }
  }
}
