package com.github.scribejava.oidc;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class OidcBoilerplateTest {

    @Test
    public void testStandardClaimsBoilerplate() {
        final Map<String, Object> map = new HashMap<>();
        map.put("sub", "123");
        map.put("name", "John");
        map.put("email_verified", true);

        final StandardClaims claims = new StandardClaims(map);
        assertThat(claims.getSub()).contains("123");
        assertThat(claims.getName()).contains("John");
        assertThat(claims.isEmailVerified()).contains(true);

        assertThat(claims.getGivenName()).isEmpty();
        assertThat(claims.getFamilyName()).isEmpty();
        assertThat(claims.getMiddleName()).isEmpty();
        assertThat(claims.getNickname()).isEmpty();
        assertThat(claims.getPreferredUsername()).isEmpty();
        assertThat(claims.getProfile()).isEmpty();
        assertThat(claims.getPicture()).isEmpty();
        assertThat(claims.getWebsite()).isEmpty();
        assertThat(claims.getEmail()).isEmpty();
        assertThat(claims.getGender()).isEmpty();
        assertThat(claims.getBirthdate()).isEmpty();
        assertThat(claims.getZoneinfo()).isEmpty();
        assertThat(claims.getLocale()).isEmpty();
        assertThat(claims.getPhoneNumber()).isEmpty();
        assertThat(claims.isPhoneNumberVerified()).isEmpty();
        assertThat(claims.getAddress()).isEmpty();
        assertThat(claims.getUpdatedAt()).isEmpty();
        assertThat(claims.getAllClaims()).isNotNull();
    }

    @Test
    public void testIdTokenBoilerplate() throws Exception {
        final JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject("123")
                .issuer("https://idp.com")
                .claim("nonce", "nonce123")
                .build();
        final SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
        signedJWT.sign(new MACSigner("secretsecretsecretsecretsecretsecretsecretsecret"));
        final String rawToken = signedJWT.serialize();

        final IdToken token = new IdToken(rawToken);
        assertThat(token.getRawResponse()).isEqualTo(rawToken);
        assertThat(token.getSubject()).isEqualTo("123");
        assertThat(token.getIssuer()).isEqualTo("https://idp.com");
        assertThat(token.getNonce()).isEqualTo("nonce123");
        assertThat(token.getClaimsSet()).isNotNull();
        assertThat(token.getStandardClaims().getSub()).contains("123");
        assertThat(token.getClaim("sub")).isEqualTo("123");
        assertThat(token.getClaims()).isNotNull();
    }
}
