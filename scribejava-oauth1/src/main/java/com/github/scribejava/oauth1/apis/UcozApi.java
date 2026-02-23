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
package com.github.scribejava.oauth1.apis;

import com.github.scribejava.oauth1.builder.api.DefaultApi10a;

/** API OAuth 1.0a pour uCoz. */
public class UcozApi extends DefaultApi10a {

  private static final String AUTHORIZE_URL = "http://uapi.ucoz.com/accounts/oauthauthorizetoken";

  protected UcozApi() {}

  /**
   * @return Instance unique de l'API uCoz.
   */
  public static UcozApi instance() {
    return InstanceHolder.INSTANCE;
  }

  @Override
  public String getAccessTokenEndpoint() {
    return "http://uapi.ucoz.com/accounts/oauthgetaccesstoken";
  }

  @Override
  public String getRequestTokenEndpoint() {
    return "http://uapi.ucoz.com/accounts/oauthgetrequesttoken";
  }

  @Override
  public String getAuthorizationBaseUrl() {
    return AUTHORIZE_URL;
  }

  private static class InstanceHolder {
    private static final UcozApi INSTANCE = new UcozApi();
  }
}
