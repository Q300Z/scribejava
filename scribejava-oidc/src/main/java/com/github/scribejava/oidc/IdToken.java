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

import com.github.scribejava.core.model.Token;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import java.util.Map;

/**
 * Représente un jeton d'identité (ID Token) OpenID Connect.
 *
 * <p>Le jeton d'identité est un jeton de sécurité qui contient des revendications (claims) sur
 * l'authentification d'un utilisateur final par un serveur d'autorisation. Il est représenté sous
 * forme de JSON Web Token (JWT).
 *
 * @see <a href="http://openid.net/specs/openid-connect-core-1_0.html#IDToken">OpenID Connect Core
 *     1.0, Section 2 (ID Token)</a>
 */
public class IdToken extends Token {

  private static final long serialVersionUID = 1L;
  private final String rawToken;
  private final JWTClaimsSet claimsSet;

  /**
   * Construit un ID Token à partir de sa représentation textuelle brute (JWT).
   *
   * @param rawToken Le jeton au format JWT sérialisé.
   * @throws com.github.scribejava.core.exceptions.OAuthException si le jeton ne peut pas être
   *     analysé.
   */
  public IdToken(final String rawToken) {
    super(rawToken);
    this.rawToken = rawToken;
    try {
      this.claimsSet = SignedJWT.parse(rawToken).getJWTClaimsSet();
    } catch (final ParseException e) {
      throw new com.github.scribejava.core.exceptions.OAuthException("Failed to parse ID Token", e);
    }
  }

  /**
   * Retourne la réponse brute du jeton.
   *
   * @return Le jeton sous sa forme brute.
   */
  @Override
  public String getRawResponse() {
    return rawToken;
  }

  /**
   * Retourne l'ensemble des revendications (Claims) contenues dans le jeton.
   *
   * @return L'instance de {@link JWTClaimsSet}.
   */
  public JWTClaimsSet getClaimsSet() {
    return claimsSet;
  }

  /**
   * Retourne l'identifiant du sujet (End-User).
   *
   * @return La valeur de la revendication "sub".
   * @see <a href="http://openid.net/specs/openid-connect-core-1_0.html#IDToken">Section 2, Claim
   *     "sub"</a>
   */
  public String getSubject() {
    return claimsSet.getSubject();
  }

  /**
   * Retourne l'identifiant de l'émetteur (Issuer).
   *
   * @return La valeur de la revendication "iss".
   * @see <a href="http://openid.net/specs/openid-connect-core-1_0.html#IDToken">Section 2, Claim
   *     "iss"</a>
   */
  public String getIssuer() {
    return claimsSet.getIssuer();
  }

  /**
   * Retourne la valeur de nonce utilisée pour atténuer les attaques par rejeu.
   *
   * @return La valeur de la revendication "nonce", ou null si absente.
   * @see <a href="http://openid.net/specs/openid-connect-core-1_0.html#IDToken">Section 2, Claim
   *     "nonce"</a>
   */
  public String getNonce() {
    return (String) claimsSet.getClaim("nonce");
  }

  /**
   * Retourne les revendications standards extraites du jeton.
   *
   * @return Une instance de {@link StandardClaims}.
   * @see <a href="http://openid.net/specs/openid-connect-core-1_0.html#StandardClaims">Section 5.1
   *     (Standard Claims)</a>
   */
  public StandardClaims getStandardClaims() {
    return new StandardClaims(claimsSet.getClaims());
  }

  /**
   * Retourne une revendication spécifique par son nom.
   *
   * @param name Le nom de la revendication.
   * @return La valeur de la revendication, ou null si elle n'existe pas.
   */
  public Object getClaim(final String name) {
    return claimsSet.getClaim(name);
  }

  /**
   * Retourne toutes les revendications sous forme de Map.
   *
   * @return Un dictionnaire contenant les noms et valeurs des revendications.
   */
  public Map<String, Object> getClaims() {
    return claimsSet.getClaims();
  }
}
