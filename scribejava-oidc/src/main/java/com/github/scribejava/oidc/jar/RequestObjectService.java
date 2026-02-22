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
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Service to create Signed Request Objects.
 *
 * <p>Implements JWT-Secured Authorization Request (JAR) as defined in:
 *
 * <ul>
 *   <li><b>RFC 9101:</b> The OAuth 2.0 Authorization Framework: JWT-Secured Authorization Request
 *       (JAR)
 *   <li><b>OpenID Connect Core 1.0:</b> Section 6 (Passing Request Parameters as JWTs)
 * </ul>
 *
 * Signing the request object ensures integrity and authenticity of the authorization parameters.
 */
public class RequestObjectService {

  private final String clientId;
  private final String audience;
  private final Supplier<JWK> signingJWKSupplier;
  private final JWSAlgorithm jwsAlgorithm;
  private final JWK encryptionJWK;
  private final com.nimbusds.jose.JWEAlgorithm jweAlgorithm;
  private final com.nimbusds.jose.EncryptionMethod encryptionMethod;

  public RequestObjectService(
      String clientId, String audience, JWK signingJWK, JWSAlgorithm jwsAlgorithm) {
    this(clientId, audience, () -> signingJWK, jwsAlgorithm, null, null, null);
  }

  public RequestObjectService(
      String clientId,
      String audience,
      Supplier<JWK> signingJWKSupplier,
      JWSAlgorithm jwsAlgorithm,
      JWK encryptionJWK,
      com.nimbusds.jose.JWEAlgorithm jweAlgorithm,
      com.nimbusds.jose.EncryptionMethod encryptionMethod) {
    this.clientId = clientId;
    this.audience = audience;
    this.signingJWKSupplier = signingJWKSupplier;
    this.jwsAlgorithm = jwsAlgorithm;
    this.encryptionJWK = encryptionJWK;
    this.jweAlgorithm = jweAlgorithm;
    this.encryptionMethod = encryptionMethod;
  }

  public String createRequestObject(Map<String, String> authorizationParams) {
    final JWK signingJWK = signingJWKSupplier.get();
    if (signingJWK == null) {
      throw new OAuthException("Signing JWK supplier returned null");
    }
    if (!signingJWK.isPrivate()) {
      throw new IllegalArgumentException("JWK must contain a private key for signing.");
    }

    final JWSHeader header =
        new JWSHeader.Builder(jwsAlgorithm)
            .keyID(signingJWK.getKeyID())
            .type(com.nimbusds.jose.JOSEObjectType.JWT)
            .build();

    final JWTClaimsSet.Builder claimsBuilder =
        new JWTClaimsSet.Builder()
            .issuer(clientId)
            .audience(audience)
            .issueTime(new Date())
            .jwtID(UUID.randomUUID().toString());

    // Add all authorization parameters as claims
    authorizationParams.forEach(claimsBuilder::claim);

    // Ensure client_id is present in claims as per RFC 9101
    if (!authorizationParams.containsKey("client_id")) {
      claimsBuilder.claim("client_id", clientId);
    }

    final SignedJWT signedJWT = new SignedJWT(header, claimsBuilder.build());

    try {
      final JWSSigner signer;
      if (signingJWK instanceof RSAKey) {
        signer = new RSASSASigner((RSAKey) signingJWK);
      } else if (signingJWK instanceof ECKey) {
        signer = new ECDSASigner((ECKey) signingJWK);
      } else {
        throw new OAuthException("Unsupported JWK type: " + signingJWK.getClass().getName());
      }
      signedJWT.sign(signer);
    } catch (JOSEException e) {
      throw new OAuthException("Error signing Request Object JWT", e);
    }

    if (encryptionJWK != null && jweAlgorithm != null && encryptionMethod != null) {
      return encrypt(signedJWT);
    }

    return signedJWT.serialize();
  }

  private String encrypt(SignedJWT signedJWT) {
    try {
      final com.nimbusds.jose.JWEHeader jweHeader =
          new com.nimbusds.jose.JWEHeader.Builder(jweAlgorithm, encryptionMethod)
              .contentType("JWT") // Nested JWT
              .keyID(encryptionJWK.getKeyID())
              .build();

      final com.nimbusds.jose.JWEObject jweObject =
          new com.nimbusds.jose.JWEObject(jweHeader, new com.nimbusds.jose.Payload(signedJWT));

      final com.nimbusds.jose.JWEEncrypter encrypter;
      if (encryptionJWK instanceof RSAKey) {
        encrypter = new com.nimbusds.jose.crypto.RSAEncrypter((RSAKey) encryptionJWK);
      } else if (encryptionJWK instanceof ECKey) {
        encrypter = new com.nimbusds.jose.crypto.ECDHEncrypter((ECKey) encryptionJWK);
      } else {
        throw new OAuthException(
            "Unsupported Encryption JWK type: " + encryptionJWK.getClass().getName());
      }

      jweObject.encrypt(encrypter);
      return jweObject.serialize();
    } catch (JOSEException e) {
      throw new OAuthException("Error encrypting Request Object JWT", e);
    }
  }
}
