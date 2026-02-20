package com.github.scribejava.oauth1.extractors;

import com.github.scribejava.oauth1.model.OAuth1RequestToken;

public class OAuth1RequestTokenExtractor extends AbstractOAuth1TokenExtractor<OAuth1RequestToken> {

    protected OAuth1RequestTokenExtractor() {
    }

    private static class InstanceHolder {

        private static final OAuth1RequestTokenExtractor INSTANCE = new OAuth1RequestTokenExtractor();
    }

    public static OAuth1RequestTokenExtractor instance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    protected OAuth1RequestToken createToken(String token, String secret, String response) {
        return new OAuth1RequestToken(token, secret, response);
    }
}
