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

import java.security.PrivateKey;
import java.util.LinkedHashMap;
import java.util.Map;

/** Constructeur fluide pour JWT natif. */
public class JwtBuilder {

  private final Map<String, Object> header = new LinkedHashMap<>();
  private final Map<String, Object> payload = new LinkedHashMap<>();

  public JwtBuilder header(String name, Object value) {
    header.put(name, value);
    return this;
  }

  public JwtBuilder payload(String name, Object value) {
    payload.put(name, value);
    return this;
  }

  public JwtBuilder claim(String name, Object value) {
    return payload(name, value);
  }

  public String buildAndSign(JwtSigner signer, PrivateKey privateKey) {
    header.put("alg", signer.getAlgorithm());
    final String hBase64 = JwtSerializer.serialize(header);
    final String pBase64 = JwtSerializer.serialize(payload);
    final String content = hBase64 + "." + pBase64;
    final String signature = signer.sign(content, privateKey);
    return content + "." + signature;
  }
}
