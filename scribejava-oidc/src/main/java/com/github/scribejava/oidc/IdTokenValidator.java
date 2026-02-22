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

import com.github.scribejava.core.exceptions.OAuthException;
import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet;
import com.nimbusds.openid.connect.sdk.validators.IDTokenValidator;
import java.util.Date;

/**
 * Validateur pour les jetons d'identité (ID Tokens) et les jetons de déconnexion (Logout Tokens)
 * OpenID Connect.
 *
 * <p>Cette classe assure la vérification de la signature, de l'émetteur, de l'audience, de
 * l'expiration et de la fraîcheur de l'authentification (max_age).
 *
 * @see <a href="http://openid.net/specs/openid-connect-core-1_0.html#IDTokenValidation">OpenID
 *     Connect Core 1.0, Section 3.1.3.7 (ID Token Validation)</a>
 */
public class IdTokenValidator {

  private final IDTokenValidator validator;
  private final String clientSecret;
  private final JWKSet jwkSet;
  private final JWK clientPrivateJWK;

  /**
   * Constructeur de base pour la validation asymétrique (RSA/EC).
   *
   * @param issuer L'identifiant de l'émetteur attendu.
   * @param clientID L'identifiant du client (audience attendue).
   * @param jwsAlgorithm L'algorithme de signature attendu.
   * @param jwkSet L'ensemble de clés publiques du fournisseur pour vérifier la signature.
   */
  public IdTokenValidator(
      String issuer, ClientID clientID, JWSAlgorithm jwsAlgorithm, JWKSet jwkSet) {
    this(issuer, clientID, jwsAlgorithm, jwkSet, null, null);
  }

  /**
   * Constructeur supportant la validation symétrique (HMAC) via le secret client.
   *
   * @param issuer L'identifiant de l'émetteur attendu.
   * @param clientID L'identifiant du client (audience attendue).
   * @param jwsAlgorithm L'algorithme de signature attendu.
   * @param jwkSet L'ensemble de clés publiques du fournisseur.
   * @param clientSecret Le secret client pour les algorithmes HS256/384/512.
   */
  public IdTokenValidator(
      String issuer,
      ClientID clientID,
      JWSAlgorithm jwsAlgorithm,
      JWKSet jwkSet,
      String clientSecret) {
    this(issuer, clientID, jwsAlgorithm, jwkSet, clientSecret, null);
  }

  /**
   * Constructeur complet supportant également le déchiffrement des jetons (JWE).
   *
   * @param issuer L'identifiant de l'émetteur attendu.
   * @param clientID L'identifiant du client.
   * @param jwsAlgorithm L'algorithme de signature attendu.
   * @param jwkSet L'ensemble de clés publiques du fournisseur.
   * @param clientSecret Le secret client.
   * @param clientPrivateJWK La clé privée du client pour le déchiffrement.
   */
  public IdTokenValidator(
      final String issuer,
      final ClientID clientID,
      final JWSAlgorithm jwsAlgorithm,
      final JWKSet jwkSet,
      final String clientSecret,
      final JWK clientPrivateJWK) {
    this.clientSecret = clientSecret;
    this.jwkSet = jwkSet;
    this.clientPrivateJWK = clientPrivateJWK;
    if (JWSAlgorithm.Family.HMAC_SHA.contains(jwsAlgorithm) && clientSecret != null) {
      this.validator =
          new IDTokenValidator(
              new com.nimbusds.oauth2.sdk.id.Issuer(issuer),
              clientID,
              jwsAlgorithm,
              new com.nimbusds.oauth2.sdk.auth.Secret(clientSecret));
    } else {
      this.validator =
          new IDTokenValidator(
              new com.nimbusds.oauth2.sdk.id.Issuer(issuer), clientID, jwsAlgorithm, jwkSet);
    }
  }

