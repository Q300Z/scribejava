package com.github.scribejava.core.oauth2.grant;

import com.github.scribejava.core.model.OAuthConstants;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.oauth.OAuth20Service;

public class ClientCredentialsGrant implements OAuth20Grant {

    private final String scope;

    public ClientCredentialsGrant() {
        this(null);
    }

    public ClientCredentialsGrant(String scope) {
        this.scope = scope;
    }

    @Override
    public OAuthRequest createRequest(OAuth20Service service) {
        final OAuthRequest request = new OAuthRequest(service.getApi().getAccessTokenVerb(),
                service.getApi().getAccessTokenEndpoint());

        service.getApi().getClientAuthentication().addClientAuthentication(request, service.getApiKey(),
                service.getApiSecret());

        if (scope != null) {
            request.addParameter(OAuthConstants.SCOPE, scope);
        } else if (service.getDefaultScope() != null) {
            request.addParameter(OAuthConstants.SCOPE, service.getDefaultScope());
        }
        request.addParameter(OAuthConstants.GRANT_TYPE, OAuthConstants.CLIENT_CREDENTIALS);

        return request;
    }
}
