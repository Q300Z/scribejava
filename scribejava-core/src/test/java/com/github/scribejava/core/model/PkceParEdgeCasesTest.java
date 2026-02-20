package com.github.scribejava.core.model;

import com.github.scribejava.core.exceptions.OAuthException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PkceParEdgeCasesTest {

    @Test
    public void shouldParseValidParResponse() throws IOException {
        final String json = "{\"request_uri\":\"urn:par:123\", \"expires_in\":3600}";
        final PushedAuthorizationResponse resp = PushedAuthorizationResponse.parse(json);
        assertThat(resp.getRequestUri()).isEqualTo("urn:par:123");
        assertThat(resp.getExpiresIn()).isEqualTo(3600L);
    }

    @Test
    public void shouldParseParResponseWithMissingExpiresIn() throws IOException {
        final String json = "{\"request_uri\":\"urn:par:123\"}";
        final PushedAuthorizationResponse resp = PushedAuthorizationResponse.parse(json);
        assertThat(resp.getRequestUri()).isEqualTo("urn:par:123");
        assertThat(resp.getExpiresIn()).isNull();
    }

    @Test
    public void shouldHandleMissingRequestUri() {
        final String json = "{\"expires_in\":3600}";
        assertThrows(OAuthException.class, () -> PushedAuthorizationResponse.parse(json));
    }

    @Test
    public void shouldHandleEmptyParResponse() {
        assertThrows(IllegalArgumentException.class, () -> PushedAuthorizationResponse.parse(""));
    }

    @Test
    public void shouldHandleInvalidJsonInParResponse() {
        assertThrows(IOException.class, () -> PushedAuthorizationResponse.parse("{invalid}"));
    }

    @Test
    public void shouldHandleImmediateExpiration() throws IOException {
        final String json = "{\"request_uri\":\"urn:par:123\", \"expires_in\":0}";
        final PushedAuthorizationResponse resp = PushedAuthorizationResponse.parse(json);
        assertThat(resp.getExpiresIn()).isEqualTo(0L);
    }
}
