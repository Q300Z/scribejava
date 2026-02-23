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
package com.github.scribejava.core.extractors;

import com.github.scribejava.core.exceptions.OAuthParametersMissingException;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.ParameterList;
import com.github.scribejava.core.utils.OAuthEncoder;
import com.github.scribejava.core.utils.Preconditions;

/**
 * Default implementation of {@link BaseStringExtractor}. Conforms to OAuth 1.0a
 * https://tools.ietf.org/html/rfc5849#section-3.4.1.1
 */
public class BaseStringExtractorImpl implements BaseStringExtractor {

    protected static final String AMPERSAND_SEPARATED_STRING = "%s&%s&%s";

    @Override
    public String extract(OAuthRequest request) {
        checkPreconditions(request);
        final String verb = OAuthEncoder.encode(getVerb(request));
        final String url = OAuthEncoder.encode(getUrl(request));
        final String params = getSortedAndEncodedParams(request);
        return String.format(AMPERSAND_SEPARATED_STRING, verb, url, params);
    }

    protected String getVerb(OAuthRequest request) {
        return request.getVerb().name();
    }

    protected String getUrl(OAuthRequest request) {
        return request.getSanitizedUrl();
    }

    protected String getSortedAndEncodedParams(OAuthRequest request) {
        final ParameterList params = new ParameterList();
        params.addAll(request.getQueryStringParams());
        params.addAll(request.getBodyParams());
        params.addAll(new ParameterList(request.getOauthParameters()));
        return params.sort().asOauthBaseString();
    }

    protected void checkPreconditions(OAuthRequest request) {
        Preconditions.checkNotNull(request, "Cannot extract base string from a null object");

        if (request.getOauthParameters() == null || request.getOauthParameters().isEmpty()) {
            throw new OAuthParametersMissingException(request);
        }
    }
}
