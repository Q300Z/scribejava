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
package com.github.scribejava.core.oauth;

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.httpclient.jdk.JDKHttpClient;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuth2AccessTokenErrorResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests des différents types de concession (grants) OAuth 2.0.
 */
public class OAuth20ServiceGrantsTest {

    private MockWebServer server;
    private OAuth20Service service;

    /**
     * Initialisation du serveur et du service.
     *
     * @throws IOException en cas d'erreur.
     */
    @BeforeEach
    public void setUp() throws IOException {
        server = new MockWebServer();
        server.start();

        final DefaultApi20 api =
                new DefaultApi20() {
                    @Override
                    public String getAccessTokenEndpoint() {
                        return server.url("/token").toString();
                    }

                    @Override
                    public String getAuthorizationBaseUrl() {
                        return server.url("/auth").toString();
                    }
                };

        service =
                new OAuth20Service(
                        api,
                        "api-key",
                        "api-secret",
                        "callback",
                        "scope",
                        "code",
                        null,
                        null,
                        null,
                        new JDKHttpClient());
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
     * Vérifie l'obtention d'un jeton via Client Credentials Grant.
     */
    @Test
    public void shouldGetAccessTokenClientCredentialsGrantAsync() throws Exception {
        server.enqueue(
                new MockResponse().setBody("{\"access_token\":\"cc-token\"}").setResponseCode(200));
        final OAuth2AccessToken token = service.getAccessTokenClientCredentialsGrantAsync().get();
        assertThat(token.getAccessToken()).isEqualTo("cc-token");
    }

    /**
     * Vérifie l'obtention d'un jeton via Resource Owner Password Credentials Grant.
     */
    @Test
    public void shouldGetAccessTokenPasswordGrantAsync() throws Exception {
        server.enqueue(
                new MockResponse().setBody("{\"access_token\":\"pwd-token\"}").setResponseCode(200));
        final OAuth2AccessToken token = service.getAccessTokenPasswordGrantAsync("user", "pass").get();
        assertThat(token.getAccessToken()).isEqualTo("pwd-token");
    }

    /**
     * Vérifie la gestion d'une réponse d'erreur OAuth standard (RFC 6749).
     */
    @Test
    public void shouldHandleOAuthErrorResponse() {
        final String errorJson = "{\"error\":\"invalid_grant\", \"error_description\":\"bad code\"}";
        server.enqueue(new MockResponse().setBody(errorJson).setResponseCode(400));

        final OAuth2AccessTokenErrorResponse oauthEx =
                assertThrows(OAuth2AccessTokenErrorResponse.class, () -> service.getAccessToken("code123"));

        assertThat(oauthEx.getErrorDescription()).isEqualTo("bad code");
    }

    /**
     * Vérifie la gestion d'une réponse d'erreur vide.
     */
    @Test
    public void shouldHandleEmptyErrorResponse() {
        server.enqueue(new MockResponse().setResponseCode(401).setBody(""));

        assertThrows(IllegalArgumentException.class, () -> service.getAccessToken("code123"));
    }
}
