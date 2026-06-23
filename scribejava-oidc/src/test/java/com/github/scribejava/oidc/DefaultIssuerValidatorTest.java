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
package com.github.scribejava.oidc;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class DefaultIssuerValidatorTest {

  @Test
  public void testDefaultIssuerValidator() {
    IssuerValidator validator = new DefaultIssuerValidator();

    // Exact match
    assertThat(
            validator.isValid(
                "https://login.microsoftonline.com/common",
                "https://login.microsoftonline.com/common",
                null))
        .isTrue();

    // Microsoft dynamic tenant match with tid claim
    Map<String, Object> claims = new HashMap<>();
    claims.put("tid", "12345");
    assertThat(
            validator.isValid(
                "https://login.microsoftonline.com/{tenantid}/v2.0",
                "https://login.microsoftonline.com/12345/v2.0",
                claims))
        .isTrue();
    assertThat(
            validator.isValid(
                "https://login.microsoftonline.com/common/v2.0",
                "https://login.microsoftonline.com/12345/v2.0",
                claims))
        .isTrue();

    // General multi-tenant Okta/custom tenant matching
    assertThat(validator.isValid("https://{tenant}.okta.com", "https://mytenant.okta.com", null))
        .isTrue();

    // Invalid matches
    assertThat(
            validator.isValid("https://{tenant}.okta.com", "https://mytenant.okta.com/extra", null))
        .isFalse();
    assertThat(validator.isValid("https://login.microsoftonline.com/common", null, null)).isFalse();
  }
}
