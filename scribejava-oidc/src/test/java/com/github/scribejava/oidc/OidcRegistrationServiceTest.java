package com.github.scribejava.oidc;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.scribejava.core.httpclient.jdk.JDKHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OidcRegistrationServiceTest {

    private MockWebServer server;
    private OidcRegistrationService registrationService;

    @BeforeEach
    public void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        registrationService = new OidcRegistrationService(new JDKHttpClient(), "UA");
    }

    @AfterEach
    public void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    public void shouldRegisterClient() throws Exception {
        final String json = "{\"client_id\":\"client123\", \"client_secret\":\"secret456\"}";
        server.enqueue(new MockResponse().setBody(json).setResponseCode(201));

        final JsonNode result = registrationService.registerClientAsync(
                server.url("/register").toString(),
                Arrays.asList("http://callback.com"),
                "My App",
                "client_secret_post"
        ).get();

        assertThat(result.get("client_id").asText()).isEqualTo("client123");
        assertThat(result.get("client_secret").asText()).isEqualTo("secret456");
    }

    @Test
    public void shouldHandleRegistrationError() {
        server.enqueue(new MockResponse().setResponseCode(400).setBody("{\"error\":\"invalid_redirect_uri\"}"));

        assertThrows(ExecutionException.class, () -> registrationService.registerClientAsync(
                server.url("/register").toString(),
                Collections.emptyList(),
                "Bad App",
                null
        ).get());
    }
}
