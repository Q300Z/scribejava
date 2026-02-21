package com.github.scribejava.core.oauth2.grant;

import com.github.scribejava.core.model.OAuthConstants;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.oauth.OAuth20Service;

public class PasswordGrant implements OAuth20Grant {

    private final String username;
    private final String password;
    private final String scope;

    public PasswordGrant(String username, String password) {
        this(username, password, null);
    }

    public PasswordGrant(String username, String password, String scope) {
        this.username = username;
        this.password = password;
        this.scope = scope;
    }

    @Override
    public OAuthRequest createRequest(OAuth20Service service) {
        final OAuthRequest request = new OAuthRequest(service.getApi().getAccessTokenVerb(),
                service.getApi().getAccessTokenEndpoint());

        request.addParameter(OAuthConstants.USERNAME, username);
        request.addParameter(OAuthConstants.PASSWORD, password);

        if (scope != null) {
            request.addParameter(OAuthConstants.SCOPE, scope);
        } else if (service.getDefaultScope() != null) {
            request.addParameter(OAuthConstants.SCOPE, service.getDefaultScope());
        }

        request.addParameter(OAuthConstants.GRANT_TYPE, OAuthConstants.PASSWORD);

        service.getApi().getClientAuthentication().addClientAuthentication(request, service.getApiKey(),
                service.getApiSecret());

        return request;
    }
}