  /**
   * Valide un ID Token.
   *
   * @param idTokenString La chaîne brute du jeton (JWS ou JWE).
   * @param expectedNonce La valeur de nonce attendue (pour contrer le rejeu).
   * @param maxAuthAgeSeconds L'âge maximum autorisé de l'authentification (en secondes).
   * @return L'instance de {@link IdToken} validée.
   * @throws OAuthException si une règle de validation n'est pas respectée.
   */
  public IdToken validate(String idTokenString, Nonce expectedNonce, long maxAuthAgeSeconds)
      throws OAuthException {
    try {
      final String tokenToValidate = decryptIfEncrypted(idTokenString);

      if (tokenToValidate.split("\\.").length == 3) {
        final SignedJWT signedJWT = SignedJWT.parse(tokenToValidate);
        verifySignature(signedJWT);
        final IDTokenClaimsSet claimsSet = validator.validate(signedJWT, expectedNonce);
        if (claimsSet.getAudience().size() > 1 && claimsSet.getAuthorizedParty() == null) {
          throw new OAuthException("ID Token has multiple audiences but 'azp' claim is missing.");
        }
        if (claimsSet.getAuthorizedParty() != null
            && !claimsSet
                .getAuthorizedParty()
                .getValue()
                .equals(validator.getClientID().getValue())) {
          throw new OAuthException("ID Token 'azp' claim does not match the client ID.");
        }
        validateMaxAuthAge(claimsSet, maxAuthAgeSeconds);
      }

      return new IdToken(tokenToValidate);
    } catch (java.text.ParseException
        | com.nimbusds.jose.JOSEException
        | com.nimbusds.jose.proc.BadJOSEException e) {
      throw new OAuthException("Error parsing or validating ID Token", e);
    }
  }

  private void validateMaxAuthAge(IDTokenClaimsSet claimsSet, long maxAuthAgeSeconds)
      throws OAuthException {
    if (maxAuthAgeSeconds > 0) {
      final Date authTime = claimsSet.getAuthenticationTime();
      if (authTime == null) {
        throw new OAuthException(
            "ID Token does not contain 'auth_time' claim,"
                + " but maxAuthAgeSeconds is specified.");
      }
      final long nowSeconds = new Date().getTime() / 1000;
      final long authTimeSeconds = authTime.getTime() / 1000;

      if (nowSeconds - authTimeSeconds > maxAuthAgeSeconds) {
        throw new OAuthException(
            "ID Token has expired due to max authentication age. Issued at: "
                + authTimeSeconds
                + ", Max age: "
                + maxAuthAgeSeconds
                + "s.");
      }
    }
  }

  private String decryptIfEncrypted(String token)
      throws com.nimbusds.jose.JOSEException, java.text.ParseException, OAuthException {
    if (token.split("\\.").length == 5) {
      if (clientPrivateJWK == null) {
        throw new OAuthException(
            "Token is encrypted but no client private key provided for decryption.");
      }
      final EncryptedJWT encryptedJWT = EncryptedJWT.parse(token);
      final JWEDecrypter decrypter;
      if (clientPrivateJWK instanceof RSAKey) {
        decrypter = new com.nimbusds.jose.crypto.RSADecrypter((RSAKey) clientPrivateJWK);
      } else if (clientPrivateJWK instanceof com.nimbusds.jose.jwk.ECKey) {
        decrypter =
            new com.nimbusds.jose.crypto.ECDHDecrypter(
                (com.nimbusds.jose.jwk.ECKey) clientPrivateJWK);
      } else {
        throw new OAuthException(
            "Unsupported JWK type for decryption: " + clientPrivateJWK.getClass().getName());
      }
      encryptedJWT.decrypt(decrypter);
      final com.nimbusds.jose.Payload payload = encryptedJWT.getPayload();
      final SignedJWT nestedSignedJWT = payload.toSignedJWT();
      if (nestedSignedJWT != null) {
        return nestedSignedJWT.serialize();
      }
      return payload.toString();
    }
    return token;
  }

