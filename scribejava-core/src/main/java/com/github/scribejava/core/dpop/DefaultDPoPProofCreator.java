package com.github.scribejava.core.dpop;

import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.exceptions.OAuthException;

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

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.util.Date;
import java.util.UUID;
import java.nio.charset.StandardCharsets;

/**
 * Default implementation of {@link DPoPProofCreator} using Nimbus JOSE+JWT library.
 */
public class DefaultDPoPProofCreator implements DPoPProofCreator {

    private final JWK dpopJWK;
    private final JWSAlgorithm jwsAlgorithm;

    public DefaultDPoPProofCreator() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair keyPair = keyGen.generateKeyPair();
            dpopJWK = new RSAKey.Builder((RSAPrivateKey) keyPair.getPrivate())
                    .publicOnly(false)
                    .keyUse(KeyUse.SIGNATURE)
                    .keyID(UUID.randomUUID().toString())
                    .build();
            jwsAlgorithm = JWSAlgorithm.RS256;
        } catch (NoSuchAlgorithmException | JOSEException e) {
            throw new OAuthException("Failed to generate RSA key pair for DPoP", e);
        }
    }

    public DefaultDPoPProofCreator(JWK dpopJWK, JWSAlgorithm jwsAlgorithm) {
        if (!dpopJWK.isPrivate()) {
            throw new IllegalArgumentException("DPoP JWK must contain a private key for signing.");
        }
        this.dpopJWK = dpopJWK;
        this.jwsAlgorithm = jwsAlgorithm;
    }

    @Override
    public String createDPoPProof(OAuthRequest request, String accessToken) {
        JWSHeader header = new JWSHeader.Builder(jwsAlgorithm)
                .jwk(dpopJWK.toPublicJWK())
                .type(new com.nimbusds.jose.JOSEObjectType("dpop+jwt"))
                .build();

        JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                .jwtID(UUID.randomUUID().toString())
                .issueTime(new Date())
                .claim("htm", request.getVerb().name())
                .claim("htu", request.getCompleteUrl());

        if (accessToken != null && !accessToken.isEmpty()) {
            try {
                claimsBuilder.claim("ath", com.nimbusds.jose.util.Base64URL.encode(java.security.MessageDigest.getInstance("SHA-256").digest(accessToken.getBytes(StandardCharsets.UTF_8))).toString());
            } catch (NoSuchAlgorithmException e) {
                throw new OAuthException("SHA-256 algorithm not found for 'ath' claim in DPoP", e);
            }
        }

        JWTClaimsSet claims = claimsBuilder.build();
        SignedJWT signedJWT = new SignedJWT(header, claims);

        try {
            if (dpopJWK instanceof RSAKey) {
                signedJWT.sign(new RSASSASigner((RSAKey) dpopJWK));
            } else if (dpopJWK instanceof ECKey) {
                signedJWT.sign(new ECDSASigner((ECKey) dpopJWK));
            } else {
                throw new OAuthException("Unsupported JWK type for DPoP signing: " + dpopJWK.getClass().getName());
            }
        } catch (JOSEException e) {
            throw new OAuthException("Error signing DPoP proof JWT", e);
        }

        return signedJWT.serialize();
    }
}
