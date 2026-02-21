package com.github.scribejava.core.oauth;

public class DiscoveredEndpoints {
    private final String authorizationEndpoint;
    private final String tokenEndpoint;

    public DiscoveredEndpoints(String authorizationEndpoint, String tokenEndpoint) {
        this.authorizationEndpoint = authorizationEndpoint;
        this.tokenEndpoint = tokenEndpoint;
    }

    public String getAuthorizationEndpoint() {
        return authorizationEndpoint;
    }

    public String getTokenEndpoint() {
        return tokenEndpoint;
    }
}
