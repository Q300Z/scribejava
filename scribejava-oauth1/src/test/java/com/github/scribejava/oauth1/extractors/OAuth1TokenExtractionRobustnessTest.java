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
package com.github.scribejava.oauth1.extractors;

import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.model.Response;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OAuth1TokenExtractionRobustnessTest {

    @Test
    public void shouldThrowOnMissingToken() throws IOException {
        final OAuth1AccessTokenExtractor extractor = OAuth1AccessTokenExtractor.instance();
        final Response response = mock(Response.class);
        // oauth_token missing
        when(response.getBody()).thenReturn("oauth_token_secret=secret");

        assertThatThrownBy(() -> extractor.extract(response))
                .isInstanceOf(OAuthException.class)
                .hasMessageContaining("Can't extract token and secret");
    }

    @Test
    public void shouldThrowOnEmptyToken() throws IOException {
        final OAuth1AccessTokenExtractor extractor = OAuth1AccessTokenExtractor.instance();
        final Response response = mock(Response.class);
        // oauth_token present but empty
        when(response.getBody()).thenReturn("oauth_token=&oauth_token_secret=secret");

        assertThatThrownBy(() -> extractor.extract(response)).isInstanceOf(OAuthException.class);
    }

    @Test
    public void shouldThrowOnEmptyBody() throws IOException {
        final OAuth1AccessTokenExtractor extractor = OAuth1AccessTokenExtractor.instance();
        final Response response = mock(Response.class);
        when(response.getBody()).thenReturn("");

        assertThatThrownBy(() -> extractor.extract(response))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
