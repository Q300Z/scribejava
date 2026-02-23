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

import com.github.scribejava.core.utils.OAuthEncoder;
import com.github.scribejava.oauth1.builder.api.DefaultApi10a;
import com.github.scribejava.oauth1.model.OAuth1RequestToken;
import java.util.Collection;

/** API OAuth 1.0a pour LinkedIn. */
public class LinkedInApi extends DefaultApi10a {

  private static final String AUTHORIZE_URL = "https://api.linkedin.com/uas/oauth/authenticate";

  private final String scopesAsString;

  /** Constructeur par défaut. */
  protected LinkedInApi() {
    this(null);
  }

  /**
   * Constructeur avec portées.
   *
   * @param scopes Les portées OAuth souhaitées.
   */
  protected LinkedInApi(Collection<String> scopes) {
    if (scopes == null || scopes.isEmpty()) {
      scopesAsString = null;
    } else {
      final StringBuilder builder = new StringBuilder();
      for (String scope : scopes) {
        builder.append('+').append(scope);
      }
      scopesAsString = builder.substring(1);
    }
  }

  /**
   * Retourne l'instance unique (singleton) de l'API LinkedIn (1.0a).
   *
   * @return L'instance de {@link LinkedInApi}.
   */
  public static LinkedInApi instance() {
    return InstanceHolder.INSTANCE;
  }

  /**
   * Retourne une instance de l'API LinkedIn avec des portées spécifiques.
   *
   * @param scopes Les portées.
   * @return L'instance configurée.
   */
  public static LinkedInApi instance(Collection<String> scopes) {
    return new LinkedInApi(scopes);
  }

  @Override
  public String getAccessTokenEndpoint() {
    return "https://api.linkedin.com/uas/oauth/accessToken";
  }

  @Override
  public String getRequestTokenEndpoint() {
    return scopesAsString == null
        ? "https://api.linkedin.com/uas/oauth/requestToken"
        : "https://api.linkedin.com/uas/oauth/requestToken?scope=" + scopesAsString;
  }

  @Override
  public String getAuthorizationBaseUrl() {
    return AUTHORIZE_URL;
  }

  @Override
  public String getAuthorizationUrl(OAuth1RequestToken requestToken) {
    return String.format(
        AUTHORIZE_URL + "?oauth_token=%s", OAuthEncoder.encode(requestToken.getToken()));
  }

  private static class InstanceHolder {

    private static final LinkedInApi INSTANCE = new LinkedInApi();
  }
}
