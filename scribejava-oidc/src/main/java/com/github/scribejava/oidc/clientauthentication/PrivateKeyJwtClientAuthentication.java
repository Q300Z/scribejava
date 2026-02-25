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
package com.github.scribejava.oidc.clientauthentication;

import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.oauth2.clientauthentication.ClientAuthentication;
import com.github.scribejava.oidc.model.JwtBuilder;
import com.github.scribejava.oidc.model.JwtSigner;
import java.security.PrivateKey;
import java.util.UUID;

/** Authentification du client via un jeton porteur JSON Web Token (JWT). */
public class PrivateKeyJwtClientAuthentication implements ClientAuthentication {

  private static final int EXPIRATION_TIMEOUT_MS = 5 * 60 * 1000;
  private final String clientId;
  private final String audience;
  private final PrivateKey privateKey;
  private final String keyId;
  private final JwtSigner signer;

  /**
   * @param clientId client id
   * @param audience audience
   * @param privateKey clé privée
   * @param keyId id de la clé (facultatif)
   * @param signer signataire
   */
  public PrivateKeyJwtClientAuthentication(
      String clientId, String audience, PrivateKey privateKey, String keyId, JwtSigner signer) {
    this.clientId = clientId;
    this.audience = audience;
    this.privateKey = privateKey;
    this.keyId = keyId;
    this.signer = signer;
  }

  @Override
  public void addClientAuthentication(
      final OAuthRequest request, final String apiKey, final String apiSecret) {
    addClientAuthentication(request);
  }

  /**
   * Ajoute l'assertion JWT d'authentification client à la requête.
   *
   * @param request La requête HTTP à laquelle ajouter les paramètres {@code client_assertion} et
   *     {@code client_assertion_type}.
   */
  public void addClientAuthentication(final OAuthRequest request) {
    final String assertion = createAssertion();
    request.addBodyParameter(
        "client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer");
    request.addBodyParameter("client_assertion", assertion);
  }

  private String createAssertion() {
    final long now = System.currentTimeMillis();
    final JwtBuilder builder =
        new JwtBuilder()
            .claim("iss", clientId)
            .claim("sub", clientId)
            .claim("aud", audience)
            .claim("exp", (now + EXPIRATION_TIMEOUT_MS) / 1000)
            .claim("iat", now / 1000)
            .claim("jti", UUID.randomUUID().toString());

    if (keyId != null) {
      builder.header("kid", keyId);
    }

    try {
      return builder.buildAndSign(signer, privateKey);
    } catch (OAuthException e) {
      throw new OAuthException("Error signing client assertion JWT", e);
    }
  }
}
