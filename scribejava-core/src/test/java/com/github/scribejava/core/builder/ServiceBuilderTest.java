package com.github.scribejava.core.builder;

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.httpclient.jdk.JDKHttpClientConfig;
import com.github.scribejava.core.oauth.OAuth20Service;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ServiceBuilderTest {

    @Test
    public void shouldBuildServiceWithAllOptions() {
        final DefaultApi20 api = new DefaultApi20() {
            @Override
            public String getAccessTokenEndpoint() {
                return "http://test.com/token";
            }

            @Override
            public String getAuthorizationBaseUrl() {
                return "https://test.com/auth";
            }
        };

        final OAuth20Service service = new ServiceBuilder("api-key")
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
