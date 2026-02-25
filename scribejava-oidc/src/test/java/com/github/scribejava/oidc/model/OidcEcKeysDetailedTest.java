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

import static org.assertj.core.api.Assertions.assertThat;

import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Tests pour le support des clés Elliptic Curve (EC). */
public class OidcEcKeysDetailedTest {

  /**
   * Vérifie le parsing d'une clé EC P-256.
   *
   * @throws Exception en cas d'échec cryptographique
   */
  @Test
  public void shouldParseEcP256Key() throws Exception {
    final Map<String, Object> jwk = new HashMap<>();
    jwk.put("kty", "EC");
    jwk.put("crv", "P-256");
    jwk.put("x", "f83OJ3D2xF1Bg8vub9tLe1gHMzV76e8Tus9uPHvRVEU");
    jwk.put("y", "x_FEzRu9m36HLN_tue659LNpXW6pCyStikYjKIWI5a0");
    jwk.put("kid", "key1");

    final JwksParser parser = new JwksParser();
    final PublicKey key = parser.parseKey(jwk).getPublicKey();
    assertThat(key).isInstanceOf(ECPublicKey.class);
    assertThat(((ECPublicKey) key).getParams().getCurve().getField().getFieldSize()).isEqualTo(256);
  }
}
