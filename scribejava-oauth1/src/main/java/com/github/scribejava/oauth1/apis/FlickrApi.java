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
 * OAuth API for Flickr.
 *
 * @see <a href="http://www.flickr.com/services/api/">Flickr API</a>
 */
public class FlickrApi extends DefaultApi10a {

  private static final String AUTHORIZE_URL = "https://www.flickr.com/services/oauth/authorize";
  /** read, write, or delete (delete includes read/write) */
  private final String permString;

  protected FlickrApi() {
    permString = null;
  }

  protected FlickrApi(FlickrPerm perm) {
    permString = perm.name().toLowerCase();
  }

  public static FlickrApi instance() {
    return InstanceHolder.INSTANCE;
  }

  public static FlickrApi instance(FlickrPerm perm) {
    return perm == null ? instance() : new FlickrApi(perm);
  }

  /** {@inheritDoc} */
  @Override
  public String getAccessTokenEndpoint() {
    return "https://www.flickr.com/services/oauth/access_token";
  }

  @Override
  public String getAuthorizationBaseUrl() {
    return permString == null ? AUTHORIZE_URL : AUTHORIZE_URL + "?perms=" + permString;
  }

  /** {@inheritDoc} */
  @Override
  public String getRequestTokenEndpoint() {
    return "https://www.flickr.com/services/oauth/request_token";
  }

  public enum FlickrPerm {
    READ,
    WRITE,
    DELETE
  }

  private static class InstanceHolder {

    private static final FlickrApi INSTANCE = new FlickrApi();
  }
}
