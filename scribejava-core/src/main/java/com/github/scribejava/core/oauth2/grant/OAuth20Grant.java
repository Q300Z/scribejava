package com.github.scribejava.core.oauth2.grant;

import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.oauth.OAuth20Service;

/**
 * Interface for OAuth 2.0 Grant types.
 * Strategy pattern to decouple grant logic from OAuth20Service.
 */
public interface OAuth20Grant {
    OAuthRequest createRequest(OAuth20Service service);
}
