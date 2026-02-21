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
package com.github.scribejava.core.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

public class OAuthEncoderTest {

  @Test
  public void shouldPercentEncodeString() {
    final String plain = "this is a test &^";
    final String encoded = "this%20is%20a%20test%20%26%5E";
    assertThat(OAuthEncoder.encode(plain)).isEqualTo(encoded);
  }

  @Test
  public void shouldFormURLDecodeString() {
    final String encoded = "this+is+a+test+%26%5E";
    final String plain = "this is a test &^";
    assertThat(OAuthEncoder.decode(encoded)).isEqualTo(plain);
  }

  @Test
  public void shouldPercentEncodeAllSpecialCharacters() {
    final String plain = "!*'();:@&=+$,/?#[]";
    final String encoded = "%21%2A%27%28%29%3B%3A%40%26%3D%2B%24%2C%2F%3F%23%5B%5D";
    assertThat(OAuthEncoder.encode(plain)).isEqualTo(encoded);
    assertThat(OAuthEncoder.decode(encoded)).isEqualTo(plain);
  }

  @Test
  public void shouldNotPercentEncodeReservedCharacters() {
    final String plain = "abcde123456-._~";
    final String encoded = plain;
    assertThat(OAuthEncoder.encode(plain)).isEqualTo(encoded);
  }

  @Test
  public void shouldThrowExceptionIfStringToEncodeIsNull() {
    assertThatThrownBy(() -> OAuthEncoder.encode(null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void shouldThrowExceptionIfStringToDecodeIsNull() {
    assertThatThrownBy(() -> OAuthEncoder.decode(null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void shouldPercentEncodeCorrectlyTwitterCodingExamples() {
    // These tests are part of the Twitter dev examples here
    // -> https://dev.twitter.com/docs/auth/percent-encoding-parameters
    final String[] sources = {"Ladies + Gentlemen", "An encoded string!", "Dogs, Cats & Mice"};
    final String[] encoded = {
      "Ladies%20%2B%20Gentlemen", "An%20encoded%20string%21", "Dogs%2C%20Cats%20%26%20Mice"
    };

    for (int i = 0; i < sources.length; i++) {
      assertThat(OAuthEncoder.encode(sources[i])).isEqualTo(encoded[i]);
    }
  }
}
