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
package com.github.scribejava.core.builder;

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.httpclient.jdk.JDKHttpClientConfig;
import com.github.scribejava.core.oauth.OAuth20Service;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ServiceBuilderTest {

    @Test
    public void shouldBuildServiceWithAllOptions() {
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
                };

        final OAuth20Service service =
                new ServiceBuilder("api-key")
                        .apiSecret("api-secret")
                        .callback("http://callback.com")
                        .defaultScope("default-scope")
                        .responseType("token")
                        .userAgent("ScribeJava")
                        .httpClientConfig(JDKHttpClientConfig.defaultConfig())
                        .debug()
                        .build(api);

        assertThat(service.getApiKey()).isEqualTo("api-key");
        assertThat(service.getApiSecret()).isEqualTo("api-secret");
        assertThat(service.getCallback()).isEqualTo("http://callback.com");
        assertThat(service.getDefaultScope()).isEqualTo("default-scope");
        assertThat(service.getResponseType()).isEqualTo("token");
    }

    @Test
    public void shouldSupportScopeBuilder() {
        final ScopeBuilder scopeBuilder = new ScopeBuilder("scope1", "scope2");
        final String scopes = scopeBuilder.build();
        assertThat(scopes).contains("scope1");
        assertThat(scopes).contains("scope2");

        final ScopeBuilder scopeBuilder2 = new ScopeBuilder();
        scopeBuilder2.withScope("scope3").withScopes("scope4", "scope5");
        final String builtScopes = scopeBuilder2.build();
        assertThat(builtScopes).contains("scope3");
        assertThat(builtScopes).contains("scope4");
        assertThat(builtScopes).contains("scope5");
    }
}
