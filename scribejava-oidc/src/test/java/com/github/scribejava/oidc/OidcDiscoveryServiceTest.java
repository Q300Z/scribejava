package com.github.scribejava.oidc;

import com.github.scribejava.core.httpclient.jdk.JDKHttpClient;
import com.nimbusds.jose.jwk.JWKSet;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class OidcDiscoveryServiceTest {

    private MockWebServer server;
    private OidcDiscoveryService service;

    @Before
    public void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        service = new OidcDiscoveryService(server.url("/").toString(), new JDKHttpClient(), "ScribeJava-Test");
    }

    @After
    public void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    public void shouldFetchMetadata() throws Exception {
        final String json = "{"
                + "\"issuer\":\"" + server.url("/") + "\","
                + "\"authorization_endpoint\":\"" + server.url("/authorize") + "\","
                + "\"token_endpoint\":\"" + server.url("/token") + "\","
                + "\"jwks_uri\":\"" + server.url("/jwks.json") + "\","
                + "\"response_types_supported\":[\"code\"],"
                + "\"subject_types_supported\":[\"public\"],"
                + "\"id_token_signing_alg_values_supported\":[\"RS256\"]"
                + "}";

        server.enqueue(new MockResponse().setBody(json).setResponseCode(200));

        final OidcProviderMetadata metadata = service.getProviderMetadata();

        assertNotNull(metadata);
        assertEquals(server.url("/").toString(), metadata.getIssuer());
        assertEquals(server.url("/token").toString(), metadata.getTokenEndpoint());
    }

    @Test
    public void shouldFetchJwks() throws Exception {
        final String jwksJson = "{\"keys\":[{"
                + "\"kty\":\"RSA\","
                + "\"use\":\"sig\","
                + "\"kid\":\"123\","
                + "\"n\":\"abc\","
                + "\"e\":\"AQAB\""
                + "}]}";

        server.enqueue(new MockResponse().setBody(jwksJson).setResponseCode(200));

        final JWKSet jwkSet = service.getJwks(server.url("/jwks.json").toString());

        assertNotNull(jwkSet);
        assertNotNull(jwkSet.getKeys().get(0));
        assertEquals("123", jwkSet.getKeys().get(0).getKeyID());
    }
}
