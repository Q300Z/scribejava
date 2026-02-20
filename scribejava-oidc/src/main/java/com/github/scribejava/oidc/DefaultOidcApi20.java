package com.github.scribejava.oidc;

import com.github.scribejava.core.builder.api.DefaultApi20;

/**
 * Base class for OpenID Connect APIs.
 * <p>
 * This abstraction allows for dynamic discovery of endpoints via OIDC Discovery 1.0.
 */
public abstract class DefaultOidcApi20 extends DefaultApi20 {

    private OidcProviderMetadata metadata;

    /**
     * Gets the Issuer URL for this API.
     *
     * @return The issuer URL.
     */
    public abstract String getIssuer();

    public OidcProviderMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(final OidcProviderMetadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public String getAccessTokenEndpoint() {
        return metadata != null ? metadata.getTokenEndpoint() : null;
    }

    @Override
    public String getAuthorizationBaseUrl() {
        return metadata != null ? metadata.getAuthorizationEndpoint() : null;
    }

    @Override
    public String getRevokeTokenEndpoint() {
        return metadata != null ? metadata.getRevocationEndpoint() : super.getRevokeTokenEndpoint();
    }

    @Override
    public String getPushedAuthorizationRequestEndpoint() {
        return metadata != null ? metadata.getPushedAuthorizationRequestEndpoint()
                : super.getPushedAuthorizationRequestEndpoint();
    }

    /**
     * Gets the JWKS URI from metadata.
     *
     * @return The JWKS URI.
     */
    public String getJwksUri() {
        return metadata != null ? metadata.getJwksUri() : null;
    }

    /**
     * Gets the UserInfo Endpoint from metadata.
     *
     * @return The UserInfo endpoint URL.
     */
    public String getUserinfoEndpoint() {
        return metadata != null ? metadata.getUserinfoEndpoint() : null;
    }
}
