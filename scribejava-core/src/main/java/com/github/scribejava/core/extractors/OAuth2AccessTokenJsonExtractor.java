package com.github.scribejava.core.extractors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuth2AccessTokenErrorResponse;
import com.github.scribejava.core.model.OAuthConstants;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.oauth2.OAuth2Error;
import com.github.scribejava.core.utils.Preconditions;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

/**
 * JSON (default) implementation of {@link TokenExtractor} for OAuth 2.0
 */
public class OAuth2AccessTokenJsonExtractor extends AbstractJsonExtractor implements TokenExtractor<OAuth2AccessToken> {

    protected OAuth2AccessTokenJsonExtractor() {
    }

    public static OAuth2AccessTokenJsonExtractor instance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public OAuth2AccessToken extract(Response response) throws IOException {
        final String body = response.getBody();
        Preconditions.checkEmptyString(body, "Response body is incorrect. Can't extract a token from an empty string");

        if (response.getCode() != 200) {
            generateError(response);
        }
        return createToken(body);
    }

    public void generateError(Response response) throws IOException {
        final String responseBody = response.getBody();
        final JsonNode responseBodyJson;
        try {
            responseBodyJson = OBJECT_MAPPER.readTree(responseBody);
        } catch (JsonProcessingException ex) {
            throw new OAuth2AccessTokenErrorResponse(null, null, null, response);
        }

        final URI errorUri = Optional.ofNullable(responseBodyJson.get("error_uri"))
                .map(JsonNode::asText)
                .map(uri -> {
                    try {
                        return URI.create(uri);
                    } catch (IllegalArgumentException iae) {
                        return null;
                    }
                })
                .orElse(null);

        final OAuth2Error errorCode = Optional.ofNullable(responseBodyJson.get("error"))
                .map(JsonNode::asText)
                .map(error -> {
                    try {
                        return OAuth2Error.parseFrom(error);
                    } catch (IllegalArgumentException iaE) {
                        return null;
                    }
                })
                .orElse(null);

        final String errorDescription = Optional.ofNullable(responseBodyJson.get("error_description"))
                .map(JsonNode::asText)
                .orElse(null);

        throw new OAuth2AccessTokenErrorResponse(errorCode, errorDescription, errorUri, response);
    }

    private OAuth2AccessToken createToken(String rawResponse) throws IOException {
        final JsonNode response = OBJECT_MAPPER.readTree(rawResponse);

        final Integer expiresIn = Optional.ofNullable(response.get("expires_in")).map(JsonNode::asInt).orElse(null);
        final String refreshToken = Optional.ofNullable(response.get(OAuthConstants.REFRESH_TOKEN))
                .map(JsonNode::asText).orElse(null);
        final String scope = Optional.ofNullable(response.get(OAuthConstants.SCOPE)).map(JsonNode::asText).orElse(null);
        final String tokenType = Optional.ofNullable(response.get("token_type")).map(JsonNode::asText).orElse(null);
        final String accessToken = extractRequiredParameter(response, OAuthConstants.ACCESS_TOKEN, rawResponse)
                .asText();

        return createToken(accessToken, tokenType, expiresIn, refreshToken, scope, response, rawResponse);
    }

    protected OAuth2AccessToken createToken(String accessToken, String tokenType, Integer expiresIn,
                                            String refreshToken, String scope, JsonNode response, String rawResponse) {
        return new OAuth2AccessToken(accessToken, tokenType, expiresIn, refreshToken, scope, rawResponse);
    }

    private static class InstanceHolder {

        private static final OAuth2AccessTokenJsonExtractor INSTANCE = new OAuth2AccessTokenJsonExtractor();
    }
}
