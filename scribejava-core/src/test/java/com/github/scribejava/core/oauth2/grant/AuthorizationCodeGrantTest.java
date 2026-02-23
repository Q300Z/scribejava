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
package com.github.scribejava.core.oauth2.grant;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.oauth.OAuth20Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AuthorizationCodeGrantTest {

  private OAuth20Service service;

  @BeforeEach
  public void setUp() {
    final DefaultApi20 api =
        new DefaultApi20() {
          @Override
          public String getAccessTokenEndpoint() {
            return "http://test.com/token";
          }

          @Override
          public String getAuthorizationBaseUrl() {
            return "http://test.com/auth";
          }
        };
    service =
        new OAuth20Service(
            api, "api-key", "api-secret", "callback", null, null, null, null, null, null);
  }

  @Test
  public void shouldCreateAuthorizationCodeRequest() {
    final AuthorizationCodeGrant grant = new AuthorizationCodeGrant("code123");
    grant.setPkceCodeVerifier("verifier");

    final OAuthRequest request = grant.createRequest(service);

    assertThat(request.getBodyParams().asFormUrlEncodedString()).contains("code=code123");
    assertThat(request.getBodyParams().asFormUrlEncodedString())
        .contains("grant_type=authorization_code");
    assertThat(request.getBodyParams().asFormUrlEncodedString()).contains("code_verifier=verifier");
  }
}
