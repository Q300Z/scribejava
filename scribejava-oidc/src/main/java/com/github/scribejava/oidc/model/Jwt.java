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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

/**
 * Représentation native d'un JSON Web Token (JWT).
 *
 * <p>Permet de parser et d'accéder aux composants d'un jeton sans dépendre de bibliothèques tiers
 * complexes de gestion de session.
 */
public class Jwt {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private final Map<String, Object> header;
  private final Map<String, Object> payload;
  private final byte[] signature;
  private final String signedContent;

  private Jwt(
      Map<String, Object> header,
      Map<String, Object> payload,
      byte[] signature,
      String signedContent) {
    this.header = header;
    this.payload = payload;
    this.signature = signature;
    this.signedContent = signedContent;
  }

  /**
   * Parse une chaîne brute en objet Jwt.
   *
   * @param rawToken Le token au format header.payload.signature.
   * @return Une instance de {@link Jwt}.
   * @throws IllegalArgumentException si le format est invalide.
   */
  public static Jwt parse(String rawToken) {
    final String[] parts = rawToken.split("\\.");
    if (parts.length != 3) {
      throw new IllegalArgumentException("Invalid JWT format. Expected 3 parts separated by dots.");
    }

    try {
      final Map<String, Object> header = MAPPER.readValue(decode(parts[0]), Map.class);
      final Map<String, Object> payload = MAPPER.readValue(decode(parts[1]), Map.class);
      final byte[] signature = decode(parts[2]);
      final String signedContent = parts[0] + "." + parts[1];

      return new Jwt(header, payload, signature, signedContent);
    } catch (IOException e) {
      throw new IllegalArgumentException("Failed to parse JWT JSON content.", e);
    }
  }

  private static byte[] decode(String part) {
    return Base64.getUrlDecoder().decode(part);
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

  public String getSignedContent() {
    return signedContent;
  }
}
