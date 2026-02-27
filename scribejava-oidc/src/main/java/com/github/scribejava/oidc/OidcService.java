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
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.oauth2.grant.OAuth20Grant;
import com.github.scribejava.core.utils.JsonUtils;
import com.github.scribejava.oidc.model.OidcNonce;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/** Service OIDC natif. */
public class OidcService extends OAuth20Service {

  private IdTokenValidator idTokenValidator;

  public OidcService(
      DefaultApi20 api,
      String apiKey,
      String apiSecret,
      String callback,
      String scope,
      String responseType,
      OutputStream debugStream,
      String userAgent,
      HttpClientConfig httpClientConfig,
      HttpClient httpClient) {
    super(
        api,
        apiKey,
        apiSecret,
        callback,
        scope,
        responseType,
        debugStream,
        userAgent,
        httpClientConfig,
        httpClient);
  }

  @Override
  public OAuth2AccessToken getAccessToken(OAuth20Grant grant)
      throws IOException, InterruptedException, java.util.concurrent.ExecutionException {
    final OAuth2AccessToken token = super.getAccessToken(grant);
    validateIdToken(token);
    return token;
  }

  @Override
  @Deprecated
  public OAuth2AccessToken getAccessToken(String code)
      throws IOException, InterruptedException, java.util.concurrent.ExecutionException {
    return getAccessToken(new com.github.scribejava.core.oauth2.grant.AuthorizationCodeGrant(code));
  }

  private void validateIdToken(OAuth2AccessToken token) {
    if (idTokenValidator != null) {
      idTokenValidator.validate(token.getRawResponse(), null, 0);
    } else {
      final Map<String, Object> response = JsonUtils.parse(token.getRawResponse());
      final String idTokenRaw = (String) response.get("id_token");
      if (idTokenRaw != null) {
        new IdToken(idTokenRaw);
      }
    }
  }

  /**
   * @param token token
   * @param nonce nonce (optionnel)
   * @return IdToken validé
   */
  public IdToken validateIdToken(OAuth2AccessToken token, OidcNonce nonce) {
    if (idTokenValidator != null) {
      return idTokenValidator.validate(token.getRawResponse(), nonce, 0);
    }
    final Map<String, Object> response = JsonUtils.parse(token.getRawResponse());
    final String idTokenRaw = (String) response.get("id_token");
    return idTokenRaw != null ? new IdToken(idTokenRaw) : null;
  }

  /**
   * @return idTokenValidator
   */
  public IdTokenValidator getIdTokenValidator() {
    return idTokenValidator;
  }

  /**
   * @param idTokenValidator idTokenValidator
   */
  public void setIdTokenValidator(IdTokenValidator idTokenValidator) {
    this.idTokenValidator = idTokenValidator;
  }

  /**
   * @param token access token
   * @return future
   */
  public java.util.concurrent.CompletableFuture<StandardClaims> getUserInfoAsync(
      OAuth2AccessToken token) {
    return java.util.concurrent.CompletableFuture.completedFuture(
        new StandardClaims(java.util.Collections.emptyMap()));
  }
}
