package com.github.scribejava.oidc;

import com.github.scribejava.core.model.Token;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.text.ParseException;
import java.util.Map;

/**
 * OpenID Connect ID Token.
 */
public class IdToken extends Token {

    private static final long serialVersionUID = 1L;
    private final String rawToken;
    private final JWTClaimsSet claimsSet;

    public IdToken(final String rawToken) {
        super(rawToken);
        this.rawToken = rawToken;
        try {
            this.claimsSet = SignedJWT.parse(rawToken).getJWTClaimsSet();
        } catch (final ParseException e) {
            throw new com.github.scribejava.core.exceptions.OAuthException("Failed to parse ID Token", e);
        }
    }

    @Override
    public String getRawResponse() {
        return rawToken;
    }

    public JWTClaimsSet getClaimsSet() {
        return claimsSet;
    }

    public String getSubject() {
        return claimsSet.getSubject();
    }

    public String getIssuer() {
        return claimsSet.getIssuer();
    }

    public String getNonce() {
        return (String) claimsSet.getClaim("nonce");
    }

    public StandardClaims getStandardClaims() {
        return new StandardClaims(claimsSet.getClaims());
    }

    public Object getClaim(final String name) {
        return claimsSet.getClaim(name);
    }

    public Map<String, Object> getClaims() {
        return claimsSet.getClaims();
    }
}
