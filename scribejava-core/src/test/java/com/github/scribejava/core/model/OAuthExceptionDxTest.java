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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** Tests DX : Accès simplifié aux détails d'erreur JSON. */
public class OAuthExceptionDxTest {

  /**
   * Vérifie que les détails d'erreur sont accessibles via JsonObject.
   *
   * @throws IOException en cas d'erreur
   */
  @Test
  public void shouldProvideErrorDetailsAsJson() throws IOException {
    final Response response = mock(Response.class);
    final String errorJson =
        "{\"error\":\"invalid_grant\", \"error_description\":\"expired code\"}";
    when(response.getBody()).thenReturn(errorJson);

    final OAuthResponseException ex = new OAuthResponseException(response);

    final Optional<JsonObject> details = ex.getErrorDetails();
    assertThat(details).isPresent();
    assertThat(details.get().getString("error")).isEqualTo("invalid_grant");
    assertThat(details.get().getString("error_description")).isEqualTo("expired code");
  }

  /**
   * Vérifie le comportement si le corps n'est pas du JSON.
   *
   * @throws IOException en cas d'erreur
   */
  @Test
  public void shouldHandleNonJsonErrorBody() throws IOException {
    final Response response = mock(Response.class);
    when(response.getBody()).thenReturn("<html>Not JSON</html>");

    final OAuthResponseException ex = new OAuthResponseException(response);

    assertThat(ex.getErrorDetails()).isEmpty();
  }
}
