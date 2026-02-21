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

import static org.assertj.core.api.Assertions.assertThat;

import com.github.scribejava.core.model.OAuthConstants;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import org.junit.jupiter.api.Test;

public class BaseStringEncodingTest {

  @Test
  public void shouldHandleComplexCharactersInBaseString() {
    final BaseStringExtractorImpl extractor = new BaseStringExtractorImpl();
    final OAuthRequest request = new OAuthRequest(Verb.POST, "http://example.com/path");

    // RFC 3986 Reserved characters and non-ASCII
    request.addQuerystringParameter("key", "val with spaces & symbols !*'");
    request.addBodyParameter("emoji", "🚀");
    request.addOAuthParameter(OAuthConstants.TIMESTAMP, "123456");
    request.addOAuthParameter(OAuthConstants.CONSUMER_KEY, "asdf");

    final String baseString = extractor.extract(request);

    // POST & http%3A%2F%2Fexample.com%2Fpath & ...
    assertThat(baseString).startsWith("POST&http%3A%2F%2Fexample.com%2Fpath&");

    // Verify that emoji and symbols are double encoded in the base string
    // 🚀 -> %F0%9F%9A%80 (UTF-8) -> %25F0%259F%259A%2580 (Double encoded for Base String)
    assertThat(baseString).contains("emoji%3D%25F0%259F%259A%2580");
    assertThat(baseString)
        .contains("key%3Dval%2520with%2520spaces%2520%2526%2520symbols%2520%2521%252A%2527");
  }

  @Test
  public void shouldHandleEmptyParamsCorrectly() {
    final BaseStringExtractorImpl extractor = new BaseStringExtractorImpl();
    final OAuthRequest request = new OAuthRequest(Verb.GET, "http://example.com");
    request.addOAuthParameter(OAuthConstants.TIMESTAMP, "123");
    request.addOAuthParameter(OAuthConstants.CONSUMER_KEY, "key");
    request.addQuerystringParameter("empty", "");

    final String baseString = extractor.extract(request);
    assertThat(baseString).contains("empty%3D%26"); // empty= & ...
  }
}