  private void verifySignature(SignedJWT signedJWT) throws OAuthException {
    try {
      final JWSHeader header = signedJWT.getHeader();
      final JWSAlgorithm alg = header.getAlgorithm();

      final JWSVerifier verifier;
      if (JWSAlgorithm.Family.RSA.contains(alg)) {
        final RSAKey rsaJWK =
            jwkSet.getKeys().stream()
                .filter(k -> header.getKeyID().equals(k.getKeyID()))
                .filter(k -> k instanceof RSAKey)
                .map(k -> (RSAKey) k)
                .findFirst()
                .orElseThrow(
                    () -> new OAuthException("RSA JWK not found for key ID: " + header.getKeyID()));
        verifier = new RSASSAVerifier(rsaJWK);
      } else if (JWSAlgorithm.Family.EC.contains(alg)) {
        final com.nimbusds.jose.jwk.ECKey ecJWK =
            jwkSet.getKeys().stream()
                .filter(k -> header.getKeyID().equals(k.getKeyID()))
                .filter(k -> k instanceof com.nimbusds.jose.jwk.ECKey)
                .map(k -> (com.nimbusds.jose.jwk.ECKey) k)
                .findFirst()
                .orElseThrow(
                    () -> new OAuthException("EC JWK not found for key ID: " + header.getKeyID()));
        verifier = new com.nimbusds.jose.crypto.ECDSAVerifier(ecJWK);
      } else if (JWSAlgorithm.Family.HMAC_SHA.contains(alg)) {
        if (clientSecret == null || clientSecret.isEmpty()) {
          throw new OAuthException("Client secret is required for HMAC signature verification.");
        }
        verifier = new MACVerifier(clientSecret.getBytes());
      } else {
        throw new OAuthException("Unsupported JWS Algorithm: " + alg);
      }

      if (!signedJWT.verify(verifier)) {
        throw new OAuthException("ID Token signature verification failed.");
      }
    } catch (com.nimbusds.jose.JOSEException e) {
      throw new OAuthException("Error during ID Token signature verification", e);
    }
  }

  /**
   * Valide un Logout Token reçu via le canal de retour (Back-Channel).
   *
   * @param logoutTokenString La chaîne brute du jeton de déconnexion.
   * @throws OAuthException si le jeton est invalide.
   * @see <a
   *     href="https://openid.net/specs/openid-connect-backchannel-1_0.html#LogoutTokenValidation">OpenID
   *     Connect Back-Channel Logout 1.0, Section 2.6</a>
   */
  public void validateLogoutToken(String logoutTokenString) throws OAuthException {
    try {
      final String tokenToValidate = decryptIfEncrypted(logoutTokenString);
      final SignedJWT signedJWT = SignedJWT.parse(tokenToValidate);
      verifySignature(signedJWT);

      final IDTokenClaimsSet claimsSet = new IDTokenClaimsSet(signedJWT.getJWTClaimsSet());

      if (claimsSet.getNonce() != null) {
        throw new OAuthException("Logout Token MUST NOT contain a nonce.");
      }

      final Object events = claimsSet.getClaim("events");
      if (!(events instanceof java.util.Map)
          || !((java.util.Map<?, ?>) events)
              .containsKey("http://schemas.openid.net/event/backchannel-logout")) {
        throw new OAuthException("Logout Token MUST contain the backchannel-logout event claim.");
      }

      validator.validate(signedJWT, null);

    } catch (java.text.ParseException
        | com.nimbusds.jose.JOSEException
        | com.nimbusds.jose.proc.BadJOSEException
        | com.nimbusds.oauth2.sdk.ParseException e) {
      throw new OAuthException("Error validating Logout Token", e);
    }
  }

  /**
   * Valide la liaison du jeton (Token Binding) à une clé publique ou un certificat spécifique.
   *
   * <p>Supporte la vérification de l'empreinte de clé JWK (DPoP) et de l'empreinte de certificat
   * X.509 (mTLS).
   *
   * @param idToken Le jeton d'identité validé.
   * @param expectedJkt L'empreinte JWK Thumbprint attendue (RFC 9449).
   * @param expectedX5t L'empreinte de certificat X.509 attendue (RFC 8705).
   * @throws OAuthException en cas de non-concordance des empreintes.
   * @see <a href="https://tools.ietf.org/html/rfc9449">RFC 9449 (DPoP)</a>
   * @see <a href="https://tools.ietf.org/html/rfc8705">RFC 8705 (mTLS)</a>
   */
  public void validateTokenBinding(IdToken idToken, String expectedJkt, String expectedX5t)
      throws OAuthException {
    final Object cnf = idToken.getClaim("cnf");
    if (!(cnf instanceof java.util.Map)) {
      return;
    }
    final java.util.Map<?, ?> cnfMap = (java.util.Map<?, ?>) cnf;

    if (expectedJkt != null) {
      final Object jkt = cnfMap.get("jkt");
      if (!expectedJkt.equals(jkt)) {
        throw new OAuthException(
            "DPoP proof key mismatch. Expected: " + expectedJkt + ", Got: " + jkt);
      }
    }

    if (expectedX5t != null) {
      final Object x5t = cnfMap.get("x5t#S256");
      if (!expectedX5t.equals(x5t)) {
        throw new OAuthException(
            "mTLS certificate mismatch. Expected: " + expectedX5t + ", Got: " + x5t);
      }
    }
  }
}
