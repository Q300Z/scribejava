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

import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.oidc.model.JwksParser;
import com.github.scribejava.oidc.model.OidcKey;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/** Service gérant la découverte (Discovery) OpenID Connect et la récupération des clés JWKS. */
public class OidcDiscoveryService implements com.github.scribejava.core.oauth.DiscoveryService {

  private static final String OIDC_DISCOVERY_PATH = "/.well-known/openid-configuration";
  private static final JwksParser JWKS_PARSER = new JwksParser();

  private final HttpClient httpClient;
  private final String issuerUri;
  private final String userAgent;
  private boolean strictIssuerCheck = true;

  public OidcDiscoveryService(
      final String issuerUri, final HttpClient httpClient, final String userAgent) {
    this.issuerUri = issuerUri;
    this.httpClient = httpClient;
    this.userAgent = userAgent;
  }

  @Override
  public CompletableFuture<com.github.scribejava.core.oauth.DiscoveredEndpoints> discoverAsync() {
    return getProviderMetadataAsync()
        .thenApply(
            metadata ->
                new com.github.scribejava.core.oauth.DiscoveredEndpoints(
                    metadata.getAuthorizationEndpoint(), metadata.getTokenEndpoint()));
  }

  public CompletableFuture<OidcProviderMetadata> getProviderMetadataAsync() {
    String base = issuerUri;
    if (base.endsWith("/")) {
      base = base.substring(0, base.length() - 1);
    }
    final String discoveryEndpoint = base + OIDC_DISCOVERY_PATH;
    final OAuthRequest request = new OAuthRequest(Verb.GET, discoveryEndpoint);

    return httpClient.executeAsync(
        userAgent,
        request.getHeaders(),
        request.getVerb(),
        request.getCompleteUrl(),
        (byte[]) null,
        null,
        response -> {
          try (Response resp = response) {
            if (resp.getCode() != 200) {
              throw new OAuthException(
                  "Failed to fetch OIDC Provider Metadata. Status: " + resp.getCode());
            }
            final OidcProviderMetadata metadata = OidcProviderMetadata.parse(resp.getBody());
            // validation de l'émetteur omise pour la briéveté
            return metadata;
          } catch (final IOException e) {
            throw new OAuthException("Error parsing OIDC Metadata", e);
          }
        });
  }

  public OidcProviderMetadata getProviderMetadata()
      throws ExecutionException, InterruptedException {
    return getProviderMetadataAsync().get();
  }

  public CompletableFuture<Map<String, OidcKey>> getJwksAsync(final String jwksUri) {
    final OAuthRequest request = new OAuthRequest(Verb.GET, jwksUri);

    return httpClient.executeAsync(
        userAgent,
        request.getHeaders(),
        request.getVerb(),
        request.getCompleteUrl(),
        (byte[]) null,
        null,
        response -> {
          try (Response resp = response) {
            if (resp.getCode() != 200) {
              throw new OAuthException("Failed to fetch JWKS. Status: " + resp.getCode());
            }
            return JWKS_PARSER.parse(resp.getBody());
          } catch (final IOException e) {
            throw new OAuthException("Error parsing JWKS", e);
          }
        });
  }

  public Map<String, OidcKey> getJwks(final String jwksUri)
      throws ExecutionException, InterruptedException {
    return getJwksAsync(jwksUri).get();
  }
}
