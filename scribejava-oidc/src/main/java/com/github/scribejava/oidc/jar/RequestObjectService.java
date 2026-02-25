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
package com.github.scribejava.oidc.jar;

import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.oidc.model.JwtBuilder;
import com.github.scribejava.oidc.model.JwtSigner;
import java.security.PrivateKey;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/** Service de création d'objets de requête signés (Request Objects) natif. */
public class RequestObjectService {

  private final String clientId;
  private final String audience;
  private final Supplier<PrivateKey> signingKeySupplier;
  private final String keyId;
  private final JwtSigner signer;

  /**
   * @param clientId client id
   * @param audience audience
   * @param signingKeySupplier fournisseur clé privée
   * @param keyId id clé
   * @param signer signataire
   */
  public RequestObjectService(
      String clientId,
      String audience,
      Supplier<PrivateKey> signingKeySupplier,
      String keyId,
      JwtSigner signer) {
    this.clientId = clientId;
    this.audience = audience;
    this.signingKeySupplier = signingKeySupplier;
    this.keyId = keyId;
    this.signer = signer;
  }

  /**
   * Crée un objet de requête JWT (Request Object).
   *
   * @param authorizationParams paramètres
   * @return JWT
   */
  public String createRequestObject(Map<String, String> authorizationParams) {
    final PrivateKey privateKey = signingKeySupplier.get();
    if (privateKey == null) {
      throw new OAuthException("Private key is missing for signing Request Object.");
    }
    final long now = System.currentTimeMillis() / 1000;

    final JwtBuilder builder =
        new JwtBuilder()
            .claim("iss", clientId)
            .claim("aud", audience)
            .claim("iat", now)
            .claim("jti", UUID.randomUUID().toString());

    if (keyId != null) {
      builder.header("kid", keyId);
    }

    authorizationParams.forEach(builder::claim);

    if (!authorizationParams.containsKey("client_id")) {
      builder.claim("client_id", clientId);
    }

    return builder.buildAndSign(signer, privateKey);
  }
}
