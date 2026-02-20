package com.github.scribejava.core.model;

import com.github.scribejava.core.oauth2.OAuth2Error;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CoreExceptionsTest {

    @Test
    public void shouldTestOAuthResponseException() throws IOException {
        final Response response = new Response(401, "Unauthorized", Collections.emptyMap(), "body");
        final OAuthResponseException ex = new OAuthResponseException(response);
        assertThat(ex.getResponse()).isSameAs(response);
    }

    @Test
    public void shouldParseAllOAuth2Errors() {
        assertThat(OAuth2Error.parseFrom("invalid_request")).isEqualTo(OAuth2Error.INVALID_REQUEST);
        assertThat(OAuth2Error.parseFrom("unauthorized_client")).isEqualTo(OAuth2Error.UNAUTHORIZED_CLIENT);
        assertThat(OAuth2Error.parseFrom("access_denied")).isEqualTo(OAuth2Error.ACCESS_DENIED);
        assertThat(OAuth2Error.parseFrom("unsupported_response_type")).isEqualTo(OAuth2Error.UNSUPPORTED_RESPONSE_TYPE);
        assertThat(OAuth2Error.parseFrom("invalid_scope")).isEqualTo(OAuth2Error.INVALID_SCOPE);
        assertThat(OAuth2Error.parseFrom("server_error")).isEqualTo(OAuth2Error.SERVER_ERROR);
        assertThat(OAuth2Error.parseFrom("temporarily_unavailable")).isEqualTo(OAuth2Error.TEMPORARILY_UNAVAILABLE);

        assertThrows(IllegalArgumentException.class, () -> OAuth2Error.parseFrom("unknown"));
    }
}
