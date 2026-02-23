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
import com.github.scribejava.core.model.OAuthConstants;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.utils.OAuthEncoder;
import com.github.scribejava.core.utils.Preconditions;
import java.util.stream.Collectors;

/** Default implementation of {@link HeaderExtractor}. Conforms to OAuth 1.0a */
public class HeaderExtractorImpl implements HeaderExtractor {

  private static final String PARAM_SEPARATOR = ", ";
  private static final String PREAMBLE = "OAuth ";

  /** {@inheritDoc} */
  @Override
  public String extract(OAuthRequest request) {
    checkPreconditions(request);

    final String oauthParams =
        request.getOauthParameters().entrySet().stream()
            .map(entry -> entry.getKey() + "=\"" + OAuthEncoder.encode(entry.getValue()) + "\"")
            .collect(Collectors.joining(PARAM_SEPARATOR)); // USE Stream

    final StringBuilder header = new StringBuilder(PREAMBLE).append(oauthParams);

    if (request.getRealm() != null && !request.getRealm().isEmpty()) {
      header
          .append(PARAM_SEPARATOR)
          .append(OAuthConstants.REALM)
          .append("=\"")
          .append(request.getRealm())
          .append('"');
    }
    return header.toString();
  }

  private void checkPreconditions(OAuthRequest request) {
    Preconditions.checkNotNull(request, "Cannot extract a header from a null object");

    if (request.getOauthParameters() == null || request.getOauthParameters().isEmpty()) {
      throw new OAuthParametersMissingException(request);
    }
  }
}
