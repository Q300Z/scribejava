package com.github.scribejava.core.oauth;

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.model.OAuth2Authorization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OAuth20ServiceHelpersTest {

    private OAuth20Service service;

    @BeforeEach
    public void setUp() {
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
        service = new OAuth20Service(api, "api-key", "api-secret", "callback", "scope", "code", null, null, null, null);
    }

    @Test
    public void shouldExtractAuthorization() {
        final String url = "http://callback.com?code=auth_code&state=auth_state";
        final OAuth2Authorization auth = service.extractAuthorization(url);
        assertThat(auth.getCode()).isEqualTo("auth_code");
        assertThat(auth.getState()).isEqualTo("auth_state");
    }

    @Test
    public void shouldExtractAuthorizationWithFragment() {
        final String url = "http://callback.com?code=auth_code&state=auth_state#fragment";
        final OAuth2Authorization auth = service.extractAuthorization(url);
        assertThat(auth.getCode()).isEqualTo("auth_code");
        assertThat(auth.getState()).isEqualTo("auth_state");
    }

    @Test
    public void shouldGetResponseType() {
        assertThat(service.getResponseType()).isEqualTo("code");
    }

    @Test
    public void shouldGetDefaultScope() {
        assertThat(service.getDefaultScope()).isEqualTo("scope");
    }

    @Test
    public void shouldGetVersion() {
        assertThat(service.getVersion()).isEqualTo("2.0");
    }
}
