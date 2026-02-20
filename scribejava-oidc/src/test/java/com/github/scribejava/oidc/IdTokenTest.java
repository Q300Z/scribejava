package com.github.scribejava.oidc;

import org.junit.jupiter.api.Test;

import java.util.Date;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import static org.assertj.core.api.Assertions.assertThat;

public class IdTokenTest {

    private static final int KEY_SIZE = 2048;
    private static final int EXPIRATION_MS = 60 * 1000;

    @Test
    public void shouldParseIdToken() throws Exception {
        // Generate RSA key for signing
        final RSAKey rsaJWK = new RSAKeyGenerator(KEY_SIZE).keyID("123").generate();
        final JWSSigner signer = new RSASSASigner(rsaJWK);

        // Prepare JWT with claims
        final JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject("alice")
                .issuer("https://c2id.com")
                .audience("123")
                .expirationTime(new Date(new Date().getTime() + EXPIRATION_MS))
                .issueTime(new Date())
                .claim("name", "Alice Doe")
                .claim("email", "alice@doe.com")
                .claim("email_verified", true)
                .claim("nonce", "xyz")
                .build();

        final SignedJWT signedJWT = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID("123").build(),
                claimsSet);
        signedJWT.sign(signer);

        final String rawToken = signedJWT.serialize();

        // Test IdToken
        final IdToken idToken = new IdToken(rawToken);

        assertThat(idToken.getIssuer()).isEqualTo("https://c2id.com");
        assertThat(idToken.getSubject()).isEqualTo("alice");
        assertThat(idToken.getNonce()).isEqualTo("xyz");
        assertThat(idToken.getStandardClaims().getName()).contains("Alice Doe");
        assertThat(idToken.getStandardClaims().getEmail()).contains("alice@doe.com");
        assertThat(idToken.getStandardClaims().isEmailVerified()).contains(true);
    }
}
