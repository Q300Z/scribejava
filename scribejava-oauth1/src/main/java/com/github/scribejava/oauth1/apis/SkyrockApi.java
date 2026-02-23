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

/**
 * OAuth API for Skyrock.
 *
 * @see <a href="http://www.skyrock.com/developer/">Skyrock.com API</a>
 */
public class SkyrockApi extends DefaultApi10a {

  private static final String API_ENDPOINT = "https://api.skyrock.com/v2";
  private static final String AUTHORIZE_URL = "/oauth/authorize";

  protected SkyrockApi() {}

  /**
   * @return Instance unique de l'API Skyrock.
   */
  public static SkyrockApi instance() {
    return InstanceHolder.INSTANCE;
  }

  @Override
  public String getAccessTokenEndpoint() {
    return API_ENDPOINT + "/oauth/access_token";
  }

  @Override
  public String getRequestTokenEndpoint() {
    return API_ENDPOINT + "/oauth/initiate";
  }

  @Override
  public String getAuthorizationBaseUrl() {
    return API_ENDPOINT + AUTHORIZE_URL;
  }

  private static class InstanceHolder {
    private static final SkyrockApi INSTANCE = new SkyrockApi();
  }
}
