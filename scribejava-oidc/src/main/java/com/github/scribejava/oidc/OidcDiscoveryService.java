package com.github.scribejava.oidc;

import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.CompletableFuture;

/**
 * Service to handle OpenID Connect Discovery.
 * Fetches and parses the OIDC Provider Metadata from the discovery endpoint.
 */
public class OidcDiscoveryService {

    private static final String OIDC_DISCOVERY_PATH = "/.well-known/openid-configuration";

    private final HttpClient httpClient;
    private final String issuerUri;
    private final String userAgent;

    public OidcDiscoveryService(String issuerUri, HttpClient httpClient, String userAgent) {
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
     * @return a CompletableFuture resolving to OIDCProviderMetadata
     */
    public CompletableFuture<OIDCProviderMetadata> getProviderMetadataAsync() {
        final String discoveryEndpoint = ensureTrailingSlash(issuerUri) + OIDC_DISCOVERY_PATH;
        final OAuthRequest request = new OAuthRequest(Verb.GET, discoveryEndpoint);

        return httpClient.executeAsync(userAgent, request.getHeaders(), request.getVerb(), request.getCompleteUrl(),
                (byte[]) null, null, response -> {
                    try (Response resp = response) {
                        if (resp.getCode() != 200) {
                            throw new OAuthException("Failed to fetch OIDC Provider Metadata from " + discoveryEndpoint + ". Status: " + resp.getCode() + ", Body: " + resp.getBody());
                        }
                        return OIDCProviderMetadata.parse(resp.getBody());
                    } catch (IOException | ParseException e) {
                        throw new OAuthException("Error parsing OIDC Provider Metadata response", e);
                    }
                });
    }

    public OIDCProviderMetadata getProviderMetadata() throws IOException, ExecutionException, InterruptedException {
        return getProviderMetadataAsync().get();
    }

    private String ensureTrailingSlash(String uri) {
        if (!uri.endsWith("/")) {
            return uri + "/";
        }
        return uri;
    }
}
