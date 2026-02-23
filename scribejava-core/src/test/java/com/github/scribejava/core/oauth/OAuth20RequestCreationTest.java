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
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.revoke.TokenTypeHint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de la création des requêtes OAuth 2.0 par {@link OAuth20Service}.
 */
public class OAuth20RequestCreationTest {

    private OAuth20Service service;

    /**
     * Initialisation du service.
     */
    @BeforeEach
    public void setUp() {
        final DefaultApi20 api =
                new DefaultApi20() {
                    @Override
                    public String getAccessTokenEndpoint() {
                        return "http://test.com/token";
                    }

                    @Override
                    public String getAuthorizationBaseUrl() {
                        return "https://test.com/auth";
                    }

                    @Override
                    public String getRevokeTokenEndpoint() {
                        return "http://test.com/revoke";
                    }
                };
        service =
                new OAuth20Service(
                        api,
                        "api-key",
                        "api-secret",
                        "callback",
                        "default-scope",
                        "code",
                        null,
                        null,
                        null,
                        null);
    }

    /**
     * Vérifie la création d'une requête de rafraîchissement de jeton.
     */
    @Test
    public void shouldCreateRefreshTokenRequest() {
        final OAuthRequest request = service.createRefreshTokenRequest("rt123", "custom-scope");
        assertThat(request.getBodyParams().asFormUrlEncodedString()).contains("refresh_token=rt123");
        assertThat(request.getBodyParams().asFormUrlEncodedString()).contains("scope=custom-scope");
        assertThat(request.getBodyParams().asFormUrlEncodedString())
                .contains("grant_type=refresh_token");
    }

    /**
     * Vérifie l'utilisation de la portée par défaut dans la requête de rafraîchissement.
     */
    @Test
    public void shouldCreateRefreshTokenRequestWithDefaultScope() {
        final OAuthRequest request = service.createRefreshTokenRequest("rt123", null);
        assertThat(request.getBodyParams().asFormUrlEncodedString()).contains("scope=default-scope");
    }

    /**
     * Vérifie la création d'une requête Password Grant.
     */
    @Test
    public void shouldCreatePasswordGrantRequest() {
        final OAuthRequest request =
                service.createAccessTokenPasswordGrantRequest("user", "pass", "scope1");
        assertThat(request.getBodyParams().asFormUrlEncodedString()).contains("username=user");
        assertThat(request.getBodyParams().asFormUrlEncodedString()).contains("password=pass");
        assertThat(request.getBodyParams().asFormUrlEncodedString()).contains("grant_type=password");
    }

    /**
     * Vérifie la création d'une requête Client Credentials Grant.
     */
    @Test
    public void shouldCreateClientCredentialsGrantRequest() {
        final OAuthRequest request = service.createAccessTokenClientCredentialsGrantRequest("scope2");
        assertThat(request.getBodyParams().asFormUrlEncodedString())
                .contains("grant_type=client_credentials");
        assertThat(request.getBodyParams().asFormUrlEncodedString()).contains("scope=scope2");
    }

    /**
     * Vérifie la création d'une requête de révocation de jeton.
     */
    @Test
    public void shouldCreateRevokeTokenRequest() {
        final OAuthRequest request =
                service.createRevokeTokenRequest("at123", TokenTypeHint.ACCESS_TOKEN);
        assertThat(request.getBodyParams().asFormUrlEncodedString()).contains("token=at123");
        assertThat(request.getBodyParams().asFormUrlEncodedString())
                .contains("token_type_hint=access_token");
    }
}
