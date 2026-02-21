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

import com.github.scribejava.core.extractors.OAuth2AccessTokenJsonExtractor;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.revoke.TokenTypeHint;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/** Handles OAuth 2.0 token revocation. */
public class OAuth20RevocationHandler {

  private final OAuth20Service service;

  public OAuth20RevocationHandler(OAuth20Service service) {
    this.service = service;
  }

  public OAuthRequest createRevokeTokenRequest(String tokenToRevoke, TokenTypeHint tokenTypeHint) {
    final OAuthRequest request =
        new OAuthRequest(Verb.POST, service.getApi().getRevokeTokenEndpoint());

    service
        .getApi()
        .getClientAuthentication()
        .addClientAuthentication(request, service.getApiKey(), service.getApiSecret());

    request.addParameter("token", tokenToRevoke);
    if (tokenTypeHint != null) {
      request.addParameter("token_type_hint", tokenTypeHint.getValue());
    }

    return request;
  }

  public void revokeToken(String tokenToRevoke, TokenTypeHint tokenTypeHint)
      throws IOException, InterruptedException, ExecutionException {
    final OAuthRequest request = createRevokeTokenRequest(tokenToRevoke, tokenTypeHint);

    try (Response response = service.execute(request)) {
      checkForError(response);
    }
  }

  public void checkForError(Response response) throws IOException {
    if (response.getCode() != 200) {
      OAuth2AccessTokenJsonExtractor.instance().generateError(response);
    }
  }
}
