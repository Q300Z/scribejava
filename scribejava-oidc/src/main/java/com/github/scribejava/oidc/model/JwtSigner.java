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

import com.github.scribejava.core.exceptions.OAuthException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;

/** Interface pour la signature de JWT native. */
public interface JwtSigner {

  /**
   * @return l'identifiant de l'algorithme (ex: RS256).
   */
  String getAlgorithm();

  /**
   * Signe un contenu et retourne la signature encodée en Base64URL.
   *
   * @param payload le contenu à signer (header + "." + claims)
   * @param privateKey la clé privée
   * @return la signature Base64URL
   * @throws OAuthException en cas d'erreur cryptographique
   */
  String sign(String payload, PrivateKey privateKey) throws OAuthException;

  /** Implémentation RS256. */
  class RsaSha256Signer implements JwtSigner {

    @Override
    public String getAlgorithm() {
      return "RS256";
    }

    @Override
    public String sign(String payload, PrivateKey privateKey) throws OAuthException {
      try {
        final Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(payload.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(signature.sign());
      } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
        throw new OAuthException("RS256 signing failed", e);
      }
    }
  }
}
