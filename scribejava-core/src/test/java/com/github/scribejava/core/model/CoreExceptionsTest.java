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
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.scribejava.core.oauth2.OAuth2Error;
import java.io.IOException;
import java.util.Collections;
import org.junit.jupiter.api.Test;

public class CoreExceptionsTest {

  @Test
  public void shouldTestOAuthResponseException() throws IOException {
    final Response response = new Response(401, "Unauthorized", Collections.emptyMap(), "body");
    final OAuthResponseException ex = new OAuthResponseException(response);
    assertThat(ex.getResponse()).isSameAs(response);
  }

  @Test
  public void shouldTestOAuthResponseExceptionJsonError() throws IOException {
    final String errorJson = "{\"error\":\"invalid_request\",\"error_description\":\"bad parameters\"}";
    final Response response = new Response(400, "Bad Request", Collections.emptyMap(), errorJson);
    final OAuthResponseException ex = new OAuthResponseException(response);

    assertThat(ex.getErrorDetails()).isPresent();
    assertThat(ex.getErrorDetails().get().getString("error")).isEqualTo("invalid_request");
    assertThat(ex.getOAuth2Error()).isPresent().contains(OAuth2Error.INVALID_REQUEST);
  }

  @Test
  public void shouldTestOAuthResponseExceptionNonJsonError() throws IOException {
    final Response response = new Response(400, "Bad Request", Collections.emptyMap(), "not-a-json-body");
    final OAuthResponseException ex = new OAuthResponseException(response);

    assertThat(ex.getErrorDetails()).isEmpty();
    assertThat(ex.getOAuth2Error()).isEmpty();
  }

  @Test
  public void shouldTestOAuthResponseExceptionEmptyBody() throws IOException {
    final Response response = new Response(400, "Bad Request", Collections.emptyMap(), (String) null);
    final OAuthResponseException ex = new OAuthResponseException(response);

    assertThat(ex.getErrorDetails()).isEmpty();
  }

  @Test
  public void shouldTestOAuthResponseExceptionMalformedJson() throws IOException {
    final Response response = new Response(400, "Bad Request", Collections.emptyMap(), "{ malformed json");
    final OAuthResponseException ex = new OAuthResponseException(response);

    assertThat(ex.getErrorDetails()).isPresent();
    assertThat(ex.getErrorDetails().get().asMap().isEmpty()).isTrue();
  }

  @Test
  public void shouldTestOAuthResponseExceptionEqualsAndHashCode() throws IOException {
    final Response response1 = new Response(401, "Unauthorized", Collections.emptyMap(), "body1");
    final Response response2 = new Response(401, "Unauthorized", Collections.emptyMap(), "body2");

    final OAuthResponseException ex1 = new OAuthResponseException(response1);
    final OAuthResponseException ex1Same = new OAuthResponseException(response1);
    final OAuthResponseException ex2 = new OAuthResponseException(response2);

    assertThat(ex1).isEqualTo(ex1);
    assertThat(ex1).isEqualTo(ex1Same);
    assertThat(ex1).isNotEqualTo(null);
    assertThat(ex1).isNotEqualTo("some string");
    assertThat(ex1).isNotEqualTo(ex2);

    assertThat(ex1.hashCode()).isEqualTo(ex1Same.hashCode());
    assertThat(ex1.hashCode()).isNotEqualTo(ex2.hashCode());
  }

  @Test
  public void shouldParseAllOAuth2Errors() {
    assertThat(OAuth2Error.parseFrom("invalid_request")).isEqualTo(OAuth2Error.INVALID_REQUEST);
    assertThat(OAuth2Error.parseFrom("unauthorized_client"))
        .isEqualTo(OAuth2Error.UNAUTHORIZED_CLIENT);
    assertThat(OAuth2Error.parseFrom("access_denied")).isEqualTo(OAuth2Error.ACCESS_DENIED);
    assertThat(OAuth2Error.parseFrom("unsupported_response_type"))
        .isEqualTo(OAuth2Error.UNSUPPORTED_RESPONSE_TYPE);
    assertThat(OAuth2Error.parseFrom("invalid_scope")).isEqualTo(OAuth2Error.INVALID_SCOPE);
    assertThat(OAuth2Error.parseFrom("server_error")).isEqualTo(OAuth2Error.SERVER_ERROR);
    assertThat(OAuth2Error.parseFrom("temporarily_unavailable"))
        .isEqualTo(OAuth2Error.TEMPORARILY_UNAVAILABLE);

    assertThrows(IllegalArgumentException.class, () -> OAuth2Error.parseFrom("unknown"));
  }
}
