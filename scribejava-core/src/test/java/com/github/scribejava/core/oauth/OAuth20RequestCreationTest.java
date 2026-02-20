package com.github.scribejava.core.oauth;

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.revoke.TokenTypeHint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OAuth20RequestCreationTest {

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

            @Override
            public String getRevokeTokenEndpoint() {
                return "http://test.com/revoke";
            }
        };
        service = new OAuth20Service(api, "api-key", "api-secret", "callback", "default-scope", "code", null, null,
                null, null);
    }

    @Test
    public void shouldCreateRefreshTokenRequest() {
        final OAuthRequest request = service.createRefreshTokenRequest("rt123", "custom-scope");
        assertThat(request.getBodyParams().asFormUrlEncodedString()).contains("refresh_token=rt123");
        assertThat(request.getBodyParams().asFormUrlEncodedString()).contains("scope=custom-scope");
        assertThat(request.getBodyParams().asFormUrlEncodedString()).contains("grant_type=refresh_token");
    }

    @Test
    public void shouldCreateRefreshTokenRequestWithDefaultScope() {
        final OAuthRequest request = service.createRefreshTokenRequest("rt123", null);
        assertThat(request.getBodyParams().asFormUrlEncodedString()).contains("scope=default-scope");
    }

    @Test
    public void shouldCreatePasswordGrantRequest() {
        final OAuthRequest request = service.createAccessTokenPasswordGrantRequest("user", "pass", "scope1");
        assertThat(request.getBodyParams().asFormUrlEncodedString()).contains("username=user");
        assertThat(request.getBodyParams().asFormUrlEncodedString()).contains("password=pass");
        assertThat(request.getBodyParams().asFormUrlEncodedString()).contains("grant_type=password");
    }

    @Test
    public void shouldCreateClientCredentialsGrantRequest() {
        final OAuthRequest request = service.createAccessTokenClientCredentialsGrantRequest("scope2");
        assertThat(request.getBodyParams().asFormUrlEncodedString()).contains("grant_type=client_credentials");
        assertThat(request.getBodyParams().asFormUrlEncodedString()).contains("scope=scope2");
    }

    @Test
    public void shouldCreateRevokeTokenRequest() {
        final OAuthRequest request = service.createRevokeTokenRequest("at123", TokenTypeHint.ACCESS_TOKEN);
        assertThat(request.getBodyParams().asFormUrlEncodedString()).contains("token=at123");
        assertThat(request.getBodyParams().asFormUrlEncodedString()).contains("token_type_hint=access_token");
    }
}
