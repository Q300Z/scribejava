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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class JwksParserEdgeCasesTest {

  @Test
  public void shouldThrowOnInvalidJwksMissingKeysArray() {
    final JwksParser parser = new JwksParser();
    assertThrows(IOException.class, () -> parser.parse("{}"));
    assertThrows(IOException.class, () -> parser.parse("{\"keys\": \"not-a-list\"}"));
  }

  @Test
  public void shouldSkipNullAndUnknownKeyTypes() throws Exception {
    final JwksParser parser = new JwksParser();
    final String json = "{\"keys\": [null, {\"kty\": \"OCT\", \"kid\": \"key1\"}]}";
    final Map<String, OidcKey> result = parser.parse(json);
    assertThat(result).isEmpty();
  }

  @Test
  public void shouldThrowOnMissingRsaComponents() {
    final JwksParser parser = new JwksParser();
    final Map<String, Object> node1 = new HashMap<>();
    node1.put("kty", "RSA");
    node1.put("kid", "1");
    node1.put("n", "some-modulus");
    // missing e

    assertThrows(IOException.class, () -> parser.parseKey(node1));

    final Map<String, Object> node2 = new HashMap<>();
    node2.put("kty", "RSA");
    node2.put("kid", "2");
    node2.put("e", "AQAB");
    // missing n

    assertThrows(IOException.class, () -> parser.parseKey(node2));
  }

  @Test
  public void shouldThrowOnInvalidRsaKeySpec() {
    final JwksParser parser = new JwksParser();
    final Map<String, Object> node = new HashMap<>();
    node.put("kty", "RSA");
    node.put("kid", "1");
    // invalid base64 values
    node.put("n", "###invalid-base64###");
    node.put("e", "AQAB");

    assertThrows(IllegalArgumentException.class, () -> parser.parseKey(node));
  }

  @Test
  public void shouldThrowOnMissingEcComponents() {
    final JwksParser parser = new JwksParser();
    final Map<String, Object> node = new HashMap<>();
    node.put("kty", "EC");
    node.put("kid", "1");
    node.put("crv", "P-256");
    node.put("x", "f83OJ3D2xF1Bg8vub9tLe1gHMzV76e8Tus9uPHvRVEU");
    // missing y

    assertThrows(IOException.class, () -> parser.parseKey(node));
  }

  @Test
  public void shouldFallbackToSec256r1OnInvalidCrv() throws Exception {
    final JwksParser parser = new JwksParser();
    final Map<String, Object> node = new HashMap<>();
    node.put("kty", "EC");
    node.put("kid", "1");
    node.put("crv", "INVALID_CRV");
    // coordinates for a valid P-256 (secp256r1) key
    node.put("x", "f83OJ3D2xF1Bg8vub9tLe1gHMzV76e8Tus9uPHvRVEU");
    node.put("y", "x_FEzRu9m36HLN_tue659LNpXW6pCyStikYjKIWI5a0");

    final OidcKey key = parser.parseKey(node);
    assertThat(key).isNotNull();
    assertThat(key.getKid()).isEqualTo("1");
    assertThat(key).isInstanceOf(EcOidcKey.class);
  }

  @Test
  public void shouldThrowOnInvalidEcSpec() {
    final JwksParser parser = new JwksParser();
    final Map<String, Object> node = new HashMap<>();
    node.put("kty", "EC");
    node.put("kid", "1");
    node.put("crv", "P-256");
    node.put("x", "###invalid-base64###");
    node.put("y", "x_FEzRu9m36HLN_tue659LNpXW6pCyStikYjKIWI5a0");

    assertThrows(IllegalArgumentException.class, () -> parser.parseKey(node));
  }

  @Test
  public void shouldReturnNullOnNullKeyNode() throws Exception {
    final JwksParser parser = new JwksParser();
    assertThat(parser.parseKey(null)).isNull();
  }
}
