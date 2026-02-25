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
import com.github.scribejava.oidc.model.JwtSigner;
import java.security.PrivateKey;

/** Constructeur de service (ServiceBuilder) spécialisé pour les extensions OpenID Connect. */
public class OidcServiceBuilder extends ServiceBuilder {

  /**
   * @param apiKey La clé API du client (Client ID).
   */
  public OidcServiceBuilder(String apiKey) {
    super(apiKey);
  }

  /**
   * @param audience audience
   * @param signingKey clé privée
   * @param keyId id clé
   * @param signer signataire
   * @return builder
   */
  public OidcServiceBuilder jwtSecuredAuthorizationRequest(
      String audience, PrivateKey signingKey, String keyId, JwtSigner signer) {
    this.authorizationRequestConverter(
        new JarAuthorizationRequestConverter(
            this.getApiKey(), audience, signingKey, keyId, signer));
    return this;
  }

  @Override
  public com.github.scribejava.core.oauth.AuthorizationRequestConverter
      getAuthorizationRequestConverter() {
    return super.getAuthorizationRequestConverter();
  }
}
