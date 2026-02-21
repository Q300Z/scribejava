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
 * Microsoft Azure Active Directory OpenID Connect 1.0 API implementation (v2.0).
 *
 * <p>This class supports discovery and ID Token features for Microsoft Entra ID.
 */
public class OidcMicrosoftAzureActiveDirectory20Api extends DefaultOidcApi20 {

  private static final String DEFAULT_TENANT = "common";
  private final String tenant;

  protected OidcMicrosoftAzureActiveDirectory20Api() {
    this(DEFAULT_TENANT);
  }

  protected OidcMicrosoftAzureActiveDirectory20Api(String tenant) {
    this.tenant = tenant;
  }

  public static OidcMicrosoftAzureActiveDirectory20Api instance() {
    return InstanceHolder.INSTANCE;
  }

  public static OidcMicrosoftAzureActiveDirectory20Api custom(String tenant) {
    return new OidcMicrosoftAzureActiveDirectory20Api(tenant);
  }

  @Override
  public String getIssuer() {
    return "https://login.microsoftonline.com/" + tenant + "/v2.0";
  }

  @Override
  public String getAccessTokenEndpoint() {
    final String endpoint = super.getAccessTokenEndpoint();
    return endpoint != null
        ? endpoint
        : "https://login.microsoftonline.com/" + tenant + "/oauth2/v2.0/token";
  }

  @Override
  public String getAuthorizationBaseUrl() {
    final String baseUrl = super.getAuthorizationBaseUrl();
    return baseUrl != null
        ? baseUrl
        : "https://login.microsoftonline.com/" + tenant + "/oauth2/v2.0/authorize";
  }

  private static class InstanceHolder {
    private static final OidcMicrosoftAzureActiveDirectory20Api INSTANCE =
        new OidcMicrosoftAzureActiveDirectory20Api();
  }
}
