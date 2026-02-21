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
 * Client Authentication using JSON Web Token (JWT) Bearer Token Profiles.
 * <p>
 * Implements {@code private_key_jwt} authentication method as defined in:
 * <ul>
 *   <li><b>RFC 7523:</b> JSON Web Token (JWT) Profile for OAuth 2.0 Client Authentication (Section 2.2)</li>
 *   <li><b>OpenID Connect Core 1.0:</b> Section 9 (Client Authentication)</li>
 * </ul>
 * This method is more secure than {@code client_secret} as it proves possession of a private key.
 */
public class PrivateKeyJwtClientAuthentication implements ClientAuthentication {

    private static final int EXPIRATION_TIMEOUT_MS = 5 * 60 * 1000;
    private final String clientId;
    private final String audience;
    private final JWK privateJWK;
    private final JWSAlgorithm jwsAlgorithm;

    public PrivateKeyJwtClientAuthentication(final String clientId, final String audience, final JWK privateJWK,
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
    public void addClientAuthentication(final OAuthRequest request, final String apiKey, final String apiSecret) {
        addClientAuthentication(request);
    }

    public void addClientAuthentication(final OAuthRequest request) {
        final String assertion = createAssertion();
        request.addBodyParameter("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer");
        request.addBodyParameter("client_assertion", assertion);
    }

    private String createAssertion() {
        final JWSHeader header = new JWSHeader.Builder(jwsAlgorithm)
                .keyID(privateJWK.getKeyID())
                .build();

        final long now = System.currentTimeMillis();
        final JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
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
