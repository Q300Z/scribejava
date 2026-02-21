package com.github.scribejava.oidc;

import com.github.scribejava.core.httpclient.jdk.JDKHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class OidcApiDiscoveryTest {

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
    public void shouldConfigureEndpointsAutomatically() throws Exception {
        final String issuer = server.url("/").toString();
        final String discoveryJson = "{"
                + "\"issuer\":\"" + issuer + "\","
                + "\"authorization_endpoint\":\"" + issuer + "auth\","
                + "\"token_endpoint\":\"" + issuer + "token\","
                + "\"jwks_uri\":\"" + issuer + "keys\""
                + "}";

        server.enqueue(new MockResponse().setBody(discoveryJson).setResponseCode(200));

        final TestOidcApi api = new TestOidcApi(issuer);
        final OidcDiscoveryService discoveryService = new OidcDiscoveryService(issuer, new JDKHttpClient(),
                "ScribeJava");
        api.setMetadata(discoveryService.getProviderMetadata());

        assertThat(api.getAuthorizationBaseUrl()).isEqualTo(issuer + "auth");
        assertThat(api.getAccessTokenEndpoint()).isEqualTo(issuer + "token");
        assertThat(api.getJwksUri()).isEqualTo(issuer + "keys");
    }

    public static class TestOidcApi extends DefaultOidcApi20 {
        private final String issuer;

        public TestOidcApi(final String issuer) {
            this.issuer = issuer;
        }

        @Override
        public String getIssuer() {
            return issuer;
        }
    }
}
