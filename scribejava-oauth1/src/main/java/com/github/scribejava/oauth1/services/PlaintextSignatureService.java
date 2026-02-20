package com.github.scribejava.oauth1.services;

import com.github.scribejava.core.services.SignatureService;
import com.github.scribejava.core.utils.OAuthEncoder;
import com.github.scribejava.core.utils.Preconditions;

/**
 * PLAINTEXT implementation of {@link SignatureService}
 */
public class PlaintextSignatureService implements SignatureService {

    private static final String METHOD = "PLAINTEXT";

    @Override
    public String getSignature(String baseString, String apiSecret, String tokenSecret) {
        Preconditions.checkNotNull(apiSecret, "Api secret can't be null");
        return OAuthEncoder.encode(apiSecret) + '&' + OAuthEncoder.encode(tokenSecret == null ? "" : tokenSecret);
    }

    @Override
    public String getSignatureMethod() {
        return METHOD;
    }
}
