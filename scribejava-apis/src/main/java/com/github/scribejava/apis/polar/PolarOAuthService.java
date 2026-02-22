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
package com.github.scribejava.apis.polar;

import com.github.scribejava.apis.PolarAPI;
import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.httpclient.HttpClientConfig;
import com.github.scribejava.core.model.OAuthConstants;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.oauth.AccessTokenRequestParams;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.pkce.PKCE;
import java.io.OutputStream;

/** Service OAuth spécifique à Polar. */
public class PolarOAuthService extends OAuth20Service {

  /**
   * Constructeur.
   *
   * @param api L'API associée.
   * @param apiKey La clé API.
   * @param apiSecret Le secret API.
   * @param callback L'URL de rappel.
   * @param defaultScope La portée par défaut.
   * @param responseType Le type de réponse.
   * @param debugStream Flux de débogage.
   * @param userAgent Chaîne User-Agent.
   * @param httpClientConfig Configuration HTTP.
   * @param httpClient Le client HTTP.
   */
  public PolarOAuthService(
      PolarAPI api,
      String apiKey,
      String apiSecret,
      String callback,
      String defaultScope,
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
        defaultScope,
        responseType,
        debugStream,
        userAgent,
        httpClientConfig,
        httpClient);
  }

  @Override
  protected OAuthRequest createAccessTokenRequest(AccessTokenRequestParams params) {
    final OAuthRequest request =
        new OAuthRequest(getApi().getAccessTokenVerb(), getApi().getAccessTokenEndpoint());

    getApi()
        .getClientAuthentication()
        .addClientAuthentication(request, getApiKey(), getApiSecret());

    request.addParameter(OAuthConstants.CODE, params.getCode());
    final String callback = getCallback();
    if (callback != null) {
      request.addParameter(OAuthConstants.REDIRECT_URI, callback);
    }
    request.addParameter(OAuthConstants.GRANT_TYPE, OAuthConstants.AUTHORIZATION_CODE);

    final String pkceCodeVerifier = params.getPkceCodeVerifier();
    if (pkceCodeVerifier != null) {
      request.addParameter(PKCE.PKCE_CODE_VERIFIER_PARAM, pkceCodeVerifier);
    }
    if (isDebug()) {
      log(
          "created access token request with body params [%s], query string params [%s]",
          request.getBodyParams().asFormUrlEncodedString(),
          request.getQueryStringParams().asFormUrlEncodedString());
    }
    return request;
  }
}
