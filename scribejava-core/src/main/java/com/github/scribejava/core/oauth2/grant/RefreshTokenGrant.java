package com.github.scribejava.core.oauth2.grant;

import com.github.scribejava.core.model.OAuthConstants;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.oauth.OAuth20Service;

public class RefreshTokenGrant implements OAuth20Grant {

    private final String refreshToken;
    private final String scope;

    public RefreshTokenGrant(String refreshToken) {
        this(refreshToken, null);
    }

    public RefreshTokenGrant(String refreshToken, String scope) {
        this.refreshToken = refreshToken;
        this.scope = scope;
    }

    @Override
    public OAuthRequest createRequest(OAuth20Service service) {
        final OAuthRequest request = new OAuthRequest(service.getApi().getAccessTokenVerb(),
                service.getApi().getAccessTokenEndpoint());

        service.getApi().getClientAuthentication().addClientAuthentication(request, service.getApiKey(),
                service.getApiSecret());

        request.addParameter(OAuthConstants.REFRESH_TOKEN, refreshToken);

        if (scope != null) {
            request.addParameter(OAuthConstants.SCOPE, scope);
        } else if (service.getDefaultScope() != null) {
            request.addParameter(OAuthConstants.SCOPE, service.getDefaultScope());
        }

        request.addParameter(OAuthConstants.GRANT_TYPE, OAuthConstants.REFRESH_TOKEN);

        return request;
    }
}
