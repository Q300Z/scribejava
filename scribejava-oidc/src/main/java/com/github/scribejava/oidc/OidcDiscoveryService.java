package com.github.scribejava.oidc;

import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.nimbusds.jose.jwk.JWKSet;

import java.io.IOException;
import java.text.ParseException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.CompletableFuture;

/**
 * Service to handle OpenID Connect Discovery and JWKS retrieval.
 * <p>
 * Implements the discovery mechanism defined in:
 * <ul>
 *   <li><b>OpenID Connect Discovery 1.0:</b> Section 4 (Obtaining OpenID Provider Configuration Information)</li>
 *   <li><b>RFC 8414:</b> OAuth 2.0 Authorization Server Metadata (Section 3)</li>
 *   <li><b>RFC 7517:</b> JSON Web Key (JWK) for JWKS retrieval via {@code jwks_uri}</li>
 * </ul>
 */
public class OidcDiscoveryService {

    private static final String OIDC_DISCOVERY_PATH = "/.well-known/openid-configuration";

    private final HttpClient httpClient;
    private final String issuerUri;
    private final String userAgent;

    public OidcDiscoveryService(final String issuerUri, final HttpClient httpClient, final String userAgent) {
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
     * Retrieves and parses the OpenID Provider Metadata.
     *
     * @return a CompletableFuture resolving to OidcProviderMetadata
     */
    public CompletableFuture<OidcProviderMetadata> getProviderMetadataAsync() {
        final String discoveryEndpoint = ensureTrailingSlash(issuerUri) + OIDC_DISCOVERY_PATH;
        final OAuthRequest request = new OAuthRequest(Verb.GET, discoveryEndpoint);

        return httpClient.executeAsync(userAgent, request.getHeaders(), request.getVerb(), request.getCompleteUrl(),
                (byte[]) null, null, response -> {
                    try (Response resp = response) {
                        if (resp.getCode() != 200) {
                            throw new OAuthException("Failed to fetch OIDC Provider Metadata from " + discoveryEndpoint
                                    + ". Status: " + resp.getCode() + ", Body: " + resp.getBody());
                        }
                        final OidcProviderMetadata metadata = OidcProviderMetadata.parse(resp.getBody());
                        if (!issuerUri.equals(metadata.getIssuer())
                                && !ensureTrailingSlash(issuerUri).equals(ensureTrailingSlash(metadata.getIssuer()))) {
                            throw new OAuthException("Issuer mismatch. Expected: " + issuerUri
                                    + ", Got: " + metadata.getIssuer());
                        }
                        return metadata;
                    } catch (final IOException e) {
                        throw new OAuthException("Error parsing OIDC Provider Metadata response", e);
                    }
                });
    }

    public OidcProviderMetadata getProviderMetadata() throws IOException, ExecutionException, InterruptedException {
        return getProviderMetadataAsync().get();
    }

    /**
     * Retrieves and parses the JWKS from the given URI.
     *
     * @param jwksUri the URI to fetch JWKS from
     * @return a CompletableFuture resolving to JWKSet
     */
    public CompletableFuture<JWKSet> getJwksAsync(final String jwksUri) {
        if (jwksUri == null || jwksUri.isEmpty()) {
            throw new IllegalArgumentException("JWKS URI cannot be null or empty.");
        }
        final OAuthRequest request = new OAuthRequest(Verb.GET, jwksUri);

        return httpClient.executeAsync(userAgent, request.getHeaders(), request.getVerb(), request.getCompleteUrl(),
                (byte[]) null, null, response -> {
                    try (Response resp = response) {
                        if (resp.getCode() != 200) {
                            throw new OAuthException("Failed to fetch JWKS from " + jwksUri + ". Status: "
                                    + resp.getCode() + ", Body: " + resp.getBody());
                        }
                        return JWKSet.parse(resp.getBody());
                    } catch (final IOException | ParseException e) {
                        throw new OAuthException("Error parsing JWKS response", e);
                    }
                });
    }

    public JWKSet getJwks(final String jwksUri) throws IOException, ExecutionException, InterruptedException {
        return getJwksAsync(jwksUri).get();
    }

    private String ensureTrailingSlash(final String uri) {
        if (!uri.endsWith("/")) {
            return uri + "/";
        }
        return uri;
    }
}
