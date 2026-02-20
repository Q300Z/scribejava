package com.github.scribejava.oidc;

import com.github.scribejava.core.httpclient.jdk.JDKHttpClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OidcLogoutTest {

    @Test
    public void shouldBuildRpInitiatedLogoutUrl() {
        final OidcService service = new OidcService(null, "client-id", "secret", "callback", null, "code", null, null,
                null, new JDKHttpClient(), null);

        final String endSessionEndpoint = "https://server.example.com/logout";
        final String idTokenHint = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJhbGljZSJ9.abc";
        final String postLogoutRedirectUri = "https://client.example.com/post-logout";
        final String state = "mystate";

        final String logoutUrl = service.getRpInitiatedLogoutUrl(endSessionEndpoint, idTokenHint, postLogoutRedirectUri,
                state);

        assertThat(logoutUrl).startsWith(endSessionEndpoint);
        assertThat(logoutUrl).contains("id_token_hint=" + idTokenHint);
        assertThat(logoutUrl).contains("post_logout_redirect_uri=https%3A%2F%2Fclient.example.com%2Fpost-logout");
        assertThat(logoutUrl).contains("state=" + state);
    }
}
