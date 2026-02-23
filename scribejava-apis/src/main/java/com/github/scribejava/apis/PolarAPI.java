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
package com.github.scribejava.apis;

import com.github.scribejava.apis.polar.PolarJsonTokenExtractor;
import com.github.scribejava.apis.polar.PolarOAuthService;
import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.extractors.TokenExtractor;
import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.httpclient.HttpClientConfig;
import com.github.scribejava.core.model.OAuth2AccessToken;
import java.io.OutputStream;

/**
 * API OAuth 2.0 pour Polar.
 *
 * @see <a href="https://www.polar.com/accesslink-api/#authentication">Polar Accesslink API
 *     Documentation</a>
 */
public class PolarAPI extends DefaultApi20 {

  /** Constructeur protégé. */
  protected PolarAPI() {}

  /**
   * Retourne l'instance unique (singleton) de l'API Polar.
   *
   * @return L'instance de {@link PolarAPI}.
   */
  public static PolarAPI instance() {
    return PolarAPI.InstanceHolder.INSTANCE;
  }

  @Override
  public String getAccessTokenEndpoint() {
    return "https://polarremote.com/v2/oauth2/token";
  }

  @Override
  public String getAuthorizationBaseUrl() {
    return "https://flow.polar.com/oauth2/authorization";
  }

  @Override
  public PolarOAuthService createService(
      String apiKey,
      String apiSecret,
      String callback,
      String defaultScope,
      String responseType,
      OutputStream debugStream,
      String userAgent,
      HttpClientConfig httpClientConfig,
      HttpClient httpClient) {

    return new PolarOAuthService(
        this,
        apiKey,
        apiSecret,
        callback,
        defaultScope,
        responseType,
        debugStream,
        userAgent,
        httpClientConfig,
        httpClient);
  }

  @Override
  public TokenExtractor<OAuth2AccessToken> getAccessTokenExtractor() {
    return PolarJsonTokenExtractor.instance();
  }

  private static class InstanceHolder {

    private static final PolarAPI INSTANCE = new PolarAPI();
  }
}
