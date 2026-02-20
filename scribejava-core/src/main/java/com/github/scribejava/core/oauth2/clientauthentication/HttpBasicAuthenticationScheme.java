package com.github.scribejava.core.oauth2.clientauthentication;

import com.github.scribejava.core.model.OAuthConstants;
import com.github.scribejava.core.model.OAuthRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 2.3. Client Authentication<br>
 * 2.3.1. Client Password<br>
 * https://tools.ietf.org/html/rfc6749#section-2.3.1
 * <br>
 * НTTP Basic authentication scheme
 */
public class HttpBasicAuthenticationScheme implements ClientAuthentication {

    protected HttpBasicAuthenticationScheme() {
    }

    private static class InstanceHolder {

        private static final HttpBasicAuthenticationScheme INSTANCE = new HttpBasicAuthenticationScheme();
    }

    public static HttpBasicAuthenticationScheme instance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public void addClientAuthentication(OAuthRequest request, String apiKey, String apiSecret) {
        if (apiKey != null && apiSecret != null) {
            request.addHeader(OAuthConstants.HEADER, OAuthConstants.BASIC + ' '
                    + Base64.getEncoder().encodeToString(String.format("%s:%s", apiKey, apiSecret).getBytes(StandardCharsets.UTF_8)));
        }
    }

}
