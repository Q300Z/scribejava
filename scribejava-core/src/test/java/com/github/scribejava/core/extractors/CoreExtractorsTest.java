/*
 * The MIT License
 *
 * Copyright (c) 2010 Pablo Fernandez
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.scribejava.core.extractors;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CoreExtractorsTest {

    @Test
    public void shouldExtractOAuth2AccessTokenFromQuerystring() throws IOException {
        final String body =
                "access_token=at123&token_type=bearer&expires_in=3600&refresh_token=rt456&scope=all";
        final Response response = new Response(200, "OK", Collections.emptyMap(), body);
        final OAuth2AccessToken token = OAuth2AccessTokenExtractor.instance().extract(response);

        assertThat(token.getAccessToken()).isEqualTo("at123");
        assertThat(token.getTokenType()).isEqualTo("bearer");
        assertThat(token.getExpiresIn()).isEqualTo(3600);
        assertThat(token.getRefreshToken()).isEqualTo("rt456");
        assertThat(token.getScope()).isEqualTo("all");
    }

    @Test
    public void shouldHandleInvalidExpiresInInOAuth2Extractor() throws IOException {
        final String body = "access_token=at123&expires_in=not-a-number";
        final Response response = new Response(200, "OK", Collections.emptyMap(), body);
        final OAuth2AccessToken token = OAuth2AccessTokenExtractor.instance().extract(response);
        assertThat(token.getExpiresIn()).isNull();
    }

    @Test
    public void shouldExtractHeader() {
        final HeaderExtractorImpl extractor = new HeaderExtractorImpl();
        final OAuthRequest request = new OAuthRequest(Verb.GET, "http://example.com");
        request.addOAuthParameter("oauth_callback", "http://callback.com");
        request.setRealm("some-realm");

        final String header = extractor.extract(request);
        assertThat(header).startsWith("OAuth ");
        assertThat(header).contains("oauth_callback=\"http%3A%2F%2Fcallback.com\"");
        assertThat(header).contains("realm=\"some-realm\"");
    }

    @Test
    public void shouldThrowExceptionIfRequestHasNoOAuthParametersInHeaderExtractor() {
        final HeaderExtractorImpl extractor = new HeaderExtractorImpl();
        final OAuthRequest request = new OAuthRequest(Verb.GET, "http://example.com");
        assertThrows(Exception.class, () -> extractor.extract(request));
    }
}
