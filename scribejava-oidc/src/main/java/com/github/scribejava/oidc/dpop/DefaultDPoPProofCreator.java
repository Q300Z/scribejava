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
 * Default implementation of {@link DPoPProofCreator} using Nimbus JOSE+JWT library.
 * <p>
 * Implements <b>RFC 9449:</b> OAuth 2.0 Demonstrating Proof of Possession (DPoP).
 * <p>
 * This creator generates a DPoP proof JWT that includes claims like {@code htm} (HTTP method),
 * {@code htu} (HTTP target URI), and optionally {@code ath} (Access Token Hash) to bind
 * the request to a specific token.
 */
public class DefaultDPoPProofCreator implements DPoPProofCreator {

    private final JWK dpopJWK;
    private final JWSAlgorithm jwsAlgorithm;

    public DefaultDPoPProofCreator() {
        try {
            final KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            final KeyPair keyPair = keyGen.generateKeyPair();
            dpopJWK = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                    .privateKey((RSAPrivateKey) keyPair.getPrivate())
                    .keyUse(KeyUse.SIGNATURE)
                    .keyID(UUID.randomUUID().toString())
                    .build();
            jwsAlgorithm = JWSAlgorithm.RS256;
        } catch (final NoSuchAlgorithmException e) {
            throw new OAuthException("Failed to generate RSA key pair for DPoP", e);
        }
    }

    public DefaultDPoPProofCreator(final JWK dpopJWK, final JWSAlgorithm jwsAlgorithm) {
        if (!dpopJWK.isPrivate()) {
            throw new IllegalArgumentException("DPoP JWK must contain a private key for signing.");
        }
        this.dpopJWK = dpopJWK;
        this.jwsAlgorithm = jwsAlgorithm;
    }

    @Override
    public String createDPoPProof(final OAuthRequest request, final String accessToken) {
        final JWSHeader header = new JWSHeader.Builder(jwsAlgorithm)
                .jwk(dpopJWK.toPublicJWK())
                .type(new com.nimbusds.jose.JOSEObjectType("dpop+jwt"))
                .build();

        final JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                .jwtID(UUID.randomUUID().toString())
                .issueTime(new Date())
                .claim("htm", request.getVerb().name())
                .claim("htu", request.getCompleteUrl());

        if (accessToken != null && !accessToken.isEmpty()) {
            try {
                final String ath = com.nimbusds.jose.util.Base64URL.encode(java.security.MessageDigest
                        .getInstance("SHA-256").digest(accessToken.getBytes(StandardCharsets.UTF_8))).toString();
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
                throw new OAuthException("Unsupported JWK type for DPoP signing: " + dpopJWK.getClass().getName());
            }
        } catch (final JOSEException e) {
            throw new OAuthException("Error signing DPoP proof JWT", e);
        }

        return signedJWT.serialize();
    }
}
