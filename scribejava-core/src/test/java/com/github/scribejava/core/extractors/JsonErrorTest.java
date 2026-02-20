package com.github.scribejava.core.extractors;

import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.Response;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JsonErrorTest {

    @Test
    public void shouldHandleNonJsonObject() {
        final Response response = new Response(200, "OK", Collections.emptyMap(), "\"not an object\"");
        assertThrows(OAuthException.class, () -> OAuth2AccessTokenJsonExtractor.instance().extract(response));
    }

    @Test
    public void shouldHandleMissingRequiredAccessToken() {
        final Response response = new Response(200, "OK", Collections.emptyMap(), "{\"expires_in\":3600}");
        final OAuthException ex = assertThrows(OAuthException.class,
                () -> OAuth2AccessTokenJsonExtractor.instance().extract(response));
        assertThat(ex.getMessage()).contains("access_token");
    }

    @Test
    public void shouldHandleInvalidJsonSyntax() {
        final Response response = new Response(200, "OK", Collections.emptyMap(), "{invalid}");
        // Jackson throws JsonProcessingException which is an IOException
        assertThrows(IOException.class, () -> OAuth2AccessTokenJsonExtractor.instance().extract(response));
    }

    @Test
    public void shouldHandleInvertedTypes() throws IOException {
        // expires_in as string "3600" should be handled by JsonNode.asInt()
        final String json = "{\"access_token\":\"at123\", \"expires_in\":\"3600\"}";
        final Response response = new Response(200, "OK", Collections.emptyMap(), json);
        final OAuth2AccessToken token = OAuth2AccessTokenJsonExtractor.instance().extract(response);
        assertThat(token.getAccessToken()).isEqualTo("at123");
        assertThat(token.getExpiresIn()).isEqualTo(3600);
    }

    @Test
    public void shouldHandleNullRequiredParameter() {
        final Response response = new Response(200, "OK", Collections.emptyMap(), "{\"access_token\":null}");
        assertThrows(OAuthException.class, () -> OAuth2AccessTokenJsonExtractor.instance().extract(response));
    }

    @Test
    public void shouldHandleEmptyResponse() {
        final Response response = new Response(200, "OK", Collections.emptyMap(), "");
        assertThrows(IllegalArgumentException.class, () -> OAuth2AccessTokenJsonExtractor.instance().extract(response));
    }

    @Test
    public void shouldHandleArrayInsteadOfObject() {
        final Response response = new Response(200, "OK", Collections.emptyMap(), "[{\"access_token\":\"at123\"}]");
        assertThrows(OAuthException.class, () -> OAuth2AccessTokenJsonExtractor.instance().extract(response));
    }

    @Test
    public void shouldHandleExtremeLongValueForExpiresIn() throws IOException {
        // Jackson will parse 999999999999999 as a LongNode, asInt() will return its int value (overflow)
        final String json = "{\"access_token\":\"at123\", \"expires_in\":999999999999999}";
        final Response response = new Response(200, "OK", Collections.emptyMap(), json);
        final OAuth2AccessToken token = OAuth2AccessTokenJsonExtractor.instance().extract(response);
        assertThat(token.getAccessToken()).isEqualTo("at123");
        // 999999999999999 in binary is ...
        // asInt() on LongNode returns (int) longValue
        assertThat(token.getExpiresIn()).isNotNull();
    }

    @Test
    public void shouldHandleNonNumericExpiresIn() throws IOException {
        final String json = "{\"access_token\":\"at123\", \"expires_in\":\"not-a-number\"}";
        final Response response = new Response(200, "OK", Collections.emptyMap(), json);
        final OAuth2AccessToken token = OAuth2AccessTokenJsonExtractor.instance().extract(response);
        assertThat(token.getAccessToken()).isEqualTo("at123");
        // asInt() returns 0 for non-numeric strings
        assertThat(token.getExpiresIn()).isEqualTo(0);
    }

    @Test
    public void shouldHandleNestedObjectForExpiresIn() throws IOException {
        final String json = "{\"access_token\":\"at123\", \"expires_in\":{\"nested\":3600}}";
        final Response response = new Response(200, "OK", Collections.emptyMap(), json);
        final OAuth2AccessToken token = OAuth2AccessTokenJsonExtractor.instance().extract(response);
        assertThat(token.getAccessToken()).isEqualTo("at123");
        // asInt() returns 0 for objects
        assertThat(token.getExpiresIn()).isEqualTo(0);
    }
}
