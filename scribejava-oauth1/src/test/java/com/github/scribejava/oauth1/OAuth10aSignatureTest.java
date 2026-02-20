package com.github.scribejava.oauth1;

import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.oauth1.oauth.OAuth10aService;
import com.github.scribejava.oauth1.builder.api.DefaultApi10a;
import com.github.scribejava.core.httpclient.jdk.JDKHttpClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OAuth10aSignatureTest {

    @Test
    public void shouldHandleSpecialCharactersInSignature() {
        final DefaultApi10a api = new DefaultApi10a() {
            @Override
            public String getRequestTokenEndpoint() {
                return "http://example.com/request";
            }

            @Override
            public String getAccessTokenEndpoint() {
                return "http://example.com/access";
            }

            @Override
            public String getAuthorizationBaseUrl() {
                return "http://example.com/auth";
            }
        };

        final OAuth10aService service = new OAuth10aService(api, "api-key", "api-secret", "callback", null, null,
                null, null, new JDKHttpClient());

        final OAuthRequest request = new OAuthRequest(Verb.GET, "http://example.com/api");
        // String from hell: space, emoji, symbols, reserved characters
        final String specialValue = "hello world! ❤️ & = + / ? % #";
        request.addQuerystringParameter("p1", specialValue);

        service.signRequest("access-token", "access-secret", request);

        final String authHeader = request.getHeaders().get("Authorization");
        assertThat(authHeader).contains("oauth_signature");
        // Verify that the special value didn't break anything (indirectly)
        assertThat(authHeader).contains("oauth_consumer_key=\"api-key\"");
    }
}
