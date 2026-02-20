package com.github.scribejava.oauth1.oauth;

import com.github.scribejava.core.httpclient.jdk.JDKHttpClient;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
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

public class OAuth10aServiceFlowTest {

    private MockWebServer server;
    private OAuth10aService service;

    @BeforeEach
    public void setUp() throws IOException {
        server = new MockWebServer();
        server.start();

        final DefaultApi10a api = new DefaultApi10a() {
            @Override
            public String getRequestTokenEndpoint() {
                return server.url("/request_token").toString();
            }

            @Override
            public String getAccessTokenEndpoint() {
                return server.url("/access_token").toString();
            }

            @Override
            public String getAuthorizationBaseUrl() {
                return server.url("/auth").toString();
            }
        };

        service = new OAuth10aService(api, "api-key", "api-secret", "callback", null, null, null, null,
                new JDKHttpClient());
    }

    @AfterEach
    public void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    public void shouldGetRequestToken() throws Exception {
        server.enqueue(new MockResponse().setBody("oauth_token=request_token&oauth_token_secret=request_secret"));
        final OAuth1RequestToken token = service.getRequestToken();
        assertThat(token.getToken()).isEqualTo("request_token");
        assertThat(token.getTokenSecret()).isEqualTo("request_secret");
    }

    @Test
    public void shouldGetAccessToken() throws Exception {
        server.enqueue(new MockResponse().setBody("oauth_token=access_token&oauth_token_secret=access_secret"));
        final OAuth1RequestToken requestToken = new OAuth1RequestToken("req_token", "req_secret");
        final OAuth1AccessToken token = service.getAccessToken(requestToken, "verifier");
        assertThat(token.getToken()).isEqualTo("access_token");
        assertThat(token.getTokenSecret()).isEqualTo("access_secret");
    }

    @Test
    public void shouldSignRequest() {
        final OAuthRequest request = new OAuthRequest(Verb.GET, "http://example.com/api");
        service.signRequest(new OAuth1AccessToken("token", "secret"), request);
        assertThat(request.getHeaders()).containsKey("Authorization");
        assertThat(request.getHeaders().get("Authorization")).contains("oauth_signature");
    }

    @Test
    public void shouldPrepareAccessTokenRequest() {
        final OAuth1RequestToken requestToken = new OAuth1RequestToken("req_token", "req_secret");
        final OAuthRequest request = service.prepareAccessTokenRequest(requestToken, "verifier");
        assertThat(request.getOauthParameters()).containsKey("oauth_token");
        assertThat(request.getOauthParameters()).containsKey("oauth_verifier");
    }

    @Test
    public void shouldPrepareRequestTokenRequest() {
        final OAuthRequest request = service.prepareRequestTokenRequest();
        assertThat(request.getOauthParameters()).containsEntry("oauth_callback", "callback");
    }

    @Test
    public void shouldGetRequestTokenAsync() throws Exception {
        server.enqueue(new MockResponse().setBody("oauth_token=request_token&oauth_token_secret=request_secret"));
        final OAuth1RequestToken token = service.getRequestTokenAsync().get();
        assertThat(token.getToken()).isEqualTo("request_token");
    }

    @Test
    public void shouldGetAccessTokenAsync() throws Exception {
        server.enqueue(new MockResponse().setBody("oauth_token=access_token&oauth_token_secret=access_secret"));
        final OAuth1RequestToken requestToken = new OAuth1RequestToken("req_token", "req_secret");
        final OAuth1AccessToken token = service.getAccessTokenAsync(requestToken, "verifier").get();
        assertThat(token.getToken()).isEqualTo("access_token");
    }
}
