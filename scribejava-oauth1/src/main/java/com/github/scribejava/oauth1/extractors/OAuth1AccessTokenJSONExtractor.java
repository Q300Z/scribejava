package com.github.scribejava.oauth1.extractors;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.scribejava.oauth1.model.OAuth1AccessToken;

import java.io.IOException;

public class OAuth1AccessTokenJSONExtractor extends AbstractOAuth1JSONTokenExtractor<OAuth1AccessToken> {

    protected OAuth1AccessTokenJSONExtractor() {
    }

    public static OAuth1AccessTokenJSONExtractor instance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    protected OAuth1AccessToken createToken(String body) throws IOException {
        final JsonNode node = OBJECT_MAPPER.readTree(body);
        return new OAuth1AccessToken(extractRequiredParameter(node, "oauth_token", body).asText(),
                extractRequiredParameter(node, "oauth_token_secret", body).asText(), body);
    }

    private static class InstanceHolder {
        private static final OAuth1AccessTokenJSONExtractor INSTANCE = new OAuth1AccessTokenJSONExtractor();
    }
}
