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
