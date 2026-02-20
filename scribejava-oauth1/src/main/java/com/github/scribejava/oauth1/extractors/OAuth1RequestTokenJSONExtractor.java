package com.github.scribejava.oauth1.extractors;

import com.github.scribejava.oauth1.model.OAuth1RequestToken;
import java.io.IOException;
import com.fasterxml.jackson.databind.JsonNode;

public class OAuth1RequestTokenJSONExtractor extends AbstractOAuth1JSONTokenExtractor<OAuth1RequestToken> {

    protected OAuth1RequestTokenJSONExtractor() {
    }

    private static class InstanceHolder {
        private static final OAuth1RequestTokenJSONExtractor INSTANCE = new OAuth1RequestTokenJSONExtractor();
    }

    public static OAuth1RequestTokenJSONExtractor instance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    protected OAuth1RequestToken createToken(String body) throws IOException {
        final JsonNode node = OBJECT_MAPPER.readTree(body);
        return new OAuth1RequestToken(extractRequiredParameter(node, "oauth_token", body).asText(),
                extractRequiredParameter(node, "oauth_token_secret", body).asText(), body);
    }
}
