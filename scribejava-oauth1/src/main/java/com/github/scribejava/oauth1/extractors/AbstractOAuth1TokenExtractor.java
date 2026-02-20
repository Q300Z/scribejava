package com.github.scribejava.oauth1.extractors;

import com.github.scribejava.core.extractors.TokenExtractor;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.utils.Preconditions;
import com.github.scribejava.oauth1.model.OAuth1Token;
import java.io.IOException;

/**
 * Base extractor for OAuth 1.0a tokens.
 *
 * @param <T> concrete type of token
 */
public abstract class AbstractOAuth1TokenExtractor<T extends OAuth1Token> implements TokenExtractor<T> {

    @Override
    public T extract(Response response) throws IOException {
        final String body = response.getBody();
        Preconditions.checkEmptyString(body, "Response body is incorrect. Can't extract a token from an empty string");
        return parse(body);
    }

    protected abstract T parse(String body);
}
