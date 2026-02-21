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
package com.github.scribejava.apis.microsoftazureactivedirectory;

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.oauth2.clientauthentication.ClientAuthentication;
import com.github.scribejava.core.oauth2.clientauthentication.RequestBodyAuthenticationScheme;

public abstract class BaseMicrosoftAzureActiveDirectoryApi extends DefaultApi20 {

  protected static final String COMMON_TENANT = "common";

  private static final String MSFT_LOGIN_URL = "https://login.microsoftonline.com/";
  private static final String OAUTH_2 = "/oauth2";
  private final String tenant;

  protected BaseMicrosoftAzureActiveDirectoryApi() {
    this(COMMON_TENANT);
  }

  protected BaseMicrosoftAzureActiveDirectoryApi(String tenant) {
    this.tenant = tenant == null || tenant.isEmpty() ? COMMON_TENANT : tenant;
  }

  @Override
  public String getAccessTokenEndpoint() {
    return MSFT_LOGIN_URL + tenant + OAUTH_2 + getEndpointVersionPath() + "/token";
  }

  @Override
  public String getAuthorizationBaseUrl() {
    return MSFT_LOGIN_URL + tenant + OAUTH_2 + getEndpointVersionPath() + "/authorize";
  }

  @Override
  public ClientAuthentication getClientAuthentication() {
    return RequestBodyAuthenticationScheme.instance();
  }

  protected String getEndpointVersionPath() {
    return "";
  }
}
