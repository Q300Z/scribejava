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
 * Constructeur de service (ServiceBuilder) spécialisé pour les extensions OpenID Connect.
 *
 * <p>Fournit des méthodes pratiques pour la configuration de JAR (RFC 9101).
 *
 * @see <a href="https://tools.ietf.org/html/rfc9101">RFC 9101 (OAuth 2.0 JAR)</a>
 */
public class OidcServiceBuilder extends ServiceBuilder {

  /**
   * Constructeur.
   *
   * @param apiKey La clé API du client (Client ID).
   */
  public OidcServiceBuilder(String apiKey) {
    super(apiKey);
  }

  /**
   * Active les requêtes d'autorisation sécurisées par JWT (JAR) - RFC 9101.
   *
   * @param audience L'audience de l'objet de requête (généralement l'URL de l'émetteur du
   *     fournisseur).
   * @param signingKey La clé privée du client (JWK) utilisée pour signer la requête.
   * @param jwsAlgorithm L'algorithme de signature (ex: RS256).
   * @return L'instance actuelle du builder.
   * @see <a href="https://tools.ietf.org/html/rfc9101">RFC 9101</a>
   */
  public OidcServiceBuilder jwtSecuredAuthorizationRequest(
      String audience, JWK signingKey, JWSAlgorithm jwsAlgorithm) {
    // We use the apiKey (clientId) as the issuer of the Request Object
    this.authorizationRequestConverter(
        new JarAuthorizationRequestConverter(this.getApiKey(), audience, signingKey, jwsAlgorithm));
    return this;
  }

  /**
   * Active les requêtes d'autorisation sécurisées par JWT (JAR) avec support du renouvellement des
   * clés (Key Rollover).
   *
   * @param audience L'audience de l'objet de requête.
   * @param signingKeySupplier Un fournisseur retournant la clé privée actuelle du client (JWK).
   * @param jwsAlgorithm L'algorithme de signature.
   * @return L'instance actuelle du builder.
   */
  public OidcServiceBuilder jwtSecuredAuthorizationRequest(
      String audience, Supplier<JWK> signingKeySupplier, JWSAlgorithm jwsAlgorithm) {
    this.authorizationRequestConverter(
        new JarAuthorizationRequestConverter(
            this.getApiKey(), audience, signingKeySupplier, jwsAlgorithm));
    return this;
  }

  @Override
  public com.github.scribejava.core.oauth.AuthorizationRequestConverter getAuthorizationRequestConverter() {
    return super.getAuthorizationRequestConverter();
  }
}
