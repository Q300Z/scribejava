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
package com.github.scribejava.oauth1.services;

import com.github.scribejava.core.exceptions.OAuthSignatureException;
import com.github.scribejava.core.services.SignatureService;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

/** A signature service that uses the RSA-SHA1 algorithm. */
public class RSASha1SignatureService implements SignatureService {

  private static final String METHOD = "RSA-SHA1";
  private static final String RSA_SHA1 = "SHA1withRSA";

  private final PrivateKey privateKey;

  public RSASha1SignatureService(PrivateKey privateKey) {
    this.privateKey = privateKey;
  }

  /** {@inheritDoc} */
  @Override
  public String getSignature(String baseString, String apiSecret, String tokenSecret) {
    try {
      final Signature signature = Signature.getInstance(RSA_SHA1);
      signature.initSign(privateKey);
      signature.update(baseString.getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(signature.sign());
    } catch (NoSuchAlgorithmException
        | InvalidKeyException
        | SignatureException
        | RuntimeException e) {
      throw new OAuthSignatureException(baseString, e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public String getSignatureMethod() {
    return METHOD;
  }
}
