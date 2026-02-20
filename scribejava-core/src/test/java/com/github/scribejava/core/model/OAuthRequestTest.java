package com.github.scribejava.core.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OAuthRequestTest {

    private OAuthRequest request;

    @BeforeEach
    public void setUp() {
        request = new OAuthRequest(Verb.GET, "http://example.com");
    }

    @Test
    public void shouldAddOAuthParamters() {
        request.addOAuthParameter(OAuthConstants.TOKEN, "token");
        request.addOAuthParameter(OAuthConstants.NONCE, "nonce");
        request.addOAuthParameter(OAuthConstants.TIMESTAMP, "ts");
        request.addOAuthParameter(OAuthConstants.SCOPE, "feeds");
        request.addOAuthParameter(OAuthConstants.REALM, "some-realm");

        assertThat(request.getOauthParameters()).hasSize(5);
    }

    @Test
    public void shouldThrowExceptionIfParameterIsNotOAuth() {
        assertThatThrownBy(() -> request.addOAuthParameter("otherParam", "value"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldNotSentHeaderTwice() {
        assertThat(request.getHeaders()).isEmpty();
        request.addHeader("HEADER-NAME", "first");
        request.addHeader("header-name", "middle");
        request.addHeader("Header-Name", "last");

        assertThat(request.getHeaders()).hasSize(1);

        assertThat(request.getHeaders()).containsKey("HEADER-NAME");
        assertThat(request.getHeaders()).containsKey("header-name");
        assertThat(request.getHeaders()).containsKey("Header-Name");

        assertThat(request.getHeaders().get("HEADER-NAME")).isEqualTo("last");
        assertThat(request.getHeaders().get("header-name")).isEqualTo("last");
        assertThat(request.getHeaders().get("Header-Name")).isEqualTo("last");
    }
}
