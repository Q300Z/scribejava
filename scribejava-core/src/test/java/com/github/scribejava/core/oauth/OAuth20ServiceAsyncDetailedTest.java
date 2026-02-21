package com.github.scribejava.core.oauth;

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.httpclient.jdk.JDKHttpClient;
import com.github.scribejava.core.httpclient.jdk.JDKHttpClientConfig;
import com.github.scribejava.core.model.DeviceAuthorization;
import com.github.scribejava.core.model.OAuth2AccessToken;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class OAuth20ServiceAsyncDetailedTest {

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
            public String getDeviceAuthorizationEndpoint() {
                return server.url("/device").toString();
            }
        };

        final JDKHttpClientConfig config = JDKHttpClientConfig.defaultConfig();
        config.setConnectTimeout(1000);
        config.setReadTimeout(1000);

        service = new OAuth20Service(api, "api-key", "api-secret", "callback", "scope", "code", null, null, config,
                new JDKHttpClient(config));
    }

    @AfterEach
    public void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    public void shouldGetAccessTokenAsync() throws Exception {
        server.enqueue(new MockResponse().setBody("{\"access_token\":\"at123\"}"));
        final OAuth2AccessToken token = service.getAccessTokenAsync("code").get();
        assertThat(token.getAccessToken()).isEqualTo("at123");
    }

    @Test
    public void shouldRefreshAccessTokenAsync() throws Exception {
        server.enqueue(new MockResponse().setBody("{\"access_token\":\"at456\"}"));
        final OAuth2AccessToken token = service.refreshAccessTokenAsync("rt123").get();
        assertThat(token.getAccessToken()).isEqualTo("at456");
    }

    @Test
    public void shouldGetDeviceAuthorizationCodesAsync() throws Exception {
        server.enqueue(new MockResponse().setBody("{\"device_code\":\"dev123\", \"user_code\":\"user456\", "
                + "\"verification_uri\":\"http://v.com\", \"expires_in\":600}"));
        final DeviceAuthorization auth = service.getDeviceAuthorizationCodesAsync().get();
        assertThat(auth.getDeviceCode()).isEqualTo("dev123");
    }
}
