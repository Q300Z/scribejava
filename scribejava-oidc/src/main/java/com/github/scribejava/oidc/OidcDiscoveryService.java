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
import com.nimbusds.jose.jwk.JWKSet;
import java.io.IOException;
import java.text.ParseException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/** Service gérant la découverte (Discovery) OpenID Connect et la récupération des clés JWKS. */
public class OidcDiscoveryService implements com.github.scribejava.core.oauth.DiscoveryService {

  private static final String OIDC_DISCOVERY_PATH = "/.well-known/openid-configuration";

  private final HttpClient httpClient;
  private final String issuerUri;
  private final String userAgent;
  private boolean strictIssuerCheck = true;

  /**
   * Constructeur.
   *
   * @param issuerUri L'URI de l'émetteur (Issuer URI).
   * @param httpClient Le client HTTP à utiliser.
   * @param userAgent La chaîne User-Agent.
   */
  public OidcDiscoveryService(
      final String issuerUri, final HttpClient httpClient, final String userAgent) {
    if (issuerUri == null || issuerUri.isEmpty()) {
      throw new IllegalArgumentException("Issuer URI cannot be null or empty.");
    }
    if (httpClient == null) {
      throw new IllegalArgumentException("HttpClient cannot be null.");
    }
    this.issuerUri = issuerUri;
    this.httpClient = httpClient;
    this.userAgent = userAgent;
  }

  /**
   * Active ou désactive la vérification stricte de l'émetteur.
   *
   * @param strictIssuerCheck true pour activer la vérification.
   */
  public void setStrictIssuerCheck(boolean strictIssuerCheck) {
    this.strictIssuerCheck = strictIssuerCheck;
  }

  /** {@inheritDoc} */
  @Override
  public CompletableFuture<com.github.scribejava.core.oauth.DiscoveredEndpoints> discoverAsync(
      String issuer) {
    return getProviderMetadataAsync()
        .thenApply(
            metadata ->
                new com.github.scribejava.core.oauth.DiscoveredEndpoints(
                    metadata.getAuthorizationEndpoint(), metadata.getTokenEndpoint()));
  }

  /**
   * Récupère les métadonnées du fournisseur OIDC de manière asynchrone.
   *
   * @return Un {@link CompletableFuture} résolvant vers {@link OidcProviderMetadata}.
   * @see <a
   *     href="https://openid.net/specs/openid-connect-discovery-1_0.html#ProviderConfigurationResponse">OpenID
   *     Connect Discovery 1.0, Section 4.2</a>
   */
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
                  "Failed to fetch OIDC Provider Metadata from "
                      + discoveryEndpoint
                      + ". Status: "
                      + resp.getCode()
                      + ", Body: "
                      + resp.getBody());
            }
            final OidcProviderMetadata metadata = OidcProviderMetadata.parse(resp.getBody());

            if (strictIssuerCheck) {
              String expected = issuerUri.endsWith("/") ? issuerUri : issuerUri + "/";
              String got =
                  metadata.getIssuer().endsWith("/")
                      ? metadata.getIssuer()
                      : metadata.getIssuer() + "/";
              if (!expected.equals(got)) {
                throw new OAuthException(
                    "Issuer mismatch. Expected: " + issuerUri + ", Got: " + metadata.getIssuer());
              }
            }

            return metadata;
          } catch (final IOException e) {
            throw new OAuthException("Error parsing OIDC Provider Metadata response", e);
          }
        });
  }

  /**
   * Récupère les métadonnées du fournisseur OIDC de manière synchrone.
   *
   * @return Un objet {@link OidcProviderMetadata}.
   * @throws IOException en cas d'erreur réseau.
   * @throws ExecutionException si la tâche échoue.
   * @throws InterruptedException si le thread est interrompu.
   */
  public OidcProviderMetadata getProviderMetadata()
      throws IOException, ExecutionException, InterruptedException {
    return getProviderMetadataAsync().get();
  }

  /**
   * Récupère l'ensemble de clés JWKS de manière asynchrone.
   *
   * @param jwksUri L'URI du point de terminaison JWKS.
   * @return Un {@link CompletableFuture} résolvant vers {@link JWKSet}.
   * @see <a href="https://openid.net/specs/openid-connect-discovery-1_0.html#ProviderMetadata">RFC
   *     7517 (JWK)</a>
   */
  public CompletableFuture<JWKSet> getJwksAsync(final String jwksUri) {
    if (jwksUri == null || jwksUri.isEmpty()) {
      throw new IllegalArgumentException("JWKS URI cannot be null or empty.");
    }
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
              throw new OAuthException(
                  "Failed to fetch JWKS from "
                      + jwksUri
                      + ". Status: "
                      + resp.getCode()
                      + ", Body: "
                      + resp.getBody());
            }
            return JWKSet.parse(resp.getBody());
          } catch (final IOException | ParseException e) {
            throw new OAuthException("Error parsing JWKS response", e);
          }
        });
  }

  /**
   * Récupère l'ensemble de clés JWKS de manière synchrone.
   *
   * @param jwksUri L'URI du point de terminaison JWKS.
   * @return Un objet {@link JWKSet}.
   * @throws IOException en cas d'erreur réseau.
   * @throws ExecutionException si la tâche échoue.
   * @throws InterruptedException si le thread est interrompu.
   */
  public JWKSet getJwks(final String jwksUri)
      throws IOException, ExecutionException, InterruptedException {
    return getJwksAsync(jwksUri).get();
  }
}
