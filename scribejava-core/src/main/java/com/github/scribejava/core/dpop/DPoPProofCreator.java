package com.github.scribejava.core.dpop;

import com.github.scribejava.core.model.OAuthRequest;

/**
 * Interface for creating DPoP proof JWTs.
 */
public interface DPoPProofCreator {

    /**
     * Creates a DPoP proof JWT for a given HTTP request.
     *
     * @param request request
     * @param accessToken accessToken
     * @return DPoP proof
     */
    String createDPoPProof(OAuthRequest request, String accessToken);
}
