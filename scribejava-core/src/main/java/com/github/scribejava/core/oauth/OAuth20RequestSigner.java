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

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.dpop.DPoPProofCreator;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;

/** Responsible for signing OAuth 2.0 requests, including DPoP proof creation. */
public class OAuth20RequestSigner {

  private final DefaultApi20 api;
  private final DPoPProofCreator dpopProofCreator;

  public OAuth20RequestSigner(DefaultApi20 api, DPoPProofCreator dpopProofCreator) {
    this.api = api;
    this.dpopProofCreator = dpopProofCreator;
  }

  public void signRequest(String accessToken, OAuthRequest request) {
    if (dpopProofCreator != null) {
      request.setDPoPProof(dpopProofCreator.createDPoPProof(request, accessToken));
    }
    api.getBearerSignature().signRequest(accessToken, request);
  }

  public void signRequest(OAuth2AccessToken accessToken, OAuthRequest request) {
    signRequest(accessToken == null ? null : accessToken.getAccessToken(), request);
  }
}
