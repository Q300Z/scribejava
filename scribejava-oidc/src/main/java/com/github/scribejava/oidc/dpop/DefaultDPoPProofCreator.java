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
package com.github.scribejava.oidc.dpop;

import com.github.scribejava.core.dpop.DPoPProofCreator;
import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.oidc.model.JwtBuilder;
import com.github.scribejava.oidc.model.JwtSigner;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Implémentation par défaut de {@link DPoPProofCreator} native ScribeJava. */
public class DefaultDPoPProofCreator implements DPoPProofCreator {

  private final PrivateKey privateKey;
  private final PublicKey publicKey;
  private final JwtSigner signer;

  /** Constructeur par défaut générant une paire de clés RSA 2048 bits éphémère. */
  public DefaultDPoPProofCreator() {
    try {
      final KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
      keyGen.initialize(2048);
      final KeyPair keyPair = keyGen.generateKeyPair();
      this.privateKey = keyPair.getPrivate();
      this.publicKey = keyPair.getPublic();
      this.signer = new JwtSigner.RsaSha256Signer();
    } catch (final NoSuchAlgorithmException e) {
      throw new OAuthException("Failed to generate RSA key pair for DPoP", e);
    }
  }

  /**
   * @param privateKey clé privée
   * @param publicKey clé publique (pour le claim jwk du header)
   * @param signer signataire
   */
  public DefaultDPoPProofCreator(PrivateKey privateKey, PublicKey publicKey, JwtSigner signer) {
    this.privateKey = privateKey;
    this.publicKey = publicKey;
    this.signer = signer;
  }

  @Override
  public String createDPoPProof(final OAuthRequest request, final String accessToken) {
    final long now = System.currentTimeMillis() / 1000;
    final JwtBuilder builder =
        new JwtBuilder()
            .header("typ", "dpop+jwt")
            .header("jwk", createPublicJwkMap())
            .claim("jti", UUID.randomUUID().toString())
            .claim("iat", now)
            .claim("htm", request.getVerb().name())
            .claim("htu", request.getCompleteUrl());

    if (accessToken != null && !accessToken.isEmpty()) {
      builder.claim("ath", computeThumbprint(accessToken));
    }

    return builder.buildAndSign(signer, privateKey);
  }

  private Map<String, Object> createPublicJwkMap() {
    if (publicKey instanceof RSAPublicKey) {
      final RSAPublicKey rsaPub = (RSAPublicKey) publicKey;
      final Map<String, Object> jwk = new HashMap<>();
      jwk.put("kty", "RSA");
      jwk.put("n", encode(rsaPub.getModulus().toByteArray()));
      jwk.put("e", encode(rsaPub.getPublicExponent().toByteArray()));
      jwk.put("use", "sig");
      return jwk;
    }
    throw new OAuthException("Unsupported public key type for DPoP JWK header");
  }

  private String computeThumbprint(String accessToken) {
    try {
      final byte[] hash =
          MessageDigest.getInstance("SHA-256").digest(accessToken.getBytes(StandardCharsets.UTF_8));
      return encode(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new OAuthException("SHA-256 not available", e);
    }
  }

  private String encode(byte[] bytes) {
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }
}
