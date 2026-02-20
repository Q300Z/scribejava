package com.github.scribejava.oauth1.extractors;

import com.github.scribejava.oauth1.model.OAuth1AccessToken;
import com.github.scribejava.core.utils.OAuthEncoder;
import com.github.scribejava.core.exceptions.OAuthException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Default implementation of {@link com.github.scribejava.core.extractors.TokenExtractor} for OAuth 1.0a
 */
public class OAuth1AccessTokenExtractor extends AbstractOAuth1TokenExtractor<OAuth1AccessToken> {

    private static final Pattern TOKEN_PATTERN = Pattern.compile("oauth_token=([^&]+)");
    private static final Pattern SECRET_PATTERN = Pattern.compile("oauth_token_secret=([^&]+)");

    protected OAuth1AccessTokenExtractor() {
    }

    private static class InstanceHolder {
        private static final OAuth1AccessTokenExtractor INSTANCE = new OAuth1AccessTokenExtractor();
    }

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
            throw new OAuthException("Response body is incorrect. Can't extract token and secret from this: '"
                    + response + "'", null);
        }
    }
}
