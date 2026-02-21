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
  public void shouldHandleExoticCharactersInBaseString() {
    final BaseStringExtractorImpl extractor = new BaseStringExtractorImpl();
    final OAuthRequest request = new OAuthRequest(Verb.GET, "http://example.com");

    // Kanji, Cyrillique et espaces multiples
    request.addQuerystringParameter("language", "日本語");
    request.addQuerystringParameter("greeting", "Добрый день");
    request.addQuerystringParameter("spaces", "  multiple   spaces  ");
    request.addOAuthParameter(OAuthConstants.TIMESTAMP, "123");
    request.addOAuthParameter(OAuthConstants.CONSUMER_KEY, "key");

    final String baseString = extractor.extract(request);

    // Vérification de l'encodage double (pour la Base String)
    // 日本語 -> %E6%97%A5%E6%9C%AC%E8%AA%9E -> %25E6%2597%25A5%25E6%259C%25AC%25E8%25AA%259E
    assertThat(baseString).contains("language%3D%25E6%2597%25A5%25E6%259C%25AC%25E8%25AA%259E");
    // Добрый день (avec espace)
    assertThat(baseString)
        .contains(
            "greeting%3D%25D0%2594%25D0%25BE%25D0%25B1%25D1%2580%25D1%258B%25D0%25B9%2520%25D0%25B4%25D0%25B5%25D0%25BD%25D1%258C");
    // Spaces
    assertThat(baseString).contains("spaces%3D%2520%2520multiple%2520%2520%2520spaces%2520%2520");
  }
}
