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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test de non-régression pour le refactor du DeviceFlowHandler.
 * (Phase 4 du refactoring SOLID).
 */
public class DeviceFlowRefactorTest {

    private OAuth20Service service;
    private DefaultApi20 api;

    @BeforeEach
    public void setUp() {
        api = mock(DefaultApi20.class);
        when(api.getDeviceAuthorizationEndpoint()).thenReturn("http://example.com/device");

        service = mock(OAuth20Service.class);
        when(service.getApi()).thenReturn(api);
        when(service.getApiKey()).thenReturn("api-key");
    }

    /**
     * Vérifie que la configuration est bien propagée à la requête.
     */
    @Test
    public void shouldPropagateConfigurationToRequest() {
        final OAuth20DeviceFlowHandler handler = new OAuth20DeviceFlowHandler(service);
        final OAuthRequest request = handler.createDeviceAuthorizationCodesRequest("all");

        assertThat(request.getVerb()).isEqualTo(Verb.POST);
        assertThat(request.getUrl()).isEqualTo("http://example.com/device");
        assertThat(request.getBodyParams().asFormUrlEncodedString()).contains("client_id=api-key");
        assertThat(request.getBodyParams().asFormUrlEncodedString()).contains("scope=all");
    }
}
