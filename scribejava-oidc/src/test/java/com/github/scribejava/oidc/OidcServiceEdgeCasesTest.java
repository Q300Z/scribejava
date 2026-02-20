package com.github.scribejava.oidc;

import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.httpclient.jdk.JDKHttpClient;
import com.github.scribejava.core.model.OAuth2AccessToken;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class OidcServiceEdgeCasesTest {

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
                server.url("/userinfo").toString(),
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
    public void shouldHandleNon200UserInfoResponse() {
        server.enqueue(new MockResponse().setResponseCode(401).setBody("Unauthorized"));
        assertThrows(ExecutionException.class, () -> service.getUserInfoAsync(new OAuth2AccessToken("token")).get());
    }

    @Test
    public void shouldHandleMalformedUserInfoJson() {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("not a json"));
        assertThrows(ExecutionException.class, () -> service.getUserInfoAsync(new OAuth2AccessToken("token")).get());
    }

    @Test
    public void shouldThrowExceptionIfUserInfoEndpointMissing() {
        final DefaultOidcApi20 api = new DefaultOidcApi20() {
            @Override
            public String getIssuer() {
                return "iss";
            }
        };
        // Metadata with NO userinfo_endpoint
        api.setMetadata(new OidcProviderMetadata("iss", "auth", "token", "jwks", null, null, null, null, null, null,
                null, null, null, null, null, null));

        final OidcService incompleteService = new OidcService(api, "id", "secret", "cb", null, "code", null, null,
                null, new JDKHttpClient(), null);
        assertThrows(OAuthException.class, () -> incompleteService.getUserInfoAsync(new OAuth2AccessToken("token")));
    }
}
