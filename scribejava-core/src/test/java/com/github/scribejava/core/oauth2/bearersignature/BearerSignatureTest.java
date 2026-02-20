package com.github.scribejava.core.oauth2.bearersignature;

import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BearerSignatureTest {

    @Test
    public void shouldSignRequestWithHeader() {
        final BearerSignatureAuthorizationRequestHeaderField signature =
                BearerSignatureAuthorizationRequestHeaderField.instance();
        final OAuthRequest request = new OAuthRequest(Verb.GET, "http://example.com");
        signature.signRequest("at123", request);

        assertThat(request.getHeaders()).containsKey("Authorization");
        assertThat(request.getHeaders().get("Authorization")).isEqualTo("Bearer at123");
    }

    @Test
    public void shouldSignRequestWithQueryParameter() {
        final BearerSignatureURIQueryParameter signature = BearerSignatureURIQueryParameter.instance();
        final OAuthRequest request = new OAuthRequest(Verb.GET, "http://example.com");
        signature.signRequest("at123", request);

        assertThat(request.getQueryStringParams().asFormUrlEncodedString()).contains("access_token=at123");
    }
}
