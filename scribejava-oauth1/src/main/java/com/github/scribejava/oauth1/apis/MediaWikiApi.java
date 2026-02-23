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
 * API OAuth 1.0a pour MediaWiki.
 *
 * <p>Supporte les wikis de la Wikimedia Foundation et les instances personnalisées.
 */
public class MediaWikiApi extends DefaultApi10a {

  private final String indexUrl;
  private final String niceUrlBase;

  /**
   * @param indexUrl The URL to the index.php of the wiki. Due to <a
   *     href="https://phabricator.wikimedia.org/T59500">a MediaWiki bug</a>, some requests must
   *     currently use the non-nice URL.
   * @param niceUrlBase The base of nice URLs for the wiki, including the trailing slash. Due to <a
   *     href="https://phabricator.wikimedia.org/T74186">another MediaWiki bug</a>, some requests
   *     must currently use the nice URL.
   */
  public MediaWikiApi(String indexUrl, String niceUrlBase) {
    this.indexUrl = indexUrl;
    this.niceUrlBase = niceUrlBase;
  }

  /**
   * The instance for wikis hosted by the Wikimedia Foundation. Consumers are requested on <a
   * href="https://meta.wikimedia.org/wiki/Special:OAuthConsumerRegistration/propose">
   * Special:OAuthConsumerRegistration/propose </a>.
   *
   * @return instance
   */
  public static MediaWikiApi instance() {
    return InstanceHolder.INSTANCE;
  }

  /**
   * The instance for wikis in the Wikimedia Foundation’s Beta Cluster. Consumers are requested on
   * <a
   * href="https://meta.wikimedia.beta.wmflabs.org/wiki/Special:OAuthConsumerRegistration/propose">
   * Special:OAuthConsumerRegistration/propose </a>.
   *
   * @return instanceBeta
   */
  public static MediaWikiApi instanceBeta() {
    return BetaInstanceHolder.BETA_INSTANCE;
  }

  @Override
  public String getRequestTokenEndpoint() {
    return indexUrl + "?title=Special:OAuth/initiate";
  }

  @Override
  public String getAuthorizationBaseUrl() {
    return niceUrlBase + "Special:OAuth/authorize";
  }

  @Override
  public String getAccessTokenEndpoint() {
    return indexUrl + "?title=Special:OAuth/token";
  }

  private static class InstanceHolder {

    private static final MediaWikiApi INSTANCE =
        new MediaWikiApi(
            "https://meta.wikimedia.org/w/index.php", "https://meta.wikimedia.org/wiki/");
  }

  private static class BetaInstanceHolder {

    private static final MediaWikiApi BETA_INSTANCE =
        new MediaWikiApi(
            "https://meta.wikimedia.beta.wmflabs.org/w/index.php",
            "https://meta.wikimedia.beta.wmflabs.org/wiki/");
  }
}
