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

import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.utils.OAuthEncoder;
import com.github.scribejava.oauth1.model.OAuth1AccessToken;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implémentation par défaut de l'extracteur de jeton d'accès pour OAuth 1.0a.
 *
 * <p>Analyse le corps de la réponse pour en extraire {@code oauth_token} et {@code
 * oauth_token_secret}.
 */
public class OAuth1AccessTokenExtractor extends AbstractOAuth1TokenExtractor<OAuth1AccessToken> {

    private static final Pattern TOKEN_PATTERN = Pattern.compile("oauth_token=([^&]+)");
    private static final Pattern SECRET_PATTERN = Pattern.compile("oauth_token_secret=([^&]+)");

    /**
     * Constructeur protégé.
     */
    protected OAuth1AccessTokenExtractor() {
    }

    /**
     * Retourne l'instance unique (singleton) de l'extracteur.
     *
     * @return L'instance de {@link OAuth1AccessTokenExtractor}.
     */
    public static OAuth1AccessTokenExtractor instance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    protected OAuth1AccessToken parse(final String body) {
        return new OAuth1AccessToken(extract(body, TOKEN_PATTERN), extract(body, SECRET_PATTERN), body);
    }

    private String extract(final String response, final Pattern pattern) {
        final Matcher matcher = pattern.matcher(response);
        if (matcher.find() && matcher.groupCount() >= 1) {
            return OAuthEncoder.decode(matcher.group(1));
        } else {
            throw new OAuthException(
                    "Response body is incorrect. Can't extract token and secret from this: '"
                            + response
                            + "'",
                    null);
        }
    }

    private static class InstanceHolder {
        private static final OAuth1AccessTokenExtractor INSTANCE = new OAuth1AccessTokenExtractor();
    }
}
