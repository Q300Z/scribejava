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
package com.github.scribejava.oauth1;

import com.github.scribejava.core.httpclient.jdk.JDKHttpClient;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.oauth1.builder.api.DefaultApi10a;
import com.github.scribejava.oauth1.oauth.OAuth10aService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OAuth10aSignatureTest {

    @Test
    public void shouldHandleSpecialCharactersInSignature() {
        final DefaultApi10a api =
                new DefaultApi10a() {
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

        final OAuth10aService service =
                new OAuth10aService(
                        api, "api-key", "api-secret", "callback", null, null, null, null, new JDKHttpClient());

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
