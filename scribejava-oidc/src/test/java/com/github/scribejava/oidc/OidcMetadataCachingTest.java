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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;

/** Tests du cache des métadonnées OIDC. */
public class OidcMetadataCachingTest {

  /**
   * Vérifie que les métadonnées sont bien mises en cache.
   *
   * @throws Exception en cas d'erreur
   */
  @Test
  public void shouldCacheMetadata() throws Exception {
    final HttpClient httpClient = mock(HttpClient.class);
    final String issuer = "https://server.com";
    final String json =
        "{\"issuer\":\"https://server.com\","
            + "\"authorization_endpoint\":\"auth\","
            + "\"token_endpoint\":\"token\"}";

    final Response response = mock(Response.class);
    when(response.getCode()).thenReturn(200);
    when(response.getBody()).thenReturn(json);

    // Mock complexe pour simuler l'exécution asynchrone du handler
    doAnswer(
            invocation -> {
              final OAuthRequest.ResponseConverter<?> converter = invocation.getArgument(6);
              return CompletableFuture.completedFuture(converter.convert(response));
            })
        .when(httpClient)
        .executeAsync(any(), any(), eq(Verb.GET), any(), (byte[]) isNull(), any(), any());

    final OidcDiscoveryService service = new OidcDiscoveryService(issuer, httpClient, "ua");
    OidcDiscoveryService.clearCache();

    // Premier appel : réseau (simulé)
    final OidcProviderMetadata meta1 = service.getProviderMetadata();
    // Deuxième appel : cache
    final OidcProviderMetadata meta2 = service.getProviderMetadata();

    assertThat(meta1).isSameAs(meta2);
    verify(httpClient, times(1))
        .executeAsync(any(), any(), eq(Verb.GET), any(), (byte[]) isNull(), any(), any());
  }
}
