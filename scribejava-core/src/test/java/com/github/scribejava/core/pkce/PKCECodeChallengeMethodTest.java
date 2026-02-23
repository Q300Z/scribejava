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
package com.github.scribejava.core.pkce;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * test PKCE according to<br>
 * Appendix B. Example for the S256 code_challenge_method<br>
 * <a href="https://tools.ietf.org/html/rfc7636#appendix-B">...</a>
 */
public class PKCECodeChallengeMethodTest {

  private static final byte[] RANDOM_BYTES =
      new byte[] {
        116,
        24,
        (byte) 223,
        (byte) 180,
        (byte) 151,
        (byte) 153,
        (byte) 224,
        37,
        79,
        (byte) 250,
        96,
        125,
        (byte) 216,
        (byte) 173,
        (byte) 187,
        (byte) 186,
        22,
        (byte) 212,
        37,
        77,
        105,
        (byte) 214,
        (byte) 191,
        (byte) 240,
        91,
        88,
        5,
        88,
        83,
        (byte) 132,
        (byte) 141,
        121
      };

  @Test
  public void testGeneratingPKCE() {
    final PKCE pkce = PKCEService.defaultInstance().generatePKCE(RANDOM_BYTES);

    assertThat(pkce.getCodeChallengeMethod()).isEqualTo(PKCECodeChallengeMethod.S256);
    assertThat(pkce.getCodeVerifier()).isEqualTo("dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk");
    assertThat(pkce.getCodeChallenge()).isEqualTo("E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM");
  }
}
