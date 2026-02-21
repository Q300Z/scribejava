package com.github.scribejava.core.oauth;

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.dpop.DPoPProofCreator;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;

/**
 * Responsible for signing OAuth 2.0 requests, including DPoP proof creation.
 */
public class OAuth20RequestSigner {

    private final DefaultApi20 api;
    private final DPoPProofCreator dpopProofCreator;

    public OAuth20RequestSigner(DefaultApi20 api, DPoPProofCreator dpopProofCreator) {
        this.api = api;
        this.dpopProofCreator = dpopProofCreator;
    }

    public void signRequest(String accessToken, OAuthRequest request) {
        if (dpopProofCreator != null) {
            request.setDPoPProof(dpopProofCreator.createDPoPProof(request, accessToken));
        }
        api.getBearerSignature().signRequest(accessToken, request);
    }

    public void signRequest(OAuth2AccessToken accessToken, OAuthRequest request) {
        signRequest(accessToken == null ? null : accessToken.getAccessToken(), request);
    }
}
