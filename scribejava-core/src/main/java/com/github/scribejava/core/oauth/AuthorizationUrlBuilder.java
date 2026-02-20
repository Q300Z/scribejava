package com.github.scribejava.core.oauth;

import com.github.scribejava.core.pkce.PKCE;
import com.github.scribejava.core.pkce.PKCEService;
import com.github.scribejava.core.model.OAuthConstants;
import com.github.scribejava.core.model.ParameterList;
import com.github.scribejava.core.model.PushedAuthorizationResponse;
import com.github.scribejava.core.exceptions.OAuthException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class AuthorizationUrlBuilder {

    private final OAuth20Service oauth20Service;

    private String state;
    private Map<String, String> additionalParams;
    private PKCE pkce;
    private String scope;
    private boolean usePushedAuthorizationRequests;

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

    public AuthorizationUrlBuilder usePushedAuthorizationRequests() {
        this.usePushedAuthorizationRequests = true;
        return this;
    }

    public PKCE getPkce() {
        return pkce;
    }

    public String build() {
        if (pkce == null) {
            initPKCE();
        }

        final Map<String, String> authorizationParams = additionalParams == null ? new HashMap<>() : new HashMap<>(additionalParams);

        if (pkce != null) {
            authorizationParams.putAll(pkce.getAuthorizationUrlParams());
        }

        if (usePushedAuthorizationRequests) {
            try {
                final PushedAuthorizationResponse parResponse = oauth20Service.pushAuthorizationRequestAsync(
                        oauth20Service.getResponseType(),
                        oauth20Service.getApiKey(),
                        oauth20Service.getCallback(),
                        scope == null ? oauth20Service.getDefaultScope() : scope,
                        state,
                        authorizationParams
                ).get();
                
                final ParameterList parameters = new ParameterList();
                parameters.add("request_uri", parResponse.getRequestUri());
                parameters.add(OAuthConstants.CLIENT_ID, oauth20Service.getApiKey());
                if (state != null) {
                    parameters.add(OAuthConstants.STATE, state);
                }
                return parameters.appendTo(oauth20Service.getApi().getAuthorizationBaseUrl());

            } catch (InterruptedException | ExecutionException e) {
                throw new OAuthException("Failed to push authorization request", e);
            }
        } else {
            return oauth20Service.getApi().getAuthorizationUrl(oauth20Service.getResponseType(), oauth20Service.getApiKey(),
                    oauth20Service.getCallback(), scope == null ? oauth20Service.getDefaultScope() : scope, state, authorizationParams);
        }
    }
}
