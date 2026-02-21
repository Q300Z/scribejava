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

/**
 * Google OpenID Connect 1.0 API implementation.
 *
 * <p>This class uses the DefaultOidcApi20 structure to support discovery and ID Token features.
 */
public class OidcGoogleApi20 extends DefaultOidcApi20 {

  protected OidcGoogleApi20() {}

  private static class InstanceHolder {
    private static final OidcGoogleApi20 INSTANCE = new OidcGoogleApi20();
  }

  public static OidcGoogleApi20 instance() {
    return InstanceHolder.INSTANCE;
  }

  @Override
  public String getIssuer() {
    return "https://accounts.google.com";
  }

  @Override
  public String getAccessTokenEndpoint() {
    final String endpoint = super.getAccessTokenEndpoint();
    return endpoint != null ? endpoint : "https://oauth2.googleapis.com/token";
  }

  @Override
  public String getAuthorizationBaseUrl() {
    final String baseUrl = super.getAuthorizationBaseUrl();
    return baseUrl != null ? baseUrl : "https://accounts.google.com/o/oauth2/v2/auth";
  }

  @Override
  public String getRevokeTokenEndpoint() {
    final String endpoint = super.getRevokeTokenEndpoint();
    return endpoint != null ? endpoint : "https://oauth2.googleapis.com/revoke";
  }
}
