package com.github.scribejava.core.oauth;

import com.github.scribejava.core.pkce.PKCE;
import com.github.scribejava.core.pkce.PKCEService;
import java.util.HashMap;
import java.util.Map;

public class AuthorizationUrlBuilder {

    private final OAuth20Service oauth20Service;

    private String state;
    private Map<String, String> additionalParams;
    private PKCE pkce;
    private String scope;

    public AuthorizationUrlBuilder(OAuth20Service oauth20Service) {
        this.oauth20Service = oauth20Service;
    }

    public AuthorizationUrlBuilder state(String state) {
        this.state = state;
        return this;
    }

    public AuthorizationUrlBuilder additionalParams(Map<String, String> additionalParams) {
        this.additionalParams = additionalParams;
        return this;
    }

    public AuthorizationUrlBuilder pkce(PKCE pkce) {
        this.pkce = pkce;
        return this;
    }

    public AuthorizationUrlBuilder initPKCE() {
        this.pkce = PKCEService.defaultInstance().generatePKCE();
        return this;
    }

    public AuthorizationUrlBuilder scope(String scope) {
        this.scope = scope;
        return this;
    }

    public PKCE getPkce() {
        return pkce;
    }

    public String build() {
        if (pkce == null) {
            initPKCE(); // Initialize PKCE by default if not set
        }

        final Map<String, String> params;
        if (additionalParams == null) {
            params = new HashMap<>();
        } else {
            params = new HashMap<>(additionalParams);
        }

        if (pkce != null) { // PKCE parameters are always added if PKCE is present
            params.putAll(pkce.getAuthorizationUrlParams());
        }
        
        return oauth20Service.getApi().getAuthorizationUrl(oauth20Service.getResponseType(), oauth20Service.getApiKey(),
                oauth20Service.getCallback(), scope == null ? oauth20Service.getDefaultScope() : scope, state, params);
    }
}
