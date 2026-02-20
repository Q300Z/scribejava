package com.github.scribejava.core.oauth;

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuth2AccessTokenErrorResponse;
import com.github.scribejava.core.httpclient.jdk.JDKHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OAuth20ServiceGrantsTest {

    private MockWebServer server;
    private OAuth20Service service;

    @BeforeEach
    public void setUp() throws IOException {
        server = new MockWebServer();
        server.start();

        final DefaultApi20 api = new DefaultApi20() {
            @Override
            public String getAccessTokenEndpoint() {
                return server.url("/token").toString();
            }

            @Override
            public String getAuthorizationBaseUrl() {
                return server.url("/auth").toString();
            }
        };

        service = new OAuth20Service(api, "api-key", "api-secret", "callback", "scope", "code", null, null, null,
                new JDKHttpClient());
    }

    @AfterEach
    public void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    public void shouldGetAccessTokenClientCredentialsGrantAsync() throws Exception {
        server.enqueue(new MockResponse().setBody("{\"access_token\":\"cc-token\"}").setResponseCode(200));
        final OAuth2AccessToken token = service.getAccessTokenClientCredentialsGrantAsync().get();
        assertThat(token.getAccessToken()).isEqualTo("cc-token");
    }

    @Test
    public void shouldGetAccessTokenPasswordGrantAsync() throws Exception {
        server.enqueue(new MockResponse().setBody("{\"access_token\":\"pwd-token\"}").setResponseCode(200));
        final OAuth2AccessToken token = service.getAccessTokenPasswordGrantAsync("user", "pass").get();
        assertThat(token.getAccessToken()).isEqualTo("pwd-token");
    }

    @Test
    public void shouldHandleOAuthErrorResponse() {
        final String errorJson = "{\"error\":\"invalid_grant\", \"error_description\":\"bad code\"}";
        server.enqueue(new MockResponse().setBody(errorJson).setResponseCode(400));

        final OAuth2AccessTokenErrorResponse oauthEx = assertThrows(OAuth2AccessTokenErrorResponse.class,
                () -> service.getAccessToken("code123"));

        assertThat(oauthEx.getErrorDescription()).isEqualTo("bad code");
    }

    @Test
    public void shouldHandleEmptyErrorResponse() {
        server.enqueue(new MockResponse().setResponseCode(401).setBody(""));

        assertThrows(IllegalArgumentException.class, () -> service.getAccessToken("code123"));
    }
}
