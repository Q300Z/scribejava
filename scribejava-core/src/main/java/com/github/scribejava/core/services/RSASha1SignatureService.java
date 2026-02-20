package com.github.scribejava.core.services;

import com.github.scribejava.core.base64.Base64;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import com.github.scribejava.core.exceptions.OAuthSignatureException;
// import java.io.UnsupportedEncodingException; // REMOVED IMPORT
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets; // ADDED IMPORT

/**
 * A signature service that uses the RSA-SHA1 algorithm.
 */
public class RSASha1SignatureService implements SignatureService {

    private static final String METHOD = "RSA-SHA1";
    private static final String RSA_SHA1 = "SHA1withRSA";
    // private static final String UTF8 = "UTF-8"; // REMOVED CONSTANT

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
            return Base64.encode(signature.sign());
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | RuntimeException e) { // REMOVED UnsupportedEncodingException
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
