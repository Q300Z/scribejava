/*
 * The MIT License
 *
 * Copyright (c) 2010 Pablo Fernandez
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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

/**
 * Tests du service de découverte (Discovery) OIDC.
 */
public class OidcDiscoveryServiceTest {

    private MockWebServer server;
    private OidcDiscoveryService service;

    /**
     * Initialisation du serveur et du service.
     *
     * @throws IOException en cas d'erreur.
     */
    @Before
    public void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        service =
                new OidcDiscoveryService(
                        server.url("/").toString(), new JDKHttpClient(), "ScribeJava-Test");
    }

    /**
     * Arrêt du serveur.
     *
     * @throws IOException en cas d'erreur.
     */
    @After
    public void tearDown() throws IOException {
        server.shutdown();
    }

    /**
     * Vérifie la récupération des métadonnées du fournisseur.
     */
    @Test
    public void shouldFetchMetadata() throws Exception {
        final String json =
                "{"
                        + "\"issuer\":\""
                        + server.url("/")
                        + "\","
                        + "\"authorization_endpoint\":\""
                        + server.url("/authorize")
                        + "\","
                        + "\"token_endpoint\":\""
                        + server.url("/token")
                        + "\","
                        + "\"jwks_uri\":\""
                        + server.url("/jwks.json")
                        + "\","
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

    /**
     * Vérifie la récupération des clés JWKS.
     */
    @Test
    public void shouldFetchJwks() throws Exception {
        final String jwksJson =
                "{\"keys\":[{"
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
