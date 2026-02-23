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
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.UUID;

/**
 * Implémentation par défaut de {@link DPoPProofCreator} utilisant la bibliothèque Nimbus JOSE+JWT.
 *
 * <p>Implémente la spécification <b>RFC 9449:</b> OAuth 2.0 Demonstrating Proof of Possession
 * (DPoP).
 *
 * <p>Ce créateur génère un jeton JWT de preuve DPoP incluant les revendications {@code htm}
 * (méthode HTTP), {@code htu} (URI cible HTTP) et optionnellement {@code ath} (empreinte du jeton
 * d'accès) pour lier la requête à un jeton spécifique.
 *
 * @see <a href="https://tools.ietf.org/html/rfc9449">RFC 9449 (DPoP)</a>
 */
public class DefaultDPoPProofCreator implements DPoPProofCreator {

    private final JWK dpopJWK;
    private final JWSAlgorithm jwsAlgorithm;

    /**
     * Constructeur par défaut générant une paire de clés RSA 2048 bits éphémère.
     *
     * @throws OAuthException si la génération de la clé échoue.
     */
    public DefaultDPoPProofCreator() {
        try {
            final KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            final KeyPair keyPair = keyGen.generateKeyPair();
            dpopJWK =
                    new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                            .privateKey((RSAPrivateKey) keyPair.getPrivate())
                            .keyUse(KeyUse.SIGNATURE)
                            .keyID(UUID.randomUUID().toString())
                            .build();
            jwsAlgorithm = JWSAlgorithm.RS256;
        } catch (final NoSuchAlgorithmException e) {
            throw new OAuthException("Failed to generate RSA key pair for DPoP", e);
        }
    }

    /**
     * Constructeur utilisant une clé JWK existante.
     *
     * @param dpopJWK      La clé privée à utiliser pour signer les preuves DPoP.
     * @param jwsAlgorithm L'algorithme de signature à utiliser.
     * @throws IllegalArgumentException si la clé fournie n'est pas une clé privée.
     */
    public DefaultDPoPProofCreator(final JWK dpopJWK, final JWSAlgorithm jwsAlgorithm) {
        if (!dpopJWK.isPrivate()) {
            throw new IllegalArgumentException("DPoP JWK must contain a private key for signing.");
        }
        this.dpopJWK = dpopJWK;
        this.jwsAlgorithm = jwsAlgorithm;
    }

    /**
     * Crée une preuve DPoP pour la requête donnée.
     *
     * @param request     La requête HTTP à protéger.
     * @param accessToken Le jeton d'accès associé (optionnel, utilisé pour la revendication {@code
     *                    ath}).
     * @return Le jeton JWT de preuve DPoP sérialisé.
     */
    @Override
    public String createDPoPProof(final OAuthRequest request, final String accessToken) {
        final JWSHeader header =
                new JWSHeader.Builder(jwsAlgorithm)
                        .jwk(dpopJWK.toPublicJWK())
                        .type(new com.nimbusds.jose.JOSEObjectType("dpop+jwt"))
                        .build();

        final JWTClaimsSet.Builder claimsBuilder =
                new JWTClaimsSet.Builder()
                        .jwtID(UUID.randomUUID().toString())
                        .issueTime(new Date())
                        .claim("htm", request.getVerb().name())
                        .claim("htu", request.getCompleteUrl());

        if (accessToken != null && !accessToken.isEmpty()) {
            try {
                final String ath =
                        com.nimbusds.jose.util.Base64URL.encode(
                                        java.security.MessageDigest.getInstance("SHA-256")
                                                .digest(accessToken.getBytes(StandardCharsets.UTF_8)))
                                .toString();
                claimsBuilder.claim("ath", ath);
            } catch (final NoSuchAlgorithmException e) {
                throw new OAuthException("SHA-256 algorithm not found for 'ath' claim in DPoP", e);
            }
        }

        final JWTClaimsSet claims = claimsBuilder.build();
        final SignedJWT signedJWT = new SignedJWT(header, claims);

        try {
            if (dpopJWK instanceof RSAKey) {
                signedJWT.sign(new RSASSASigner((RSAKey) dpopJWK));
            } else if (dpopJWK instanceof ECKey) {
                signedJWT.sign(new ECDSASigner((ECKey) dpopJWK));
            } else {
                throw new OAuthException(
                        "Unsupported JWK type for DPoP signing: " + dpopJWK.getClass().getName());
            }
        } catch (final JOSEException e) {
            throw new OAuthException("Error signing DPoP proof JWT", e);
        }

        return signedJWT.serialize();
    }
}
