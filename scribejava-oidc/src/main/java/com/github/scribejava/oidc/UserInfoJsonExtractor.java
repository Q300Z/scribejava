package com.github.scribejava.oidc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.core.model.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Extractor for OpenID Connect UserInfo response.
 */
public class UserInfoJsonExtractor {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    protected UserInfoJsonExtractor() {
    }

    private static class InstanceHolder {
        private static final UserInfoJsonExtractor INSTANCE = new UserInfoJsonExtractor();
    }

    public static UserInfoJsonExtractor instance() {
        return InstanceHolder.INSTANCE;
    }

    public StandardClaims extract(final Response response) throws IOException {
        final JsonNode node = OBJECT_MAPPER.readTree(response.getBody());
        final Map<String, Object> claimsMap = new HashMap<>();

        final Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            final Map.Entry<String, JsonNode> entry = fields.next();
            final JsonNode value = entry.getValue();
            if (value.isBoolean()) {
                claimsMap.put(entry.getKey(), value.asBoolean());
            } else if (value.isNumber()) {
                claimsMap.put(entry.getKey(), value.numberValue());
            } else {
                claimsMap.put(entry.getKey(), value.asText());
            }
        }
        return new StandardClaims(claimsMap);
    }
}
