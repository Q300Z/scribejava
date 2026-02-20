package com.github.scribejava.core.exceptions;

import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OAuthExceptionsCoverageTest {

    @Test
    public void testOAuthParametersMissingException() {
        final OAuthRequest request = new OAuthRequest(Verb.GET, "http://example.com");
        final OAuthParametersMissingException ex = new OAuthParametersMissingException(request);
        assertThat(ex.getMessage()).contains("http://example.com");
    }

    @Test
    public void testOAuthSignatureException() {
        final Exception cause = new Exception("cause");
        final OAuthSignatureException ex = new OAuthSignatureException("message", cause);
        assertThat(ex.getMessage()).contains("message");
        assertThat(ex.getCause()).isSameAs(cause);
    }
}
