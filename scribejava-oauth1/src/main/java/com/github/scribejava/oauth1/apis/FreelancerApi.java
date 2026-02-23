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

/** API OAuth 1.0a pour Freelancer. */
public class FreelancerApi extends DefaultApi10a {

  private static final String AUTHORIZATION_URL =
      "http://www.freelancer.com/users/api-token/auth.php";

  protected FreelancerApi() {}

  /**
   * @return Instance unique de l'API Freelancer.
   */
  public static FreelancerApi instance() {
    return InstanceHolder.INSTANCE;
  }

  @Override
  public String getAccessTokenEndpoint() {
    return "http://api.freelancer.com/RequestAccessToken.php";
  }

  @Override
  public String getRequestTokenEndpoint() {
    return "http://api.freelancer.com/RequestRequestToken.php";
  }

  @Override
  public String getAuthorizationBaseUrl() {
    return AUTHORIZATION_URL;
  }

  private static class InstanceHolder {
    private static final FreelancerApi INSTANCE = new FreelancerApi();
  }

  /** Version Sandbox de l'API Freelancer. */
  public static class Sandbox extends FreelancerApi {

    private static final String SANDBOX_AUTHORIZATION_URL =
        "http://www.sandbox.freelancer.com/users/api-token/auth.php";

    private Sandbox() {}

    /**
     * @return Instance unique Sandbox.
     */
    public static Sandbox instance() {
      return InstanceHolder.INSTANCE;
    }

    @Override
    public String getAccessTokenEndpoint() {
      return "http://api.sandbox.freelancer.com/RequestAccessToken.php";
    }

    @Override
    public String getRequestTokenEndpoint() {
      return "http://api.sandbox.freelancer.com/RequestRequestToken.php";
    }

    @Override
    public String getAuthorizationBaseUrl() {
      return SANDBOX_AUTHORIZATION_URL;
    }

    private static class InstanceHolder {
      private static final Sandbox INSTANCE = new Sandbox();
    }
  }
}
