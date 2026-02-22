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
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * JAR (RFC 9101) implementation of AuthorizationRequestConverter. Converts plain parameters into a
 * signed JWT 'request' parameter.
 */
public class JarAuthorizationRequestConverter implements AuthorizationRequestConverter {

  private final RequestObjectService requestObjectService;

  public JarAuthorizationRequestConverter(
      String clientId, String audience, JWK signingJWK, JWSAlgorithm jwsAlgorithm) {
    this(clientId, audience, signingJWK, jwsAlgorithm, null, null, null);
  }

  public JarAuthorizationRequestConverter(
      String clientId,
      String audience,
      Supplier<JWK> signingJWKSupplier,
      JWSAlgorithm jwsAlgorithm) {
    this(clientId, audience, signingJWKSupplier, jwsAlgorithm, null, null, null);
  }

  public JarAuthorizationRequestConverter(
      String clientId,
      String audience,
      JWK signingJWK,
      JWSAlgorithm jwsAlgorithm,
      JWK encryptionJWK,
      com.nimbusds.jose.JWEAlgorithm jweAlgorithm,
      com.nimbusds.jose.EncryptionMethod encryptionMethod) {
    this(
        clientId,
        audience,
        () -> signingJWK,
        jwsAlgorithm,
        encryptionJWK,
        jweAlgorithm,
        encryptionMethod);
  }

  public JarAuthorizationRequestConverter(
      String clientId,
      String audience,
      Supplier<JWK> signingJWKSupplier,
      JWSAlgorithm jwsAlgorithm,
      JWK encryptionJWK,
      com.nimbusds.jose.JWEAlgorithm jweAlgorithm,
      com.nimbusds.jose.EncryptionMethod encryptionMethod) {
    this.requestObjectService =
        new RequestObjectService(
            clientId,
            audience,
            signingJWKSupplier,
            jwsAlgorithm,
            encryptionJWK,
            jweAlgorithm,
            encryptionMethod);
  }

  @Override
  public Map<String, String> convert(Map<String, String> params) {
    final String requestJwt = requestObjectService.createRequestObject(params);

    final Map<String, String> newParams = new HashMap<>();
    // RFC 9101: "request" parameter MUST be present
    newParams.put("request", requestJwt);
    // RFC 9101: "client_id" MUST be present in the query parameters as well
    if (params.containsKey("client_id")) {
      newParams.put("client_id", params.get("client_id"));
    }
    return newParams;
  }
}
