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

import com.github.scribejava.core.utils.JsonUtils;
import java.io.IOException;
import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Parseur natif pour les ensembles de clés (JWKS). */
public class JwksParser {

  /**
   * Parse un corps JSON JWKS.
   *
   * @param json Le corps de réponse JSON.
   * @return Une Map liant kid à OidcKey.
   * @throws IOException si le parsing échoue.
   */
  @SuppressWarnings("unchecked")
  public Map<String, OidcKey> parse(String json) throws IOException {
    final Map<String, Object> root = JsonUtils.parse(json);
    final Object keysNode = root.get("keys");
    if (!(keysNode instanceof List)) {
      throw new IOException("Invalid JWKS: 'keys' array missing.");
    }

    final List<Map<String, Object>> keyList = (List<Map<String, Object>>) keysNode;
    final Map<String, OidcKey> keys = new HashMap<>();
    for (final Map<String, Object> keyNode : keyList) {
      final OidcKey key = parseKey(keyNode);
      if (key != null) {
        keys.put(key.getKid(), key);
      }
    }
    return keys;
  }

  /**
   * Parse une clé individuelle.
   *
   * @param keyNode Map représentant le JWK
   * @return OidcKey ou null si type inconnu
   * @throws IOException si erreur de parsing
   */
  public OidcKey parseKey(Map<String, Object> keyNode) throws IOException {
    final String kty = (String) keyNode.get("kty");
    if ("RSA".equals(kty)) {
      return parseRsaKey(keyNode);
    } else if ("EC".equals(kty)) {
      return parseEcKey(keyNode);
    }
    return null;
  }

  private OidcKey parseRsaKey(Map<String, Object> node) throws IOException {
    final String kid = (String) node.get("kid");
    final String alg = node.containsKey("alg") ? (String) node.get("alg") : "RS256";
    final String nStr = (String) node.get("n");
    final String eStr = (String) node.get("e");

    if (nStr == null || eStr == null) {
      throw new IOException("Missing RSA components (n, e) in JWK");
    }

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

  private OidcKey parseEcKey(Map<String, Object> node) throws IOException {
    final String kid = (String) node.get("kid");
    final String alg = node.containsKey("alg") ? (String) node.get("alg") : "ES256";
    final String crv = (String) node.get("crv");
    final String xStr = (String) node.get("x");
    final String yStr = (String) node.get("y");

    if (crv == null || xStr == null || yStr == null) {
      throw new IOException("Missing EC components (crv, x, y) in JWK");
    }

    final BigInteger x = new BigInteger(1, Base64.getUrlDecoder().decode(xStr));
    final BigInteger y = new BigInteger(1, Base64.getUrlDecoder().decode(yStr));

    try {
      final AlgorithmParameters params = AlgorithmParameters.getInstance("EC");
      // Essayer d'initialiser via le nom de la courbe directement (certains JDK acceptent String)
      try {
        params.init(new ECGenParameterSpec(crv));
      } catch (Exception e) {
        // Fallback pour les environnements restreints : forcer le nom standard NIST
        params.init(new ECGenParameterSpec("secp256r1"));
      }
      final ECParameterSpec ecParameters = params.getParameterSpec(ECParameterSpec.class);

      final KeyFactory kf = KeyFactory.getInstance("EC");
      final PublicKey publicKey =
          kf.generatePublic(new ECPublicKeySpec(new ECPoint(x, y), ecParameters));
      return new EcOidcKey(kid, alg, publicKey);
    } catch (Exception ex) {
      throw new IOException("Failed to reconstruct EC Public Key from JWK", ex);
    }
  }
}
