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

import com.github.scribejava.core.oauth.AuthorizationRequestConverter;
import com.github.scribejava.oidc.model.JwtSigner;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Map;

/** Implémentation JAR (JWT-Secured Authorization Request) natif. */
public class JarAuthorizationRequestConverter implements AuthorizationRequestConverter {

  private final RequestObjectService requestObjectService;

  /**
   * @param clientId client id
   * @param audience audience
   * @param signingKey clé privée
   * @param keyId id clé
   * @param signer signataire
   */
  public JarAuthorizationRequestConverter(
      String clientId, String audience, PrivateKey signingKey, String keyId, JwtSigner signer) {
    this.requestObjectService =
        new RequestObjectService(clientId, audience, () -> signingKey, keyId, signer);
  }

  @Override
  public Map<String, String> convert(Map<String, String> params) {
    final String requestJwt = requestObjectService.createRequestObject(params);

    final Map<String, String> newParams = new HashMap<>();
    newParams.put("request", requestJwt);
    if (params.containsKey("client_id")) {
      newParams.put("client_id", params.get("client_id"));
    }
    return newParams;
  }
}
