package com.github.scribejava.oidc;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.httpclient.jdk.JDKHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class OidcUserInfoTest {

    private MockWebServer server;
    private OidcService service;

    @BeforeEach
    public void setUp() throws IOException {
        server = new MockWebServer();
        server.start();

        final OidcProviderMetadata metadata = new OidcProviderMetadata(
                server.url("/").toString(),
                server.url("/auth").toString(),
                server.url("/token").toString(),
                server.url("/jwks").toString(),
                null, null, null,
                server.url("/userinfo").toString(), // UserInfo Endpoint
                null, null, null, null, null, null, null, null
        );

        final DefaultOidcApi20 api = new DefaultOidcApi20() {
            @Override
            public String getIssuer() {
                return server.url("/").toString();
            }
        };
        api.setMetadata(metadata);

        service = new OidcService(api, "client-id", "secret", "callback", null, "code", null, null, null,
                new JDKHttpClient(), null);
    }

    @AfterEach
    public void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    public void shouldFetchUserInfo() throws Exception {
        final String userInfoJson = "{"
                + "\"sub\":\"12345\","
                + "\"name\":\"John Doe\","
                + "\"email\":\"john@doe.com\","
                + "\"email_verified\":true"
                + "}";

        server.enqueue(new MockResponse().setBody(userInfoJson).setResponseCode(200));

        final OAuth2AccessToken token = new OAuth2AccessToken("access-token-123");
        final StandardClaims claims = service.getUserInfoAsync(token).get();

        assertThat(claims.getSub()).contains("12345");
        assertThat(claims.getName()).contains("John Doe");
        assertThat(claims.getEmail()).contains("john@doe.com");
        assertThat(claims.isEmailVerified()).contains(true);
    }
}
