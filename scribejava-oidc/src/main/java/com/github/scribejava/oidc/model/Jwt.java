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
import com.github.scribejava.core.utils.JsonUtils;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/** Représentation native et autonome d'un JSON Web Token (JWT). */
public class Jwt {

  private final Map<String, Object> header;
  private final Map<String, Object> payload;
  private final byte[] signature;
  private final String rawToken;
  private final String signedContent;

  public Jwt(
      Map<String, Object> header,
      Map<String, Object> payload,
      byte[] signature,
      String rawToken,
      String signedContent) {
    this.header = header;
    this.payload = payload;
    this.signature = signature;
    this.rawToken = rawToken;
    this.signedContent = signedContent;
  }

  /**
   * Parse un jeton JWT sérialisé.
   *
   * @param rawToken Le jeton au format String.
   * @return Une instance de Jwt.
   */
  public static Jwt parse(String rawToken) {
    if (rawToken == null) {
      throw new OAuthException("JWT token cannot be null");
    }
    final String[] parts = rawToken.split("\\.");
    if (parts.length != 3) {
      throw new OAuthException("Invalid JWT format. Expected 3 parts but found " + parts.length);
    }

    try {
      final Base64.Decoder decoder = Base64.getUrlDecoder();
      final String headerJson = new String(decoder.decode(parts[0]), StandardCharsets.UTF_8);
      final String payloadJson = new String(decoder.decode(parts[1]), StandardCharsets.UTF_8);
      final byte[] signature = decoder.decode(parts[2]);
      final String signedContent = parts[0] + "." + parts[1];

      return new Jwt(
          JsonUtils.parse(headerJson),
          JsonUtils.parse(payloadJson),
          signature,
          rawToken,
          signedContent);
    } catch (Exception ex) {
      throw new OAuthException("Failed to parse JWT: " + ex.getMessage(), ex);
    }
  }

  public byte[] getSignedContent() {
    return signedContent.getBytes(StandardCharsets.UTF_8);
  }

  public Map<String, Object> getHeader() {
    return header;
  }

  public Map<String, Object> getPayload() {
    return payload;
  }

  public byte[] getSignature() {
    return signature;
  }

  public String getRawToken() {
    return rawToken;
  }
}
