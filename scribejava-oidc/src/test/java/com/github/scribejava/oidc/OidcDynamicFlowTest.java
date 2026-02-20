package com.github.scribejava.oidc;

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

public class OidcDynamicFlowTest {

    private MockWebServer server;
    private OidcDiscoveryService discoveryService;
    private String issuer;

    @BeforeEach
    public void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        issuer = server.url("/").toString();
        discoveryService = new OidcDiscoveryService(issuer, new JDKHttpClient(), "UA");
    }

    @AfterEach
    public void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    public void shouldDiscoverMetadata() throws Exception {
        final String json = "{\"issuer\":\"" + issuer + "\", \"authorization_endpoint\":\"" + issuer + "auth\", "
                + "\"token_endpoint\":\"" + issuer + "token\", \"jwks_uri\":\"" + issuer + "jwks\", "
                + "\"subject_types_supported\":[\"public\"]}";
        server.enqueue(new MockResponse().setBody(json).setResponseCode(200));

        final OidcProviderMetadata metadata = discoveryService.getProviderMetadata();
        assertThat(metadata.getIssuer()).isEqualTo(issuer);
        assertThat(metadata.getTokenEndpoint()).isEqualTo(issuer + "token");
    }

    @Test
    public void shouldHandleMalformedDiscoveryResponse() {
        server.enqueue(new MockResponse().setBody("not-json").setResponseCode(200));
        assertThrows(Exception.class, () -> discoveryService.getProviderMetadata());
    }

    @Test
    public void shouldHandleDiscoveryHttpError() {
        server.enqueue(new MockResponse().setResponseCode(404));
        assertThrows(Exception.class, () -> discoveryService.getProviderMetadata());
    }

    @Test
    public void shouldHandleIssuerMismatch() {
        final String json = "{\"issuer\":\"https://mismatch.com\", \"authorization_endpoint\":\"" + issuer + "auth\","
                + " \"token_endpoint\":\"" + issuer + "token\", \"jwks_uri\":\"" + issuer + "jwks\","
                + " \"subject_types_supported\":[\"public\"]}";
        server.enqueue(new MockResponse().setBody(json).setResponseCode(200));

        final ExecutionException ex = assertThrows(ExecutionException.class,
                () -> discoveryService.getProviderMetadata());
        assertThat(ex.getCause().getMessage()).contains("Issuer mismatch");
    }
}
