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
package com.github.scribejava.oauth1.oauth;

import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.httpclient.HttpClientConfig;
import com.github.scribejava.core.model.OAuthAsyncRequestCallback;
import com.github.scribejava.core.model.OAuthConstants;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.oauth.OAuthService;
import com.github.scribejava.oauth1.builder.api.DefaultApi10a;
import com.github.scribejava.oauth1.builder.api.OAuth1SignatureType;
import com.github.scribejava.oauth1.model.OAuth1AccessToken;
import com.github.scribejava.oauth1.model.OAuth1RequestToken;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/** OAuth 1.0a implementation of {@link OAuthService} */
public class OAuth10aService extends OAuthService {

  private static final String VERSION = "1.0";
  private final DefaultApi10a api;
  private final String scope;

  public OAuth10aService(
      final DefaultApi10a api,
      final String apiKey,
      final String apiSecret,
      final String callback,
      final String scope,
      final OutputStream debugStream,
      final String userAgent,
      final HttpClientConfig httpClientConfig,
      final HttpClient httpClient) {
    super(apiKey, apiSecret, callback, debugStream, userAgent, httpClientConfig, httpClient);
    this.api = api;
    this.scope = scope;
  }

  public OAuth1RequestToken getRequestToken()
      throws IOException, InterruptedException, ExecutionException {
    if (isDebug()) {
      log("obtaining request token from " + api.getRequestTokenEndpoint());
    }
    final OAuthRequest request = prepareRequestTokenRequest();

    log("sending request...");
    try (Response response = execute(request)) {
      if (isDebug()) {
        final String body = response.getBody();
        log("response status code: " + response.getCode());
        log("response body: " + body);
      }
      return api.getRequestTokenExtractor().extract(response);
    }
  }

  public CompletableFuture<OAuth1RequestToken> getRequestTokenAsync() {
    return getRequestTokenAsync(null);
  }

  public CompletableFuture<OAuth1RequestToken> getRequestTokenAsync(
      final OAuthAsyncRequestCallback<OAuth1RequestToken> callback) {
    if (isDebug()) {
      log("async obtaining request token from " + api.getRequestTokenEndpoint());
    }
    final OAuthRequest request = prepareRequestTokenRequest();
    return execute(
        request,
        callback,
        response -> {
          try (Response resp = response) {
            return getApi().getRequestTokenExtractor().extract(resp);
          }
        });
  }

  protected OAuthRequest prepareRequestTokenRequest() {
    final OAuthRequest request =
        new OAuthRequest(api.getRequestTokenVerb(), api.getRequestTokenEndpoint());
    String callback = getCallback();
    if (callback == null) {
      callback = OAuthConstants.OOB;
    }
    if (isDebug()) {
      log("setting oauth_callback to " + callback);
    }
    request.addOAuthParameter(OAuthConstants.CALLBACK, callback);
    addOAuthParams(request, "");
    appendSignature(request);
    return request;
  }

  protected void addOAuthParams(final OAuthRequest request, final String tokenSecret) {
    request.addOAuthParameter(
        OAuthConstants.TIMESTAMP, api.getTimestampService().getTimestampInSeconds());
    request.addOAuthParameter(OAuthConstants.NONCE, api.getTimestampService().getNonce());
    request.addOAuthParameter(OAuthConstants.CONSUMER_KEY, getApiKey());
    request.addOAuthParameter(
        OAuthConstants.SIGN_METHOD, api.getSignatureService().getSignatureMethod());
    request.addOAuthParameter(OAuthConstants.VERSION, getVersion());
    if (scope != null) {
      request.addOAuthParameter(OAuthConstants.SCOPE, scope);
    }
    request.addOAuthParameter(OAuthConstants.SIGNATURE, getSignature(request, tokenSecret));

    if (isDebug()) {
      log("appended additional OAuth parameters: " + request.getOauthParameters());
    }
  }

