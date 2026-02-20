package com.github.scribejava.core.model;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

public class OAuthRequestPayloadTest {

    @Test
    public void shouldHandleStringPayload() {
        final OAuthRequest request = new OAuthRequest(Verb.POST, "http://example.com");
        final String payload = "{\"key\":\"value\"}";
        request.setPayload(payload);

        assertThat(request.getStringPayload()).isEqualTo(payload);
        // getByteArrayPayload() returns body parameters, not the string payload
        assertThat(request.getByteArrayPayload()).isEmpty();
    }

    @Test
    public void shouldHandleSpecialCharactersInStringPayload() {
        final OAuthRequest request = new OAuthRequest(Verb.POST, "http://example.com");
        final String payload = "{\"key\":\"valué ❤️\"}";
        request.setPayload(payload);
        request.setCharset(StandardCharsets.UTF_8.name());

        assertThat(request.getStringPayload()).isEqualTo(payload);
        assertThat(request.getByteArrayPayload()).isEmpty();
    }

    @Test
    public void shouldHandleByteArrayPayload() {
        final OAuthRequest request = new OAuthRequest(Verb.POST, "http://example.com");
        final byte[] payload = {0x01, 0x02, 0x03, 0x04};
        request.setPayload(payload);

        assertThat(request.getStringPayload()).isNull();
        assertThat(request.getByteArrayPayload()).isEqualTo(payload);
    }

    @Test
    public void shouldHandleFilePayload() {
        final OAuthRequest request = new OAuthRequest(Verb.POST, "http://example.com");
        final File file = new File("dummy-file");
        request.setPayload(file);

        assertThat(request.getFilePayload()).isEqualTo(file);
        assertThat(request.getStringPayload()).isNull();
        // getByteArrayPayload() should still work if body parameters are added
        request.addBodyParameter("key", "value");
        assertThat(new String(request.getByteArrayPayload(), StandardCharsets.UTF_8)).isEqualTo("key=value");
    }

    @Test
    public void shouldHandleNoPayloadForGet() {
        final OAuthRequest request = new OAuthRequest(Verb.GET, "http://example.com");
        assertThat(request.getStringPayload()).isNull();
        assertThat(request.getByteArrayPayload()).isEmpty();
    }

    @Test
    public void shouldHandleNoPayloadForDelete() {
        final OAuthRequest request = new OAuthRequest(Verb.DELETE, "http://example.com");
        assertThat(request.getStringPayload()).isNull();
        assertThat(request.getByteArrayPayload()).isEmpty();
    }

    @Test
    public void shouldResetPayloadWhenNewOneIsSet() {
        final OAuthRequest request = new OAuthRequest(Verb.POST, "http://example.com");
        request.setPayload("string-payload");
        assertThat(request.getStringPayload()).isNotNull();

        request.setPayload(new byte[]{0x01});
        assertThat(request.getStringPayload()).isNull();
        assertThat(request.getByteArrayPayload()).containsExactly(0x01);

        request.setPayload(new File("file"));
        assertThat(request.getByteArrayPayload()).isEmpty();
        assertThat(request.getFilePayload()).isNotNull();
    }
}
