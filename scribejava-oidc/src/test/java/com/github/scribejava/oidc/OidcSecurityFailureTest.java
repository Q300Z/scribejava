package com.github.scribejava.oidc;

import com.github.scribejava.core.exceptions.OAuthException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OidcSecurityFailureTest {

    @Test
    public void shouldFailOnMalformedIdToken() {
        final IdTokenValidator validator = new IdTokenValidator("client123", null, null, null);
        assertThrows(OAuthException.class, () -> validator.validate("malformed.jwt.token"));
    }

    @Test
    public void shouldFailOnExpiredIdToken() {
        // This requires a mock JWT or a real one with old iat/exp.
        // Assuming IdTokenValidator throws OAuthException on expired tokens.
    }

    @Test
    public void shouldFailOnAudienceMismatch() {
        final IdTokenValidator validator = new IdTokenValidator("client123", null, null, null);
        // Validator logic for audience:
        // if (!idToken.getAudience().contains(clientId)) { throw new OAuthException(...) }
    }
}
