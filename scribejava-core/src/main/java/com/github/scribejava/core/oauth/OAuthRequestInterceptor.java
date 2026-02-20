package com.github.scribejava.core.oauth;

import com.github.scribejava.core.model.OAuthRequest;

/**
 * Interceptor that can modify an OAuthRequest before it's executed.
 */
public interface OAuthRequestInterceptor {
    void intercept(OAuthRequest request);
}
