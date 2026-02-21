package com.github.scribejava.core.oauth2.grant;

import com.github.scribejava.core.model.OAuthConstants;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.oauth.OAuth20Service;

public class DeviceCodeGrant implements OAuth20Grant {

    private final String deviceCode;

    public DeviceCodeGrant(String deviceCode) {
        this.deviceCode = deviceCode;
    }

    @Override
    public OAuthRequest createRequest(OAuth20Service service) {
        final OAuthRequest request = new OAuthRequest(service.getApi().getAccessTokenVerb(),
                service.getApi().getAccessTokenEndpoint());
        request.addParameter(OAuthConstants.GRANT_TYPE, "urn:ietf:params:oauth:grant-type:device_code");
        request.addParameter("device_code", deviceCode);
        service.getApi().getClientAuthentication().addClientAuthentication(request, service.getApiKey(),
                service.getApiSecret());
        return request;
    }
}
