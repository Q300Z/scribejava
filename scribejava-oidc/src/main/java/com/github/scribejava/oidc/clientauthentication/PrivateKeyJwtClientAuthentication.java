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
package com.github.scribejava.oidc.clientauthentication;

import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.oauth2.clientauthentication.ClientAuthentication;
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
import java.util.UUID;

/**
 * Authentification du client via un jeton porteur JSON Web Token (JWT).
 *
 * <p>Implémente la méthode d'authentification {@code private_key_jwt} permettant une sécurité
 * renforcée en prouvant la possession d'une clé privée au lieu d'utiliser un secret partagé.
 *
 * <ul>
 *   <li><b>RFC 7523:</b> JSON Web Token (JWT) Profile for OAuth 2.0 Client Authentication (Section
 *       2.2)
 *   <li><b>OpenID Connect Core 1.0:</b> Section 9 (Client Authentication)
 * </ul>
 */
public class PrivateKeyJwtClientAuthentication implements ClientAuthentication {

    private static final int EXPIRATION_TIMEOUT_MS = 5 * 60 * 1000;
    private final String clientId;
    private final String audience;
    private final JWK privateJWK;
    private final JWSAlgorithm jwsAlgorithm;

    /**
     * Constructeur.
     *
     * @param clientId     L'identifiant du client (Client ID).
     * @param audience     L'audience attendue par le serveur (typiquement l'URL du point de terminaison
     *                     de jeton).
     * @param privateJWK   La clé privée du client au format JWK.
     * @param jwsAlgorithm L'algorithme de signature à utiliser (ex: RS256, ES256).
     * @throws IllegalArgumentException si la clé fournie n'est pas une clé privée.
     */
    public PrivateKeyJwtClientAuthentication(
            final String clientId,
            final String audience,
            final JWK privateJWK,
            final JWSAlgorithm jwsAlgorithm) {
        this.clientId = clientId;
        this.audience = audience;
        this.privateJWK = privateJWK;
        this.jwsAlgorithm = jwsAlgorithm;

        if (!privateJWK.isPrivate()) {
            throw new IllegalArgumentException("JWK must contain a private key.");
        }
    }

    @Override
    public void addClientAuthentication(
            final OAuthRequest request, final String apiKey, final String apiSecret) {
        addClientAuthentication(request);
    }

    /**
     * Ajoute l'assertion JWT d'authentification client à la requête.
     *
     * @param request La requête HTTP à laquelle ajouter les paramètres {@code client_assertion} et
     *                {@code client_assertion_type}.
     */
    public void addClientAuthentication(final OAuthRequest request) {
        final String assertion = createAssertion();
        request.addBodyParameter(
                "client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer");
        request.addBodyParameter("client_assertion", assertion);
    }

    private String createAssertion() {
        final JWSHeader header =
                new JWSHeader.Builder(jwsAlgorithm).keyID(privateJWK.getKeyID()).build();

        final long now = System.currentTimeMillis();
        final JWTClaimsSet claimsSet =
                new JWTClaimsSet.Builder()
                        .issuer(clientId)
                        .subject(clientId)
                        .audience(audience)
                        .expirationTime(new Date(now + EXPIRATION_TIMEOUT_MS))
                        .issueTime(new Date(now))
                        .jwtID(UUID.randomUUID().toString())
                        .build();

        final SignedJWT signedJWT = new SignedJWT(header, claimsSet);

        try {
            final JWSSigner signer;
            if (privateJWK instanceof RSAKey) {
                signer = new RSASSASigner((RSAKey) privateJWK);
            } else if (privateJWK instanceof ECKey) {
                signer = new ECDSASigner((ECKey) privateJWK);
            } else {
                throw new OAuthException("Unsupported JWK type: " + privateJWK.getClass().getName());
            }
            signedJWT.sign(signer);
        } catch (final JOSEException e) {
            throw new OAuthException("Error signing client assertion JWT", e);
        }

        return signedJWT.serialize();
    }
}
