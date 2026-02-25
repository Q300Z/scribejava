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

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.httpclient.HttpClientConfig;
import java.io.OutputStream;

/** Classe de base pour les APIs OpenID Connect 1.0. */
public abstract class DefaultOidcApi20 extends DefaultApi20 {

  private OidcProviderMetadata metadata;

  public OidcProviderMetadata getMetadata() {
    return metadata;
  }

  public void setMetadata(final OidcProviderMetadata metadata) {
    this.metadata = metadata;
  }

  @Override
  public String getAccessTokenEndpoint() {
    return metadata != null ? metadata.getTokenEndpoint() : null;
  }

  @Override
  public String getAuthorizationBaseUrl() {
    return metadata != null ? metadata.getAuthorizationEndpoint() : null;
  }

  @Override
  public String getRevokeTokenEndpoint() {
    return metadata != null ? metadata.getRevocationEndpoint() : super.getRevokeTokenEndpoint();
  }

  @Override
  public String getPushedAuthorizationRequestEndpoint() {
    return metadata != null
        ? metadata.getPushedAuthorizationRequestEndpoint()
        : super.getPushedAuthorizationRequestEndpoint();
  }

  public String getJwksUri() {
    return metadata != null ? metadata.getJwksUri() : null;
  }

  public String getUserinfoEndpoint() {
    return metadata != null ? metadata.getUserinfoEndpoint() : null;
  }

  @Override
  public OidcService createService(
      String apiKey,
      String apiSecret,
      String callback,
      String defaultScope,
      String responseType,
      OutputStream debugStream,
      String userAgent,
      HttpClientConfig httpClientConfig,
      HttpClient httpClient) {
    return new OidcService(
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
}
