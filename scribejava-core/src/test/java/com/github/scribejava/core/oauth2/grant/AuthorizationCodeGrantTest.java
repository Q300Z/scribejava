package com.github.scribejava.core.oauth2.grant;

import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.builder.api.DefaultApi20;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class AuthorizationCodeGrantTest {

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
                return "http://test.com/auth";
            }
        };
        service = new OAuth20Service(api, "api-key", "api-secret", "callback", null, null, null, null, null, null);
    }

    @Test
    public void shouldCreateAuthorizationCodeRequest() {
        final AuthorizationCodeGrant grant = new AuthorizationCodeGrant("code123");
        grant.setPkceCodeVerifier("verifier");

        final OAuthRequest request = grant.createRequest(service);

        assertThat(request.getBodyParams().asFormUrlEncodedString()).contains("code=code123");
        assertThat(request.getBodyParams().asFormUrlEncodedString()).contains("grant_type=authorization_code");
        assertThat(request.getBodyParams().asFormUrlEncodedString()).contains("code_verifier=verifier");
    }
}
