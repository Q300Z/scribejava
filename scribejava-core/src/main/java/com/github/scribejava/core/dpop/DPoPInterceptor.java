package com.github.scribejava.core.dpop;

import com.github.scribejava.core.model.OAuthConstants;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.oauth.OAuthRequestInterceptor;

/**
 * Interceptor that adds DPoP headers to requests.
 */
public class DPoPInterceptor implements OAuthRequestInterceptor {

    private final DPoPProofCreator proofCreator;
    private String accessToken; // Can be set if we want to include 'ath' claim

    public DPoPInterceptor(DPoPProofCreator proofCreator) {
        this.proofCreator = proofCreator;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public void intercept(OAuthRequest request) {
        request.setDPoPProof(proofCreator.createDPoPProof(request, accessToken));
    }
}
