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
package com.github.scribejava.oidc.builder;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.oidc.jar.JarAuthorizationRequestConverter;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import java.util.function.Supplier;

/**
 * Specialized ServiceBuilder for OpenID Connect extensions. Provides convenient methods for JAR
 * (RFC 9101) configuration.
 */
public class OidcServiceBuilder extends ServiceBuilder {

  public OidcServiceBuilder(String apiKey) {
    super(apiKey);
  }

  /**
   * Enables JWT-Secured Authorization Request (JAR) - RFC 9101.
   *
   * @param audience The audience for the Request Object (usually the Issuer URL of the OP).
   * @param signingKey The client's private key (JWK) used to sign the request.
   * @param jwsAlgorithm The signing algorithm (e.g. RS256).
   * @return this builder
   */
  public OidcServiceBuilder jwtSecuredAuthorizationRequest(
      String audience, JWK signingKey, JWSAlgorithm jwsAlgorithm) {
    // We use the apiKey (clientId) as the issuer of the Request Object
    this.authorizationRequestConverter(
        new JarAuthorizationRequestConverter(this.getApiKey(), audience, signingKey, jwsAlgorithm));
    return this;
  }

  /**
   * Enables JWT-Secured Authorization Request (JAR) with Key Rollover support.
   *
   * @param audience The audience for the Request Object (usually the Issuer URL of the OP).
   * @param signingKeySupplier A supplier that returns the current client private key (JWK).
   * @param jwsAlgorithm The signing algorithm (e.g. RS256).
   * @return this builder
   */
  public OidcServiceBuilder jwtSecuredAuthorizationRequest(
      String audience, Supplier<JWK> signingKeySupplier, JWSAlgorithm jwsAlgorithm) {
    this.authorizationRequestConverter(
        new JarAuthorizationRequestConverter(
            this.getApiKey(), audience, signingKeySupplier, jwsAlgorithm));
    return this;
  }
}
