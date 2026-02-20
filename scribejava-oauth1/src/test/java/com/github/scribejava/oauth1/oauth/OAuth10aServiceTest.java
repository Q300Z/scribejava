package com.github.scribejava.oauth1.oauth;

import com.github.scribejava.core.httpclient.jdk.JDKHttpClient;
import com.github.scribejava.oauth1.builder.api.DefaultApi10a;
import com.github.scribejava.oauth1.model.OAuth1AccessToken;
import com.github.scribejava.oauth1.model.OAuth1RequestToken;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class OAuth10aServiceTest {

    private MockWebServer server;
    private OAuth10aService service;

    @BeforeEach
    public void setUp() throws IOException {
        server = new MockWebServer();
        server.start();

        final DefaultApi10a api = new DefaultApi10a() {
            @Override
            public String getRequestTokenEndpoint() {
                return server.url("/request").toString();
            }

            @Override
            public String getAccessTokenEndpoint() {
                return server.url("/access").toString();
            }

            @Override
            public String getAuthorizationUrl(final OAuth1RequestToken requestToken) {
                return "https://auth";
            }

            @Override
            public String getAuthorizationBaseUrl() {
                return "https://auth";
            }
        };

        service = new OAuth10aService(api, "key", "secret", "cb", null, null, null, null, new JDKHttpClient());
    }

    @AfterEach
    public void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    public void shouldGetRequestTokenAsync() throws Exception {
        server.enqueue(new MockResponse().setBody("oauth_token=rt123&oauth_token_secret=rs123").setResponseCode(200));
        final OAuth1RequestToken token = service.getRequestTokenAsync().get();
        assertThat(token.getToken()).isEqualTo("rt123");
        assertThat(token.getTokenSecret()).isEqualTo("rs123");
    }

    @Test
    public void shouldGetAccessTokenAsync() throws Exception {
        server.enqueue(new MockResponse().setBody("oauth_token=at123&oauth_token_secret=as123").setResponseCode(200));
        final OAuth1AccessToken token = service.getAccessTokenAsync(new OAuth1RequestToken("rt", "rs"), "verifier")
                .get();
        assertThat(token.getToken()).isEqualTo("at123");
        assertThat(token.getTokenSecret()).isEqualTo("as123");
    }
}
