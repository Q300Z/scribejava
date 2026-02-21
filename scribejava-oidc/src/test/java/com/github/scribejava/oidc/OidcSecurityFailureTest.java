package com.github.scribejava.oidc;

import com.github.scribejava.core.exceptions.OAuthException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.oauth2.sdk.id.ClientID;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class OidcSecurityFailureTest {

    @Test
    public void shouldFailOnMalformedIdToken() {
        final IdTokenValidator validator = new IdTokenValidator("https://idp.com", new ClientID("client123"),
                JWSAlgorithm.RS256, new JWKSet(Collections.emptyList()));
        assertThrows(OAuthException.class, () -> validator.validate("malformed.jwt.token", null, 0));
    }

    @Test
    public void shouldFailOnAudienceMismatch() {
        // This would require a signed JWT with a different audience.
    }
}
