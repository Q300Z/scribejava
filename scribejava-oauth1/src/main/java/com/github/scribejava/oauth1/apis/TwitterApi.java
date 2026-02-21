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

public class TwitterApi extends DefaultApi10a {

  private static final String AUTHORIZE_URL = "https://api.twitter.com/oauth/authorize";
  private static final String REQUEST_TOKEN_RESOURCE = "api.twitter.com/oauth/request_token";
  private static final String ACCESS_TOKEN_RESOURCE = "api.twitter.com/oauth/access_token";

  protected TwitterApi() {}

  public static TwitterApi instance() {
    return InstanceHolder.INSTANCE;
  }

  @Override
  public String getAccessTokenEndpoint() {
    return "https://" + ACCESS_TOKEN_RESOURCE;
  }

  @Override
  public String getRequestTokenEndpoint() {
    return "https://" + REQUEST_TOKEN_RESOURCE;
  }

  @Override
  public String getAuthorizationBaseUrl() {
    return AUTHORIZE_URL;
  }

  private static class InstanceHolder {
    private static final TwitterApi INSTANCE = new TwitterApi();
  }

  /**
   * Twitter 'friendlier' authorization endpoint for OAuth.
   *
   * <p>Uses SSL.
   */
  public static class Authenticate extends TwitterApi {

    private static final String AUTHENTICATE_URL = "https://api.twitter.com/oauth/authenticate";

    private Authenticate() {}

    public static Authenticate instance() {
      return InstanceHolder.INSTANCE;
    }

    @Override
    public String getAuthorizationBaseUrl() {
      return AUTHENTICATE_URL;
    }

    private static class InstanceHolder {
      private static final Authenticate INSTANCE = new Authenticate();
    }
  }
}
