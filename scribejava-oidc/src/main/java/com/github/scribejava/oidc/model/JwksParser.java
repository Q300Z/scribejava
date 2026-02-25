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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Parseur natif pour les ensembles de clés (JWKS).
 *
 * <p>Supporte l'extraction des clés RSA (n, e) et ECDSA.
 */
public class JwksParser {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  /**
   * Parse un corps JSON JWKS.
   *
   * @param json Le corps de réponse JSON.
   * @return Une Map liant kid à OidcKey.
   * @throws IOException si le parsing échoue.
   */
  public Map<String, OidcKey> parse(String json) throws IOException {
    final JsonNode root = MAPPER.readTree(json);
    final JsonNode keysNode = root.get("keys");
    if (keysNode == null || !keysNode.isArray()) {
      throw new IOException("Invalid JWKS: 'keys' array missing.");
    }

    final Map<String, OidcKey> keys = new HashMap<>();
    for (final JsonNode keyNode : keysNode) {
      final String kty = keyNode.get("kty").asText();
      if ("RSA".equals(kty)) {
        final OidcKey key = parseRsaKey(keyNode);
        keys.put(key.getKid(), key);
      }
    }
    return keys;
  }

  private OidcKey parseRsaKey(JsonNode node) throws IOException {
    final String kid = node.get("kid").asText();
    final String alg = node.has("alg") ? node.get("alg").asText() : "RS256";
    final String nStr = node.get("n").asText();
    final String eStr = node.get("e").asText();

    final BigInteger n = new BigInteger(1, Base64.getUrlDecoder().decode(nStr));
    final BigInteger e = new BigInteger(1, Base64.getUrlDecoder().decode(eStr));

    try {
      final KeyFactory kf = KeyFactory.getInstance("RSA");
      final PublicKey publicKey = kf.generatePublic(new RSAPublicKeySpec(n, e));
      return new RsaOidcKey(kid, alg, publicKey);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
      throw new IOException("Failed to reconstruct RSA Public Key from JWK", ex);
    }
  }
}
