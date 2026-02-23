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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OidcProvidersTest {

    @Test
    public void shouldReturnCorrectGoogleOidcConfig() {
        final OidcGoogleApi20 api = OidcGoogleApi20.instance();
        assertThat(api.getIssuer()).isEqualTo("https://accounts.google.com");
        assertThat(api.getAccessTokenEndpoint()).isEqualTo("https://oauth2.googleapis.com/token");
        assertThat(api.getAuthorizationBaseUrl())
                .isEqualTo("https://accounts.google.com/o/oauth2/v2/auth");
    }

    @Test
    public void shouldReturnCorrectGitHubOidcConfig() {
        final OidcGitHubApi20 api = OidcGitHubApi20.instance();
        assertThat(api.getIssuer()).isEqualTo("https://token.actions.githubusercontent.com");
        assertThat(api.getAccessTokenEndpoint())
                .isEqualTo("https://github.com/login/oauth/access_token");
    }

    @Test
    public void shouldReturnCorrectMicrosoftOidcConfig() {
        final OidcMicrosoftAzureActiveDirectory20Api api =
                OidcMicrosoftAzureActiveDirectory20Api.instance();
        assertThat(api.getIssuer()).isEqualTo("https://login.microsoftonline.com/common/v2.0");

        final OidcMicrosoftAzureActiveDirectory20Api customApi =
                OidcMicrosoftAzureActiveDirectory20Api.custom("my-tenant");
        assertThat(customApi.getIssuer()).isEqualTo("https://login.microsoftonline.com/my-tenant/v2.0");
        assertThat(customApi.getAccessTokenEndpoint()).contains("my-tenant");
    }
}
