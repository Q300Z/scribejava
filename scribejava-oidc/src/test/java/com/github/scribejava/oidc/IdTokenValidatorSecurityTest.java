package com.github.scribejava.oidc;

import com.github.scribejava.core.exceptions.OAuthException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.id.ClientID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class IdTokenValidatorSecurityTest {

    private RSAKey rsaJWK;
    private JWKSet jwkSet;
    private IdTokenValidator validator;
    private final String clientId = "client-123";
    private final String issuer = "https://idp.example.com";

    @BeforeEach
    public void setUp() throws Exception {
        rsaJWK = new RSAKeyGenerator(2048).keyID("kid-1").generate();
        jwkSet = new JWKSet(rsaJWK.toPublicJWK());
        validator = new IdTokenValidator(issuer, new ClientID(clientId), JWSAlgorithm.RS256, jwkSet);
    }

    private String createToken(final String iss, final String aud, final Date exp) throws Exception {
        final JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject("user1")
                .issuer(iss)
                .audience(aud)
                .expirationTime(exp)
                .issueTime(new Date())
                .build();
        final SignedJWT signedJWT = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID("kid-1").build(),
                claimsSet);
        signedJWT.sign(new RSASSASigner(rsaJWK));
        return signedJWT.serialize();
    }

    @Test
    public void shouldRejectExpiredToken() throws Exception {
        final String token = createToken(issuer, clientId, new Date(System.currentTimeMillis() - 100000));
        assertThrows(OAuthException.class, () -> validator.validate(token, null, 0));
    }

    @Test
    public void shouldRejectWrongAudience() throws Exception {
        final String token = createToken(issuer, "wrong-client", new Date(System.currentTimeMillis() + 10000));
        assertThrows(OAuthException.class, () -> validator.validate(token, null, 0));
    }

    @Test
    public void shouldRejectInvalidSignature() throws Exception {
        final String token = createToken(issuer, clientId, new Date(System.currentTimeMillis() + 10000));
        final String tamperedToken = token.substring(0, token.length() - 5) + (token.endsWith("a") ? "b" : "a");
        assertThrows(OAuthException.class, () -> validator.validate(tamperedToken, null, 0));
    }

    @Test
    public void shouldRejectMultipleAudiencesWithoutAzp() throws Exception {
        final JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject("user1")
                .issuer(issuer)
                .audience(java.util.Arrays.asList(clientId, "other-client"))
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + 10000))
                .build();
        final SignedJWT signedJWT = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID("kid-1").build(),
                claimsSet);
        signedJWT.sign(new RSASSASigner(rsaJWK));

        final OAuthException ex = assertThrows(OAuthException.class,
                () -> validator.validate(signedJWT.serialize(), null, 0));
        assertThat(ex.getMessage()).contains("azp");
    }

    @Test
    public void shouldAcceptMultipleAudiencesWithAzp() throws Exception {
        final JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject("user1")
                .issuer(issuer)
                .audience(java.util.Arrays.asList(clientId, "other-client"))
                .claim("azp", clientId)
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + 10000))
                .build();
        final SignedJWT signedJWT = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID("kid-1").build(),
                claimsSet);
        signedJWT.sign(new RSASSASigner(rsaJWK));

        final IdToken result = validator.validate(signedJWT.serialize(), null, 0);
        assertThat(result).isNotNull();
    }

    @Test
    public void shouldRejectAzpMismatch() throws Exception {
        final JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject("user1")
                .issuer(issuer)
                .audience(clientId)
                .claim("azp", "wrong-client")
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + 10000))
                .build();
        final SignedJWT signedJWT = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID("kid-1").build(),
                claimsSet);
        signedJWT.sign(new RSASSASigner(rsaJWK));

        assertThrows(OAuthException.class, () -> validator.validate(signedJWT.serialize(), null, 0));
    }

    @Test
    public void shouldRejectTokenIssuedInTheFuture() throws Exception {
        // Issue time 1 hour in the future
        final JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject("user1")
                .issuer(issuer)
                .audience(clientId)
                .issueTime(new Date(System.currentTimeMillis() + 3600000))
                .expirationTime(new Date(System.currentTimeMillis() + 7200000))
                .build();
        final SignedJWT signedJWT = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID("kid-1").build(),
                claimsSet);
        signedJWT.sign(new RSASSASigner(rsaJWK));

        assertThrows(OAuthException.class, () -> validator.validate(signedJWT.serialize(), null, 0));
    }
}
