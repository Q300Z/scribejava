package com.github.scribejava.core.oauth2.grant;

import com.github.scribejava.core.model.OAuthConstants;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.pkce.PKCE;

import java.util.Map;
import java.util.HashMap;

public class AuthorizationCodeGrant implements OAuth20Grant {

    private final String code;
    private String pkceCodeVerifier;
    private final Map<String, String> extraParameters = new HashMap<>();

    public AuthorizationCodeGrant(String code) {
        this.code = code;
    }

    public void setPkceCodeVerifier(String pkceCodeVerifier) {
        this.pkceCodeVerifier = pkceCodeVerifier;
    }

    public void addExtraParameter(String name, String value) {
        extraParameters.put(name, value);
    }

    @Override
    public OAuthRequest createRequest(OAuth20Service service) {
        final OAuthRequest request = new OAuthRequest(service.getApi().getAccessTokenVerb(),
                service.getApi().getAccessTokenEndpoint());

        service.getApi().getClientAuthentication().addClientAuthentication(request, service.getApiKey(),
                service.getApiSecret());

        request.addParameter(OAuthConstants.CODE, code);
        final String callback = service.getCallback();
        if (callback != null) {
            request.addParameter(OAuthConstants.REDIRECT_URI, callback);
        }

        request.addParameter(OAuthConstants.GRANT_TYPE, OAuthConstants.AUTHORIZATION_CODE);

        if (pkceCodeVerifier != null) {
            request.addParameter(PKCE.PKCE_CODE_VERIFIER_PARAM, pkceCodeVerifier);
        }

        for (Map.Entry<String, String> entry : extraParameters.entrySet()) {
            request.addParameter(entry.getKey(), entry.getValue());
        }

        return request;
    }
}
