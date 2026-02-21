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
public abstract class AbstractOAuth1TokenExtractor<T extends OAuth1Token>
    implements TokenExtractor<T> {

  @Override
  public T extract(Response response) throws IOException {
    final String body = response.getBody();
    Preconditions.checkEmptyString(
        body, "Response body is incorrect. Can't extract a token from an empty string");
    return parse(body);
  }

  protected abstract T parse(String body);
}
