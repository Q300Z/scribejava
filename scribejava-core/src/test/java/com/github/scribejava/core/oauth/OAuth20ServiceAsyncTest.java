package com.github.scribejava.core.oauth;

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.httpclient.jdk.JDKHttpClient;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthAsyncRequestCallback;
import com.github.scribejava.core.model.PushedAuthorizationResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OAuth20ServiceAsyncTest {

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

            @Override
            public String getRevokeTokenEndpoint() {
                return server.url("/revoke").toString();
            }

            @Override
            public String getPushedAuthorizationRequestEndpoint() {
                return server.url("/par").toString();
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
    public void shouldGetAccessTokenAsync() throws Exception {
        server.enqueue(new MockResponse().setBody("{\"access_token\":\"at123\"}").setResponseCode(200));
        final OAuth2AccessToken token = service.getAccessToken("code123",
                (OAuthAsyncRequestCallback<OAuth2AccessToken>) null).get();
        assertThat(token.getAccessToken()).isEqualTo("at123");
    }

    @Test
    public void shouldRefreshAccessTokenAsync() throws Exception {
        server.enqueue(new MockResponse().setBody("{\"access_token\":\"at456\"}").setResponseCode(200));
        final OAuth2AccessToken token = service.refreshAccessToken("rt123",
                (OAuthAsyncRequestCallback<OAuth2AccessToken>) null).get();
        assertThat(token.getAccessToken()).isEqualTo("at456");
    }

    @Test
    public void shouldRevokeTokenAsync() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200));
        service.revokeTokenAsync("at123").get();
    }

    @Test
    public void shouldPushAuthorizationRequestAsync() throws Exception {
        server.enqueue(new MockResponse().setBody("{\"request_uri\":\"urn:par:123\", \"expires_in\":90}")
                .setResponseCode(201));
        final PushedAuthorizationResponse resp = service.pushAuthorizationRequestAsync("code", "api-key",
                "callback", "scope", "state", null).get();
        assertThat(resp.getRequestUri()).isEqualTo("urn:par:123");
    }

    @Test
    public void shouldHandleErrorInAsyncRequest() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(500).setBody("Internal Server Error"));
        assertThrows(ExecutionException.class, () -> service.getAccessToken("code123",
                (OAuthAsyncRequestCallback<OAuth2AccessToken>) null).get());
    }
}
