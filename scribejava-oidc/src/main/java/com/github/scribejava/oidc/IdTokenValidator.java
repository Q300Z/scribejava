package com.github.scribejava.oidc;

import com.github.scribejava.core.exceptions.OAuthException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet;
import com.nimbusds.openid.connect.sdk.validators.IDTokenValidator;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.Optional;

/**
 * Validator for OpenID Connect ID Tokens.
 */
public class IdTokenValidator {

    private final IDTokenValidator validator;
    private final String clientSecret;

    public IdTokenValidator(String issuer, ClientID clientID, JWSAlgorithm jwsAlgorithm, JWKSet jwkSet) {
        this(issuer, clientID, jwsAlgorithm, jwkSet, null);
    }

    public IdTokenValidator(String issuer, ClientID clientID, JWSAlgorithm jwsAlgorithm, JWKSet jwkSet, String clientSecret) {
        this.clientSecret = clientSecret;
        this.validator = new IDTokenValidator(
                new com.nimbusds.oauth2.sdk.id.Issuer(issuer),
                clientID,
                jwsAlgorithm,
                jwkSet
        );
    }

    /**
     * Validates a signed ID Token string.
     */
    public IDTokenClaimsSet validate(String idTokenString, Nonce expectedNonce, long maxAuthAgeSeconds) throws OAuthException {
        try {
            SignedJWT signedJWT = SignedJWT.parse(idTokenString);

            // Verify signature
            verifySignature(signedJWT);

            // Validate claims using Nimbus's built-in validator
            IDTokenClaimsSet claimsSet = validator.validate(signedJWT, expectedNonce);

            // Custom validation for max authentication age if provided
            if (maxAuthAgeSeconds > 0) {
                Long authTime = claimsSet.getAuthTime();
                if (authTime == null) {
                    throw new OAuthException("ID Token does not contain 'auth_time' claim, but maxAuthAgeSeconds is specified.");
                }
                long nowSeconds = new Date().getTime() / 1000;

                if (nowSeconds - authTime > maxAuthAgeSeconds) {
                    throw new OAuthException("ID Token has expired due to max authentication age. Issued at: " + authTime + ", Max age: " + maxAuthAgeSeconds + "s.");
                }
            }

            return claimsSet;
        } catch (java.text.ParseException | com.nimbusds.oauth2.sdk.ParseException e) {
            throw new OAuthException("Error parsing ID Token", e);
        }
    }

    private void verifySignature(SignedJWT signedJWT) throws OAuthException {
        try {
            JWSHeader header = signedJWT.getHeader();
            JWSAlgorithm alg = header.getAlgorithm();

            JWSVerifier verifier;
            if (JWSAlgorithm.Family.RSA.contains(alg)) {
                RSAKey rsaJWK = Optional.ofNullable(validator.getJWKSet().getKeyByKeyID(header.getKeyID()))
                        .filter(k -> k instanceof RSAKey)
                        .map(k -> (RSAKey) k)
                        .orElseThrow(() -> new OAuthException("RSA JWK not found for key ID: " + header.getKeyID()));
                verifier = new RSASSAVerifier(rsaJWK);
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
        } catch (com.nimbusds.jose.JOSEException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new OAuthException("Error during ID Token signature verification", e);
        }
    }
}
