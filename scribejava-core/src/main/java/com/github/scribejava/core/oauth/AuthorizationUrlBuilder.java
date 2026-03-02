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
package com.github.scribejava.core.oauth;

import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.model.OAuthConstants;
import com.github.scribejava.core.model.ParameterList;
import com.github.scribejava.core.model.PushedAuthorizationResponse;
import com.github.scribejava.core.pkce.PKCE;
import com.github.scribejava.core.pkce.PKCEService;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/** Constructeur fluide pour les URLs d'autorisation OAuth 2.0. */
public class AuthorizationUrlBuilder {

  private final OAuth20Service oauth20Service;

  private String state;
  private Map<String, String> additionalParams;
  private PKCE pkce;
  private String scope;
  private boolean usePushedAuthorizationRequests;

  /**
   * @param oauth20Service oauth20Service
   */
  public AuthorizationUrlBuilder(OAuth20Service oauth20Service) {
    this.oauth20Service = oauth20Service;
  }

  /**
   * @param state state
   * @return builder
   */
  public AuthorizationUrlBuilder state(String state) {
    this.state = state;
    return this;
  }

  /**
   * @param nonce nonce
   * @return builder
   */
  public AuthorizationUrlBuilder nonce(String nonce) {
    if (this.additionalParams == null) {
      this.additionalParams = new HashMap<>();
    }
    this.additionalParams.put("nonce", nonce);
    return this;
  }

  /**
   * @param additionalParams additionalParams
   * @return builder
   */
  public AuthorizationUrlBuilder additionalParams(Map<String, String> additionalParams) {
    this.additionalParams = additionalParams;
    return this;
  }

  /**
   * @param pkce pkce
   * @return builder
   */
  public AuthorizationUrlBuilder pkce(PKCE pkce) {
    this.pkce = pkce;
    return this;
  }

  /**
   * @return builder
   */
  public AuthorizationUrlBuilder initPKCE() {
    this.pkce = PKCEService.defaultInstance().generatePKCE();
    return this;
  }

  /**
   * @param scope scope
   * @return builder
   */
  public AuthorizationUrlBuilder scope(String scope) {
    this.scope = scope;
    return this;
  }

  /**
   * @return pkce
   */
  public PKCE getPkce() {
    return pkce;
  }

  /**
   * @return url
   */
  public String build() {
    final Map<String, String> params =
        additionalParams == null ? new HashMap<>() : new HashMap<>(additionalParams);

    oauth20Service.getAuthorizationRequestInterceptors().forEach(i -> i.intercept(params));

    if (pkce != null) {
      params.putAll(pkce.getAuthorizationUrlParams());
    }

    if (usePushedAuthorizationRequests) {
      try {
        final PushedAuthorizationResponse parResponse =
            oauth20Service
                .pushAuthorizationRequestAsync(
                    oauth20Service.getResponseType(),
                    oauth20Service.getApiKey(),
                    oauth20Service.getCallback(),
                    scope == null ? oauth20Service.getDefaultScope() : scope,
                    state,
                    params)
                .get();

        final ParameterList parameters = new ParameterList();
        parameters.add("request_uri", parResponse.getRequestUri());
        parameters.add(OAuthConstants.CLIENT_ID, oauth20Service.getApiKey());
        if (state != null) {
          parameters.add(OAuthConstants.STATE, state);
        }
        return parameters.appendTo(oauth20Service.getApi().getAuthorizationBaseUrl());
      } catch (InterruptedException | ExecutionException e) {
        throw new OAuthException("Failed to push authorization request", e);
      }
    } else {
      final ParameterList parameters = new ParameterList(params);
      parameters.add(OAuthConstants.RESPONSE_TYPE, oauth20Service.getResponseType());
      parameters.add(OAuthConstants.CLIENT_ID, oauth20Service.getApiKey());

      final String callback = oauth20Service.getCallback();
      if (callback != null) {
        parameters.add(OAuthConstants.REDIRECT_URI, callback);
      }

      final String effectiveScope = scope == null ? oauth20Service.getDefaultScope() : scope;
      if (effectiveScope != null) {
        parameters.add(OAuthConstants.SCOPE, effectiveScope);
      }

      if (state != null) {
        parameters.add(OAuthConstants.STATE, state);
      }

      final Map<String, String> convertedParams =
          oauth20Service.getAuthorizationRequestConverter().convert(parameters.asMap());
      return new ParameterList(convertedParams)
          .appendTo(oauth20Service.getApi().getAuthorizationBaseUrl());
    }
  }
}
