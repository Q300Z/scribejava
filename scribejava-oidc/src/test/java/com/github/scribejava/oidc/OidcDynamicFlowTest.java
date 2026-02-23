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
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests du flux de découverte dynamique OpenID Connect.
 */
public class OidcDynamicFlowTest {

    private MockWebServer server;
    private OidcDiscoveryService discoveryService;
    private String issuer;

    /**
     * Initialisation du serveur de simulation.
     *
     * @throws IOException en cas d'erreur.
     */
    @BeforeEach
    public void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        issuer = server.url("/").toString();
        discoveryService = new OidcDiscoveryService(issuer, new JDKHttpClient(), "UA");
    }

    /**
     * Arrêt du serveur.
     *
     * @throws IOException en cas d'erreur.
     */
    @AfterEach
    public void tearDown() throws IOException {
        server.shutdown();
    }

    /**
     * Vérifie la découverte correcte des métadonnées du fournisseur.
     */
    @Test
    public void shouldDiscoverMetadata() throws Exception {
        final String json =
                "{\"issuer\":\""
                        + issuer
                        + "\", \"authorization_endpoint\":\""
                        + issuer
                        + "auth\", "
                        + "\"token_endpoint\":\""
                        + issuer
                        + "token\", \"jwks_uri\":\""
                        + issuer
                        + "jwks\", "
                        + "\"subject_types_supported\":[\"public\"]}";
        server.enqueue(new MockResponse().setBody(json).setResponseCode(200));

        final OidcProviderMetadata metadata = discoveryService.getProviderMetadata();
        assertThat(metadata.getIssuer()).isEqualTo(issuer);
        assertThat(metadata.getTokenEndpoint()).isEqualTo(issuer + "token");
    }

    /**
     * Vérifie la gestion d'une réponse de découverte malformée.
     */
    @Test
    public void shouldHandleMalformedDiscoveryResponse() {
        server.enqueue(new MockResponse().setBody("not-json").setResponseCode(200));
        assertThrows(Exception.class, () -> discoveryService.getProviderMetadata());
    }

    /**
     * Vérifie la gestion d'une erreur HTTP lors de la découverte.
     */
    @Test
    public void shouldHandleDiscoveryHttpError() {
        server.enqueue(new MockResponse().setResponseCode(404));
        assertThrows(Exception.class, () -> discoveryService.getProviderMetadata());
    }

    /**
     * Vérifie que la différence entre l'émetteur attendu et reçu lève une erreur.
     */
    @Test
    public void shouldHandleIssuerMismatch() {
        final String json =
                "{\"issuer\":\"https://mismatch.com\", \"authorization_endpoint\":\""
                        + issuer
                        + "auth\","
                        + " \"token_endpoint\":\""
                        + issuer
                        + "token\", \"jwks_uri\":\""
                        + issuer
                        + "jwks\","
                        + " \"subject_types_supported\":[\"public\"]}";
        server.enqueue(new MockResponse().setBody(json).setResponseCode(200));

        final ExecutionException ex =
                assertThrows(ExecutionException.class, () -> discoveryService.getProviderMetadata());
        assertThat(ex.getCause().getMessage()).contains("Issuer mismatch");
    }
}
