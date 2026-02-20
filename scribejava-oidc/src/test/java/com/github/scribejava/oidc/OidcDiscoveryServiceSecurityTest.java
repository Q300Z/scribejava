package com.github.scribejava.oidc;

import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.httpclient.jdk.JDKHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OidcDiscoveryServiceSecurityTest {

    private MockWebServer server;

    @BeforeEach
    public void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    public void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    public void shouldRejectMetadataWithMismatchingIssuer() {
        final String issuer = server.url("/").toString();
        final String discoveryJson = "{"
                + "\"issuer\":\"https://malicious-issuer.com\","
                + "\"authorization_endpoint\":\"" + issuer + "auth\","
                + "\"token_endpoint\":\"" + issuer + "token\","
                + "\"jwks_uri\":\"" + issuer + "keys\""
                + "}";

        server.enqueue(new MockResponse().setBody(discoveryJson).setResponseCode(200));

        final OidcDiscoveryService discoveryService = new OidcDiscoveryService(issuer, new JDKHttpClient(),
                "ScribeJava");
        final ExecutionException ex = assertThrows(ExecutionException.class, discoveryService::getProviderMetadata);
        assertThat(ex.getCause()).isInstanceOf(OAuthException.class);
        assertThat(ex.getCause().getMessage()).contains("Issuer mismatch");
    }

    @Test
    public void shouldRejectIssuerMismatch() {
        final OidcService oidcService = new OidcService(null, "client-id", "secret", "callback", null, "code", null,
                null, null, new JDKHttpClient(), null);
        assertThrows(OAuthException.class, () -> oidcService.validateIssuerResponse("https://malicious.com",
                "https://expected-idp.com"));
    }
}
