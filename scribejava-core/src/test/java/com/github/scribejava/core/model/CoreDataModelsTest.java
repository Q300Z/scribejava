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
package com.github.scribejava.core.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CoreDataModelsTest {

    @Test
    public void testOAuth2AccessToken() {
        final OAuth2AccessToken token =
                new OAuth2AccessToken("access", "bearer", 3600, "refresh", "scope", "raw");
        assertThat(token.getAccessToken()).isEqualTo("access");
        assertThat(token.getTokenType()).isEqualTo("bearer");
        assertThat(token.getExpiresIn()).isEqualTo(3600);
        assertThat(token.getRefreshToken()).isEqualTo("refresh");
        assertThat(token.getScope()).isEqualTo("scope");
        assertThat(token.getRawResponse()).isEqualTo("raw");

        final OAuth2AccessToken simpleToken = new OAuth2AccessToken("access");
        assertThat(simpleToken.getAccessToken()).isEqualTo("access");
        assertThat(simpleToken.getRefreshToken()).isNull();
    }

    @Test
    public void testDeviceAuthorization() {
        final DeviceAuthorization auth = new DeviceAuthorization("d_code", "u_code", "uri", 600);
        assertThat(auth.getDeviceCode()).isEqualTo("d_code");
        assertThat(auth.getUserCode()).isEqualTo("u_code");
        assertThat(auth.getVerificationUri()).isEqualTo("uri");
        assertThat(auth.getExpiresInSeconds()).isEqualTo(600);

        auth.setIntervalSeconds(10);
        assertThat(auth.getIntervalSeconds()).isEqualTo(10);

        auth.setVerificationUriComplete("uri_complete");
        assertThat(auth.getVerificationUriComplete()).isEqualTo("uri_complete");
    }

    @Test
    public void testPushedAuthorizationResponse() {
        final PushedAuthorizationResponse par =
                new PushedAuthorizationResponse("urn:uri", 90L, "raw_resp");
        assertThat(par.getRequestUri()).isEqualTo("urn:uri");
        assertThat(par.getExpiresIn()).isEqualTo(90L);
        assertThat(par.getRawResponse()).isEqualTo("raw_resp");
    }

    @Test
    public void testOAuth2Authorization() {
        final OAuth2Authorization auth = new OAuth2Authorization();
        auth.setCode("code123");
        auth.setState("state123");
        assertThat(auth.getCode()).isEqualTo("code123");
        assertThat(auth.getState()).isEqualTo("state123");
    }
}
