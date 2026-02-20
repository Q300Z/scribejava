package com.github.scribejava.core.pkce;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public enum PKCECodeChallengeMethod {
    S256 {
        @Override
        public String transform2CodeChallenge(String codeVerifier) throws NoSuchAlgorithmException {
            return Base64.getUrlEncoder().withoutPadding().encodeToString(
                    MessageDigest.getInstance("SHA-256").digest(codeVerifier.getBytes(StandardCharsets.US_ASCII)));
        }
    },
    PLAIN {
        @Override
        public String transform2CodeChallenge(String codeVerifier) {
            return codeVerifier;
        }
    };

    public abstract String transform2CodeChallenge(String codeVerifier) throws NoSuchAlgorithmException;
}
