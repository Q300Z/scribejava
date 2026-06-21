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
package com.github.scribejava.oidc.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import org.junit.jupiter.api.Test;

/** Tests pour JwtSignatureVerifier. */
public class JwtSignatureVerifierTest {

  /**
   * Test de vérification RS256 standard.
   *
   * @throws NoSuchAlgorithmException si l'algorithme n'existe pas.
   */
  @Test
  public void shouldVerifyRS256() throws NoSuchAlgorithmException {
    final KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
    keyGen.initialize(2048);
    final KeyPair keyPair = keyGen.generateKeyPair();

    final JwtSigner signer = new JwtSigner.RsaSha256Signer();
    final String content = "header.payload";
    final String signatureBase64 = signer.sign(content, keyPair.getPrivate());
    final byte[] signature = Base64.getUrlDecoder().decode(signatureBase64);

    final JwtSignatureVerifier verifier = new JwtSignatureVerifier();
    assertThat(
            verifier.verifyRS256(
                content.getBytes(StandardCharsets.UTF_8), signature, keyPair.getPublic()))
        .isTrue();
  }

  /**
   * Test de vérification des algorithmes RS/PS.
   *
   * @throws Exception si erreur.
   */
  @Test
  public void shouldVerifyDynamicRSAlgorithms() throws Exception {
    final com.nimbusds.jose.jwk.RSAKey rsaKey = new RSAKeyGenerator(2048).generate();
    final RSAPublicKey publicKey = rsaKey.toRSAPublicKey();
    final JWSSigner signer = new RSASSASigner(rsaKey);

    final JwtSignatureVerifier verifier = new JwtSignatureVerifier();

    for (final JWSAlgorithm alg :
        new JWSAlgorithm[] {
          JWSAlgorithm.RS256, JWSAlgorithm.RS384, JWSAlgorithm.RS512,
          JWSAlgorithm.PS256, JWSAlgorithm.PS384, JWSAlgorithm.PS512
        }) {
      final SignedJWT signedJWT =
          new SignedJWT(
              new JWSHeader.Builder(alg).build(),
              new JWTClaimsSet.Builder().subject("test").build());
      signedJWT.sign(signer);

      final byte[] signedContent = signedJWT.getSigningInput();
      final byte[] signature = signedJWT.getSignature().decode();

      assertThat(verifier.verify(alg.getName(), signedContent, signature, publicKey))
          .as("Verification failed for algorithm " + alg.getName())
          .isTrue();
    }
  }

  /**
   * Test de vérification ES256.
   *
   * @throws Exception si erreur.
   */
  @Test
  public void shouldVerifyES256() throws Exception {
    final JwtSignatureVerifier verifier = new JwtSignatureVerifier();
    final com.nimbusds.jose.jwk.ECKey ecKey = new ECKeyGenerator(Curve.P_256).generate();
    final ECPublicKey publicKey = ecKey.toECPublicKey();
    final JWSSigner signer = new ECDSASigner(ecKey);
    final SignedJWT signedJWT =
        new SignedJWT(
            new JWSHeader.Builder(JWSAlgorithm.ES256).build(),
            new JWTClaimsSet.Builder().subject("test").build());
    signedJWT.sign(signer);
    assertThat(
            verifier.verify(
                "ES256", signedJWT.getSigningInput(), signedJWT.getSignature().decode(), publicKey))
        .isTrue();
  }

  /**
   * Test de vérification ES384.
   *
   * @throws Exception si erreur.
   */
  @Test
  public void shouldVerifyES384() throws Exception {
    final JwtSignatureVerifier verifier = new JwtSignatureVerifier();
    final com.nimbusds.jose.jwk.ECKey ecKey = new ECKeyGenerator(Curve.P_384).generate();
    final ECPublicKey publicKey = ecKey.toECPublicKey();
    final JWSSigner signer = new ECDSASigner(ecKey);
    final SignedJWT signedJWT =
        new SignedJWT(
            new JWSHeader.Builder(JWSAlgorithm.ES384).build(),
            new JWTClaimsSet.Builder().subject("test").build());
    signedJWT.sign(signer);
    assertThat(
            verifier.verify(
                "ES384", signedJWT.getSigningInput(), signedJWT.getSignature().decode(), publicKey))
        .isTrue();
  }

  /**
   * Test de vérification ES512.
   *
   * @throws Exception si erreur.
   */
  @Test
  public void shouldVerifyES512() throws Exception {
    final JwtSignatureVerifier verifier = new JwtSignatureVerifier();
    final com.nimbusds.jose.jwk.ECKey ecKey = new ECKeyGenerator(Curve.P_521).generate();
    final ECPublicKey publicKey = ecKey.toECPublicKey();
    final JWSSigner signer = new ECDSASigner(ecKey);
    final SignedJWT signedJWT =
        new SignedJWT(
            new JWSHeader.Builder(JWSAlgorithm.ES512).build(),
            new JWTClaimsSet.Builder().subject("test").build());
    signedJWT.sign(signer);
    assertThat(
            verifier.verify(
                "ES512", signedJWT.getSigningInput(), signedJWT.getSignature().decode(), publicKey))
        .isTrue();
  }
}
