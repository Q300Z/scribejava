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
package com.github.scribejava.core.integration;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.oauth.OAuth20Service;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * Service de haut niveau permettant d'exécuter des requêtes authentifiées sans gérer manuellement
 * le cycle de vie du jeton.
 *
 * @param <K> Type de la clé d'identification de l'utilisateur.
 */
public class AuthorizedClientService<K> {

  private final OAuth20Service oauthService;
  private final TokenAutoRenewer<K> renewer;

  /**
   * @param oauthService service
   * @param renewer renewer
   */
  public AuthorizedClientService(OAuth20Service oauthService, TokenAutoRenewer<K> renewer) {
    this.oauthService = Objects.requireNonNull(oauthService);
    this.renewer = Objects.requireNonNull(renewer);
  }

  /**
   * @param key key
   * @param request request
   * @return response
   * @throws IOException IOException
   * @throws InterruptedException InterruptedException
   * @throws ExecutionException ExecutionException
   */
  public Response execute(K key, OAuthRequest request)
      throws IOException, InterruptedException, ExecutionException {
    final OAuth2AccessToken token = renewer.getValidToken(key);
    oauthService.signRequest(token, request);
    return oauthService.execute(request);
  }

  /**
   * @return oauthService
   */
  public OAuth20Service getOauthService() {
    return oauthService;
  }

  /**
   * @return renewer
   */
  public TokenAutoRenewer<K> getRenewer() {
    return renewer;
  }
}
