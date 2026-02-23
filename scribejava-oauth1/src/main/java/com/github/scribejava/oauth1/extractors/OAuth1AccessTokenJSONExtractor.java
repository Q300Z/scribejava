/*
 * The MIT License
 *
 * Copyright (c) 2010 Pablo Fernandez
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.scribejava.oauth1.extractors;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.scribejava.oauth1.model.OAuth1AccessToken;

import java.io.IOException;

/**
 * Extracteur JSON pour les jetons d'accès OAuth 1.0a (Access Token).
 */
public class OAuth1AccessTokenJSONExtractor
        extends AbstractOAuth1JSONTokenExtractor<OAuth1AccessToken> {

    protected OAuth1AccessTokenJSONExtractor() {
    }

    /**
     * Retourne l'instance unique de l'extracteur.
     *
     * @return L'instance {@link OAuth1AccessTokenJSONExtractor}.
     */
    public static OAuth1AccessTokenJSONExtractor instance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    protected OAuth1AccessToken createToken(String body) throws IOException {
        final JsonNode node = OBJECT_MAPPER.readTree(body);
        return new OAuth1AccessToken(
                extractRequiredParameter(node, "oauth_token", body).asText(),
                extractRequiredParameter(node, "oauth_token_secret", body).asText(),
                body);
    }

    private static class InstanceHolder {
        private static final OAuth1AccessTokenJSONExtractor INSTANCE =
                new OAuth1AccessTokenJSONExtractor();
    }
}
