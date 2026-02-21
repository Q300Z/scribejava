package com.github.scribejava.core.extractors;

import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.model.Response;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class CoreExtractionFailureTest {

    @Test
    public void shouldFailOnNon200Response() {
        final Response response = new Response(400, "Bad Request", Collections.emptyMap(), "{}");
        assertThrows(OAuthException.class, () -> OAuth2AccessTokenExtractor.instance().extract(response));
        assertThrows(OAuthException.class, () -> OAuth2AccessTokenJsonExtractor.instance().extract(response));
    }

    @Test
    public void shouldFailOnEmptyBody() {
        final Response response = new Response(200, "OK", Collections.emptyMap(), "");
        assertThrows(IllegalArgumentException.class, () -> OAuth2AccessTokenExtractor.instance().extract(response));
    }

    @Test
    public void shouldFailOnMissingAccessToken() {
        final Response response = new Response(200, "OK", Collections.emptyMap(), "expires_in=3600");
        assertThrows(OAuthException.class, () -> OAuth2AccessTokenExtractor.instance().extract(response));
    }

    @Test
    public void shouldFailOnInvalidJson() {
        final Response response = new Response(200, "OK", Collections.emptyMap(), "{not-json}");
        assertThrows(Exception.class, () -> OAuth2AccessTokenJsonExtractor.instance().extract(response));
    }
}
