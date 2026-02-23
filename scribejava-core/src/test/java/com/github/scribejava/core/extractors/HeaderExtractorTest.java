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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.github.scribejava.core.ObjectMother;
import com.github.scribejava.core.model.OAuthRequest;
import org.junit.Before;
import org.junit.Test;

public class HeaderExtractorTest {

  private HeaderExtractorImpl extractor;
  private OAuthRequest request;

  @Before
  public void setUp() {
    request = ObjectMother.createSampleOAuthRequest();
    extractor = new HeaderExtractorImpl();
  }

  @Test
  public void shouldExtractStandardHeader() {
    final String header = extractor.extract(request);
    final String oauth = "OAuth ";
    final String callback = "oauth_callback=\"http%3A%2F%2Fexample%2Fcallback\"";
    final String signature = "oauth_signature=\"OAuth-Signature\"";
    final String key = "oauth_consumer_key=\"AS%23%24%5E%2A%40%26\"";
    final String timestamp = "oauth_timestamp=\"123456\"";

    assertTrue(header.startsWith(oauth));
    assertTrue(header.contains(callback));
    assertTrue(header.contains(signature));
    assertTrue(header.contains(key));
    assertTrue(header.contains(timestamp));
    // Assert that header only contains the checked elements above and nothing else
    assertEquals(
        ", , , ",
        header
            .replaceFirst(oauth, "")
            .replaceFirst(callback, "")
            .replaceFirst(signature, "")
            .replaceFirst(key, "")
            .replaceFirst(timestamp, ""));
  }
}
