package com.github.scribejava.oauth1.services;

import com.github.scribejava.core.exceptions.OAuthSignatureException;
import com.github.scribejava.core.services.SignatureService;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

/**
 * A signature service that uses the RSA-SHA1 algorithm.
 */
public class RSASha1SignatureService implements SignatureService {

    private static final String METHOD = "RSA-SHA1";
    private static final String RSA_SHA1 = "SHA1withRSA";

    private final PrivateKey privateKey;

    public RSASha1SignatureService(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSignature(String baseString, String apiSecret, String tokenSecret) {
        try {
            final Signature signature = Signature.getInstance(RSA_SHA1);
            signature.initSign(privateKey);
            signature.update(baseString.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | RuntimeException e) {
            throw new OAuthSignatureException(baseString, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSignatureMethod() {
        return METHOD;
    }
}
