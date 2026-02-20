package com.github.scribejava.oidc;

import com.github.scribejava.core.exceptions.OAuthException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.openid.connect.sdk.Nonce;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OidcSecurityDeepDiveTest {

    private static final String ISSUER = "https://issuer.example.com";
    private static final ClientID CLIENT_ID = new ClientID("client-id");
    private RSAKey rsaKey;
    private JWKSet jwkSet;

    @BeforeEach
    public void setUp() throws Exception {
        rsaKey = new RSAKeyGenerator(2048).keyID("rsa-1").generate();
        jwkSet = new JWKSet(rsaKey);
    }

    @Test
    public void shouldRejectExpiredToken() throws Exception {
        final JWTClaimsSet claimsSet = createBaseClaims()
                .issueTime(new Date())
                .expirationTime(new Date(new Date().getTime() - 3600000)) // 1 hour in the past
                .build();
        final String token = sign(claimsSet);

        final IdTokenValidator validator = new IdTokenValidator(ISSUER, CLIENT_ID, JWSAlgorithm.RS256, jwkSet);
        assertThrows(OAuthException.class, () -> validator.validate(token, new Nonce("nonce123"), 0));
    }

    @Test
    public void shouldRejectTokenFromFuture() throws Exception {
        final JWTClaimsSet claimsSet = createBaseClaims()
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + 3600000))
                .issueTime(new Date(new Date().getTime() + 100000)) // 100s in the future
                .build();
        final String token = sign(claimsSet);

        final IdTokenValidator validator = new IdTokenValidator(ISSUER, CLIENT_ID, JWSAlgorithm.RS256, jwkSet);
        assertThrows(OAuthException.class, () -> validator.validate(token, new Nonce("nonce123"), 0));
    }

    @Test
    public void shouldHandleMultipleAudiencesWithAzp() throws Exception {
        final JWTClaimsSet claimsSet = createBaseClaims()
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + 3600000))
                .audience(Arrays.asList(CLIENT_ID.getValue(), "other-client"))
                .claim("azp", CLIENT_ID.getValue())
                .build();
        final String token = sign(claimsSet);

        final IdTokenValidator validator = new IdTokenValidator(ISSUER, CLIENT_ID, JWSAlgorithm.RS256, jwkSet);
        final IdToken validatedToken = validator.validate(token, new Nonce("nonce123"), 0);
        assertThat(validatedToken).isNotNull();
    }

    @Test
    public void shouldRejectMultipleAudiencesWithoutAzp() throws Exception {
        // OIDC Core says azp is REQUIRED if aud has multiple values
        final JWTClaimsSet claimsSet = createBaseClaims()
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + 3600000))
                .audience(Arrays.asList(CLIENT_ID.getValue(), "other-client"))
                // azp missing
                .build();
        final String token = sign(claimsSet);

        final IdTokenValidator validator = new IdTokenValidator(ISSUER, CLIENT_ID, JWSAlgorithm.RS256, jwkSet);
        assertThrows(OAuthException.class, () -> validator.validate(token, new Nonce("nonce123"), 0));
    }

    @Test
    public void shouldValidateLogoutToken() throws Exception {
        final Map<String, Object> events = new HashMap<>();
        events.put("http://schemas.openid.net/event/backchannel-logout", Collections.emptyMap());

        final JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer(ISSUER)
                .audience(CLIENT_ID.getValue())
                .subject("user123")
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + 3600000))
                .jwtID("logout-123")
                .claim("events", events)
                .claim("sid", "session-123")
                .build();
        final String token = sign(claimsSet);

        final IdTokenValidator validator = new IdTokenValidator(ISSUER, CLIENT_ID, JWSAlgorithm.RS256, jwkSet);
        validator.validateLogoutToken(token);
    }

    @Test
    public void shouldRejectLogoutTokenWithNonce() throws Exception {
        final Map<String, Object> events = new HashMap<>();
        events.put("http://schemas.openid.net/event/backchannel-logout", Collections.emptyMap());

        final JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer(ISSUER)
                .audience(CLIENT_ID.getValue())
                .subject("user123")
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + 3600000))
                .claim("events", events)
                .claim("nonce", "nonce-should-not-be-here")
                .build();
        final String token = sign(claimsSet);

        final IdTokenValidator validator = new IdTokenValidator(ISSUER, CLIENT_ID, JWSAlgorithm.RS256, jwkSet);
        assertThrows(OAuthException.class, () -> validator.validateLogoutToken(token));
    }

    @Test
    public void shouldRejectLogoutTokenWithoutEvents() throws Exception {
        final JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer(ISSUER)
                .audience(CLIENT_ID.getValue())
                .subject("user123")
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + 3600000))
                // Missing events claim
                .build();
        final String token = sign(claimsSet);

        final IdTokenValidator validator = new IdTokenValidator(ISSUER, CLIENT_ID, JWSAlgorithm.RS256, jwkSet);
        assertThrows(OAuthException.class, () -> validator.validateLogoutToken(token));
    }

    @Test
    public void shouldRejectTokenIfKeyTypeMismatch() throws Exception {
        final SignedJWT signedJWT = createSignedJWTWithRsa(rsaKey, JWSAlgorithm.RS256);
        // We configure the validator with ONLY an EC Key but we send an RSA signed JWT
        final com.nimbusds.jose.jwk.ECKey ecKey = new com.nimbusds.jose.jwk.gen.ECKeyGenerator(
                com.nimbusds.jose.jwk.Curve.P_256)
                .keyID("ec-1").generate();
        final IdTokenValidator validator = new IdTokenValidator(ISSUER, CLIENT_ID, JWSAlgorithm.RS256,
                new JWKSet(ecKey));

        assertThrows(OAuthException.class, () -> validator.validate(signedJWT.serialize(), new Nonce("nonce123"), 0));
    }

    private SignedJWT createSignedJWTWithRsa(final RSAKey key, final JWSAlgorithm alg) throws Exception {
        final SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(alg).keyID(key.getKeyID()).build(),
                createBaseClaims().build());
        signedJWT.sign(new com.nimbusds.jose.crypto.RSASSASigner(key));
        return signedJWT;
    }

    private JWTClaimsSet.Builder createBaseClaims() {
        return new JWTClaimsSet.Builder()
                .issuer(ISSUER)
                .audience(CLIENT_ID.getValue())
                .subject("user123")
                .claim("nonce", "nonce123");
    }

    private String sign(final JWTClaimsSet claims) throws Exception {
        final JWSSigner signer = new RSASSASigner(rsaKey);
        final SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.getKeyID()).build(),
                claims);
        signedJWT.sign(signer);
        return signedJWT.serialize();
    }
}
