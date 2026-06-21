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

import java.security.Provider;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implémentation par défaut de {@link SignatureVerifier} héritant de {@link JwtSignatureVerifier}.
 * Permet d'enregistrer des algorithmes personnalisés et de spécifier un Provider JCA.
 */
public class DefaultSignatureVerifier extends JwtSignatureVerifier implements SignatureVerifier {

  private final Map<String, String> algorithmMapping = new ConcurrentHashMap<>();
  private Provider provider;
  private String providerName;

  public DefaultSignatureVerifier() {
    algorithmMapping.put("RS256", "SHA256withRSA");
    algorithmMapping.put("RS384", "SHA384withRSA");
    algorithmMapping.put("RS512", "SHA512withRSA");
    algorithmMapping.put("ES256", "SHA256withECDSA");
    algorithmMapping.put("ES384", "SHA384withECDSA");
    algorithmMapping.put("ES512", "SHA512withECDSA");
    algorithmMapping.put("PS256", "RSASSA-PSS");
    algorithmMapping.put("PS384", "RSASSA-PSS");
    algorithmMapping.put("PS512", "RSASSA-PSS");
  }

  public void registerAlgorithm(String alg, String jcaAlg) {
    algorithmMapping.put(alg, jcaAlg);
  }

  public Provider getProvider() {
    return provider;
  }

  public void setProvider(Provider provider) {
    this.provider = provider;
    this.providerName = null;
  }

  public String getProviderName() {
    return providerName;
  }

  public void setProviderName(String providerName) {
    this.providerName = providerName;
    this.provider = null;
  }

  @Override
  public boolean verify(String alg, byte[] signedContent, byte[] signature, PublicKey publicKey) {
    if (alg == null) {
      return false;
    }

    final String jcaAlg = algorithmMapping.get(alg);
    if (jcaAlg == null) {
      return super.verify(alg, signedContent, signature, publicKey);
    }

    try {
      final Signature sig;
      if (provider != null) {
        sig = Signature.getInstance(jcaAlg, provider);
      } else if (providerName != null) {
        sig = Signature.getInstance(jcaAlg, providerName);
      } else {
        sig = Signature.getInstance(jcaAlg);
      }

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
}
