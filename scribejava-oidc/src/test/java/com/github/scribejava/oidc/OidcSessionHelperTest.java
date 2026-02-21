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

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class OidcSessionHelperTest {

  @Test
  public void shouldGenerateSessionManagementHtml() {
    final String html =
        OidcSessionHelper.getSessionManagementIframeHtml(
            "https://idp.com/check-session", "client-123", "session-state-abc");

    assertTrue(html.contains("https://idp.com/check-session"));
    assertTrue(html.contains("client-123"));
    assertTrue(html.contains("session-state-abc"));
    assertTrue(html.contains("postMessage"));
  }

  @Test
  public void shouldGenerateFrontChannelLogoutHtml() {
    final String html =
        OidcSessionHelper.getFrontChannelLogoutIframeHtml(
            "https://client.com/logout", "https://idp.com", "sid-123");

    assertTrue(
        html.contains(
            "<iframe src=\"https://client.com/logout?iss=https%3A%2F%2Fidp.com&sid=sid-123\""));
    assertTrue(html.contains("style=\"display:none\""));
  }
}
