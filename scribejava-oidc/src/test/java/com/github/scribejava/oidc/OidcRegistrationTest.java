package com.github.scribejava.oidc;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.scribejava.core.httpclient.jdk.JDKHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class OidcRegistrationTest {

    private MockWebServer server;
    private OidcRegistrationService service;

    @Before
    public void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        service = new OidcRegistrationService(new JDKHttpClient(), "ScribeJava-Test");
    }

    @After
    public void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    public void shouldRegisterClient() throws Exception {
        final String registrationResponse = "{"
                + "\"client_id\":\"generated-client-id\","
                + "\"client_secret\":\"generated-client-secret\","
                + "\"client_id_issued_at\":1577836800"
                + "}";

        server.enqueue(new MockResponse().setBody(registrationResponse).setResponseCode(201));

        final JsonNode result = service.registerClientAsync(
                server.url("/register").toString(),
                Arrays.asList("https://client.example.com/cb"),
                "My App",
                "private_key_jwt"
        ).get();

        assertNotNull(result);
        assertEquals("generated-client-id", result.get("client_id").asText());
        assertEquals("generated-client-secret", result.get("client_secret").asText());
    }
}
