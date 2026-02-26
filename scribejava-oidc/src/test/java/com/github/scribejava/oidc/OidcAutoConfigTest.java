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
package com.github.scribejava.oidc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.model.JsonBuilder;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.oauth.OAuth20Service;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;

/** Tests DX : Auto-configuration via OIDC Discovery. */
public class OidcAutoConfigTest {

  /**
   * Vérifie que le builder configure correctement les endpoints via JsonBuilder.
   *
   * @throws Exception en cas d'erreur
   */
  @Test
  public void shouldAutoConfigureFromDiscovery() throws Exception {
    final HttpClient httpClient = mock(HttpClient.class);
    final String issuer = "https://server.com";

    final String json =
        new JsonBuilder()
            .add("issuer", issuer)
            .add("authorization_endpoint", "https://server.com/auth")
            .add("token_endpoint", "https://server.com/token")
            .build();

    final Response response = mock(Response.class);
    when(response.getCode()).thenReturn(200);
    when(response.getBody()).thenReturn(json);

    // Mock Discovery robuste supportant thenApply()
    doAnswer(
            invocation -> {
              final OAuthRequest.ResponseConverter<?> converter = invocation.getArgument(6);
              final Object result = converter != null ? converter.convert(response) : response;
              return CompletableFuture.completedFuture(result);
            })
        .when(httpClient)
        .executeAsync(any(), any(), any(), any(), (byte[]) any(), any(), any());

    final OidcServiceBuilder builder = new OidcServiceBuilder("client-id");
    builder.baseOnDiscovery(issuer, httpClient, "ua");

    final DefaultOidcApi20 api =
        new DefaultOidcApi20() {
          @Override
          public String getAccessTokenEndpoint() {
            return null;
          }

          @Override
          public String getAuthorizationBaseUrl() {
            return null;
          }
        };

    final OAuth20Service service = builder.build(api);

    assertThat(service.getAuthorizationUrl()).contains("https://server.com/auth");
  }
}
