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
package com.github.scribejava.oidc;

import com.github.scribejava.oidc.model.OidcKey;
import com.github.scribejava.oidc.model.RsaOidcKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import java.util.HashMap;
import java.util.Map;

/** Utilitaire de test pour convertir les objets Nimbus en objets natifs ScribeJava. */
public class OidcTestFixture {

  private OidcTestFixture() {}

  /**
   * @param nimbusSet set nimbus
   * @return map native
   * @throws Exception erreur
   */
  public static Map<String, OidcKey> convert(JWKSet nimbusSet) throws Exception {
    final Map<String, OidcKey> map = new HashMap<>();
    if (nimbusSet == null) {
      return map;
    }
    for (final JWK jwk : nimbusSet.getKeys()) {
      if (jwk instanceof RSAKey) {
        final RSAKey rsa = (RSAKey) jwk;
        map.put(rsa.getKeyID(), new RsaOidcKey(rsa.getKeyID(), "RS256", rsa.toPublicKey()));
      }
    }
    return map;
  }
}
