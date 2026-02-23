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
package com.github.scribejava.core.oauth2.grant;

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.oauth2.clientauthentication.HttpBasicAuthenticationScheme;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires pour {@link PasswordGrant}.
 */
public class PasswordGrantTest {

    private OAuth20Service service;
    private DefaultApi20 api;

    @BeforeEach
    public void setUp() {
        service = mock(OAuth20Service.class);
        api = mock(DefaultApi20.class);
        when(service.getApi()).thenReturn(api);
        when(service.getApiKey()).thenReturn("api-key");
        when(service.getApiSecret()).thenReturn("api-secret");
        when(api.getAccessTokenVerb()).thenReturn(Verb.POST);
        when(api.getAccessTokenEndpoint()).thenReturn("http://example.com/token");
        when(api.getClientAuthentication()).thenReturn(HttpBasicAuthenticationScheme.instance());
    }

    /**
     * Test de création de requête.
     */
    @Test
    public void shouldCreateCorrectRequest() {
        final PasswordGrant grant = new PasswordGrant("user", "pass", "all");
        final OAuthRequest request = grant.createRequest(service);

        assertThat(request.getVerb()).isEqualTo(Verb.POST);
        assertThat(request.getUrl()).isEqualTo("http://example.com/token");

        final String body = request.getBodyParams().asFormUrlEncodedString();
        assertThat(body).contains("username=user");
        assertThat(body).contains("password=pass");
        assertThat(body).contains("scope=all");
        assertThat(body).contains("grant_type=password");
    }

    /**
     * Test du scope par défaut.
     */
    @Test
    public void shouldFallbackToServiceDefaultScope() {
        when(service.getDefaultScope()).thenReturn("default-scope");
        final PasswordGrant grant = new PasswordGrant("user", "pass");
        final OAuthRequest request = grant.createRequest(service);

        assertThat(request.getBodyParams().asFormUrlEncodedString()).contains("scope=default-scope");
    }
}
