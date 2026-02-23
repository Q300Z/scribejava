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
import com.github.scribejava.core.utils.OAuthEncoder;
import com.github.scribejava.core.utils.Preconditions;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * HMAC-SHA1 implementation of {@link SignatureService} <a
 * href="https://tools.ietf.org/html/rfc5849#section-3.4.2">...</a>
 */
public class HMACSha1SignatureService implements SignatureService {

  private static final String EMPTY_STRING = "";
  private static final String CARRIAGE_RETURN = "\r\n";
  private static final String HMAC_SHA1 = "HmacSHA1";
  private static final String METHOD = "HMAC-SHA1";

  /** {@inheritDoc} */
  @Override
  public String getSignature(String baseString, String apiSecret, String tokenSecret) {
    try {
      Preconditions.checkEmptyString(baseString, "Base string can't be null or empty string");
      Preconditions.checkNotNull(apiSecret, "Api secret can't be null");
      return doSign(
          baseString, OAuthEncoder.encode(apiSecret) + '&' + OAuthEncoder.encode(tokenSecret));
    } catch (NoSuchAlgorithmException | InvalidKeyException | RuntimeException e) {
      throw new OAuthSignatureException(baseString, e);
    }
  }

  private String doSign(String toSign, String keyString)
      throws NoSuchAlgorithmException, InvalidKeyException {
    final SecretKeySpec key =
        new SecretKeySpec(keyString.getBytes(StandardCharsets.UTF_8), HMAC_SHA1);
    final Mac mac = Mac.getInstance(HMAC_SHA1);
    mac.init(key);
    final byte[] bytes = mac.doFinal(toSign.getBytes(StandardCharsets.UTF_8));
    return Base64.getEncoder().encodeToString(bytes).replace(CARRIAGE_RETURN, EMPTY_STRING);
  }

  /** {@inheritDoc} */
  @Override
  public String getSignatureMethod() {
    return METHOD;
  }
}