  public OAuth1AccessToken getAccessToken(
      final OAuth1RequestToken requestToken, final String oauthVerifier)
      throws IOException, InterruptedException, ExecutionException {
    if (isDebug()) {
      log("obtaining access token from " + api.getAccessTokenEndpoint());
    }
    final OAuthRequest request = prepareAccessTokenRequest(requestToken, oauthVerifier);
    try (Response response = execute(request)) {
      return api.getAccessTokenExtractor().extract(response);
    }
  }

  public CompletableFuture<OAuth1AccessToken> getAccessTokenAsync(
      final OAuth1RequestToken requestToken, final String oauthVerifier) {
    return getAccessTokenAsync(requestToken, oauthVerifier, null);
  }

  public CompletableFuture<OAuth1AccessToken> getAccessTokenAsync(
      final OAuth1RequestToken requestToken,
      final String oauthVerifier,
      final OAuthAsyncRequestCallback<OAuth1AccessToken> callback) {
    if (isDebug()) {
      log("async obtaining access token from " + api.getAccessTokenEndpoint());
    }
    final OAuthRequest request = prepareAccessTokenRequest(requestToken, oauthVerifier);
    return execute(
        request,
        callback,
        response -> {
          try (Response resp = response) {
            return getApi().getAccessTokenExtractor().extract(resp);
          }
        });
  }

  protected OAuthRequest prepareAccessTokenRequest(
      final OAuth1RequestToken requestToken, final String oauthVerifier) {
    final OAuthRequest request =
        new OAuthRequest(api.getAccessTokenVerb(), api.getAccessTokenEndpoint());
    request.addOAuthParameter(OAuthConstants.TOKEN, requestToken.getToken());
    request.addOAuthParameter(OAuthConstants.VERIFIER, oauthVerifier);
    if (isDebug()) {
      log("setting token to: " + requestToken + " and verifier to: " + oauthVerifier);
    }
    addOAuthParams(request, requestToken.getTokenSecret());
    appendSignature(request);
    return request;
  }

  public void signRequest(final OAuth1AccessToken token, final OAuthRequest request) {
    if (isDebug()) {
      log("signing request: " + request.getCompleteUrl());
    }

    if (!token.isEmpty() || api.isEmptyOAuthTokenParamIsRequired()) {
      request.addOAuthParameter(OAuthConstants.TOKEN, token.getToken());
    }
    if (isDebug()) {
      log("setting token to: " + token);
    }
    addOAuthParams(request, token.getTokenSecret());
    appendSignature(request);
  }

  public void signRequest(
      final String token, final String tokenSecret, final OAuthRequest request) {
    signRequest(new OAuth1AccessToken(token, tokenSecret), request);
  }

  @Override
  public String getVersion() {
    return VERSION;
  }

  public String getAuthorizationUrl(final OAuth1RequestToken requestToken) {
    return api.getAuthorizationUrl(requestToken);
  }

  private String getSignature(final OAuthRequest request, final String tokenSecret) {
    log("generating signature...");
    final String baseString = api.getBaseStringExtractor().extract(request);
    final String signature =
        api.getSignatureService().getSignature(baseString, getApiSecret(), tokenSecret);

    if (isDebug()) {
      log("base string is: " + baseString);
      log("signature is: " + signature);
    }
    return signature;
  }

  protected void appendSignature(final OAuthRequest request) {
    final OAuth1SignatureType signatureType = api.getSignatureType();
    switch (signatureType) {
      case HEADER:
        log("using Http Header signature");

        final String oauthHeader = api.getHeaderExtractor().extract(request);
        request.addHeader(OAuthConstants.HEADER, oauthHeader);
        break;
      case QUERY_STRING:
        log("using Querystring signature");

        for (final Map.Entry<String, String> oauthParameter :
            request.getOauthParameters().entrySet()) {
          request.addQuerystringParameter(oauthParameter.getKey(), oauthParameter.getValue());
        }
        break;
      default:
        throw new IllegalStateException("Unknown Signature Type '" + signatureType + "'.");
    }
  }

  public DefaultApi10a getApi() {
    return api;
  }
}
