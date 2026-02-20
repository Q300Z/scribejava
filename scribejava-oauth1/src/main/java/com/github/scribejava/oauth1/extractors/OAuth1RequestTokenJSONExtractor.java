package com.github.scribejava.oauth1.extractors;

import com.github.scribejava.oauth1.model.OAuth1RequestToken;

public class OAuth1RequestTokenJSONExtractor extends AbstractOAuth1JSONTokenExtractor<OAuth1RequestToken> {

    protected OAuth1RequestTokenJSONExtractor() {
    }

    @Override
    protected OAuth1RequestToken createToken(String token, String secret, String response) {
        return new OAuth1RequestToken(token, secret, response);
    }

    private static class InstanceHolder {

        private static final OAuth1RequestTokenJSONExtractor INSTANCE = new OAuth1RequestTokenJSONExtractor();
    }

    public static OAuth1RequestTokenJSONExtractor instance() {
        return InstanceHolder.INSTANCE;
    }
}
