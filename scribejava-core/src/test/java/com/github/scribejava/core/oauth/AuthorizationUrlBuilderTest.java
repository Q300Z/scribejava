package com.github.scribejava.core.oauth;

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.pkce.PKCE;
import com.github.scribejava.core.pkce.PKCECodeChallengeMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthorizationUrlBuilderTest {

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
        service = new OAuth20Service(api, "api-key", "api-secret", "callback", "default-scope", "code", null, null,
                null, null);
    }

    @Test
    public void shouldBuildSimpleAuthorizationUrl() {
        final String url = service.createAuthorizationUrlBuilder().build();
        assertThat(url).contains("response_type=code");
        assertThat(url).contains("client_id=api-key");
        assertThat(url).contains("redirect_uri=callback");
        assertThat(url).contains("scope=default-scope");
    }

    @Test
    public void shouldOverrideScopeAndState() {
        final String url = service.createAuthorizationUrlBuilder()
                .scope("custom-scope")
                .state("custom-state")
                .build();
        assertThat(url).contains("scope=custom-scope");
        assertThat(url).contains("state=custom-state");
    }

    @Test
    public void shouldAddAdditionalParameters() {
        final Map<String, String> params = new HashMap<>();
        params.put("display", "page");
        params.put("prompt", "login");

        final String url = service.createAuthorizationUrlBuilder()
                .additionalParams(params)
                .build();
        assertThat(url).contains("display=page");
        assertThat(url).contains("prompt=login");
    }

    @Test
    public void shouldSupportPKCE() {
        final PKCE pkce = new PKCE();
        pkce.setCodeChallenge("challenge123");
        pkce.setCodeChallengeMethod(PKCECodeChallengeMethod.S256);
        pkce.setCodeVerifier("verifier123");

        final String url = service.createAuthorizationUrlBuilder()
                .pkce(pkce)
                .build();
        assertThat(url).contains("code_challenge=challenge123");
        assertThat(url).contains("code_challenge_method=S256");
    }

    @Test
    public void shouldInitPKCEAutomatically() {
        final AuthorizationUrlBuilder builder = service.createAuthorizationUrlBuilder().initPKCE();
        final String url = builder.build();
        assertThat(url).contains("code_challenge=");
        assertThat(url).contains("code_challenge_method=S256");
        assertThat(builder.getPkce().getCodeVerifier()).isNotNull();
    }
}
