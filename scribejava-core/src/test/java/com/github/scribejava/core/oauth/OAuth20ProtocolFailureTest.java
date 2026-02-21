package com.github.scribejava.core.oauth;

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.httpclient.jdk.JDKHttpClient;
import com.github.scribejava.core.httpclient.jdk.JDKHttpClientConfig;
import com.github.scribejava.core.model.OAuth2AccessTokenErrorResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.SocketTimeoutException;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class OAuth20ProtocolFailureTest {

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

        final JDKHttpClientConfig config = JDKHttpClientConfig.defaultConfig();
        config.setConnectTimeout(500);
        config.setReadTimeout(500);

        service = new OAuth20Service(api, "api-key", "api-secret", "callback", null, "code", null, null, config,
                new JDKHttpClient(config));
    }

    @AfterEach
    public void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    public void shouldFailOnOAuthErrorResponse() {
        server.enqueue(new MockResponse().setResponseCode(400)
                .setBody("{\"error\":\"invalid_grant\", \"error_description\":\"code expired\"}"));
        assertThrows(OAuth2AccessTokenErrorResponse.class, () -> service.getAccessToken("some-code"));
    }

    @Test
    public void shouldFailOnEmptyCode() {
        // null code throws IllegalArgumentException from Preconditions
        assertThrows(IllegalArgumentException.class, () -> service.getAccessToken((String) null));

        // empty code is NOT checked by Preconditions in OAuth20Service, so it hits the network.
        assertThrows(SocketTimeoutException.class, () -> service.getAccessToken(""));
    }

    @Test
    public void shouldFailOnEmptyRefreshToken() {
        assertThrows(IllegalArgumentException.class, () -> service.refreshAccessToken(null));
        assertThrows(IllegalArgumentException.class, () -> service.refreshAccessToken(""));
    }
}
