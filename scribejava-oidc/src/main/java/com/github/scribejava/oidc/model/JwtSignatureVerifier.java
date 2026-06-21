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

/** Validateur de signature pour les JSON Web Tokens natif. */
public class JwtSignatureVerifier {

  /**
   * Vérifie une signature RS256.
   *
   * @param signedContent Le contenu signé (bytes).
   * @param signature La signature brute.
   * @param publicKey La clé publique de l'émetteur.
   * @return true si la signature est valide.
   */
  public boolean verifyRS256(byte[] signedContent, byte[] signature, PublicKey publicKey) {
    try {
      final Signature sig = Signature.getInstance("SHA256withRSA");
      sig.initVerify(publicKey);
      sig.update(signedContent);
      return sig.verify(signature);
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Vérifie la signature avec un algorithme spécifié.
   *
   * @param alg L'algorithme de signature (ex: RS256, ES256, PS256, etc.).
   * @param signedContent Le contenu signé (bytes).
   * @param signature La signature brute.
   * @param publicKey La clé publique de l'émetteur.
   * @return true si la signature est valide.
   */
  public boolean verify(String alg, byte[] signedContent, byte[] signature, PublicKey publicKey) {
    try {
      final String jcaAlg;
      if (alg == null) {
        return false;
      }
      switch (alg) {
        case "RS256":
          jcaAlg = "SHA256withRSA";
          break;
        case "RS384":
          jcaAlg = "SHA384withRSA";
          break;
        case "RS512":
          jcaAlg = "SHA512withRSA";
          break;
        case "ES256":
          jcaAlg = "SHA256withECDSA";
          break;
        case "ES384":
          jcaAlg = "SHA384withECDSA";
          break;
        case "ES512":
          jcaAlg = "SHA512withECDSA";
          break;
        case "PS256":
        case "PS384":
        case "PS512":
          jcaAlg = "RSASSA-PSS";
          break;
        default:
          return false;
      }

      final Signature sig = Signature.getInstance(jcaAlg);

      if (alg.startsWith("PS")) {
        final java.security.spec.PSSParameterSpec pssSpec;
        if ("PS256".equals(alg)) {
          pssSpec =
              new java.security.spec.PSSParameterSpec(
                  "SHA-256", "MGF1", java.security.spec.MGF1ParameterSpec.SHA256, 32, 1);
        } else if ("PS384".equals(alg)) {
          pssSpec =
              new java.security.spec.PSSParameterSpec(
                  "SHA-384", "MGF1", java.security.spec.MGF1ParameterSpec.SHA384, 48, 1);
        } else {
          pssSpec =
              new java.security.spec.PSSParameterSpec(
                  "SHA-512", "MGF1", java.security.spec.MGF1ParameterSpec.SHA512, 64, 1);
        }
        sig.setParameter(pssSpec);
      }

      sig.initVerify(publicKey);
      sig.update(signedContent);

      byte[] signatureBytes = signature;
      if (alg.startsWith("ES")) {
        signatureBytes = transcodeSignatureToDER(signature);
      }

      return sig.verify(signatureBytes);
    } catch (Exception e) {
      return false;
    }
  }

  byte[] transcodeSignatureToDER(byte[] jwsSignature) throws Exception {
    final int rawLength = jwsSignature.length;
    final int keyLength = rawLength / 2;

    final byte[] rBytes = new byte[keyLength];
    final byte[] sBytes = new byte[keyLength];
    System.arraycopy(jwsSignature, 0, rBytes, 0, keyLength);
    System.arraycopy(jwsSignature, keyLength, sBytes, 0, keyLength);

    int rOffset = 0;
    while (rOffset < keyLength - 1 && rBytes[rOffset] == 0) {
      rOffset++;
    }
    int sOffset = 0;
    while (sOffset < keyLength - 1 && sBytes[sOffset] == 0) {
      sOffset++;
    }

    final int rLen = keyLength - rOffset;
    final int sLen = keyLength - sOffset;

    final boolean rPad = (rBytes[rOffset] & 0x80) != 0;
    final boolean sPad = (sBytes[sOffset] & 0x80) != 0;

    final int rEncodedLen = rLen + (rPad ? 1 : 0);
    final int sEncodedLen = sLen + (sPad ? 1 : 0);

    final int seqLen = 2 + rEncodedLen + 2 + sEncodedLen;

    final byte[] der;
    int pos = 0;
    if (seqLen < 128) {
      der = new byte[2 + seqLen];
      der[pos++] = 0x30;
      der[pos++] = (byte) seqLen;
    } else {
      der = new byte[3 + seqLen];
      der[pos++] = 0x30;
      der[pos++] = (byte) 0x81;
      der[pos++] = (byte) seqLen;
    }

    der[pos++] = 0x02;
    der[pos++] = (byte) rEncodedLen;
    if (rPad) {
      der[pos++] = 0x00;
    }
    System.arraycopy(rBytes, rOffset, der, pos, rLen);
    pos += rLen;

    der[pos++] = 0x02;
    der[pos++] = (byte) sEncodedLen;
    if (sPad) {
      der[pos++] = 0x00;
    }
    System.arraycopy(sBytes, sOffset, der, pos, sLen);

    return der;
  }
}
