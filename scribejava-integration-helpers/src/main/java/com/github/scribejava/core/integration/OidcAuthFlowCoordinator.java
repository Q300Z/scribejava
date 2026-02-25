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
import com.github.scribejava.core.oauth2.grant.AuthorizationCodeGrant;
import com.github.scribejava.oidc.IdToken;
import com.github.scribejava.oidc.OidcService;
import com.github.scribejava.oidc.StandardClaims;
import com.github.scribejava.oidc.model.OidcNonce;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Spécialisation OIDC du coordinateur. Automatise la validation du Nonce et de l'ID Token.
 *
 * @param <K> Type de la clé d'identification.
 */
public class OidcAuthFlowCoordinator<K> extends AuthFlowCoordinator<K> {

  protected final OidcService oidcService;

  /**
   * @param oauthService service
   * @param repository repository
   */
  public OidcAuthFlowCoordinator(
      OidcService oauthService, TokenRepository<K, ExpiringTokenWrapper> repository) {
    super(oauthService, repository);
    this.oidcService = oauthService;
  }

  /**
   * Termine le flux OIDC avec validation du contexte de session et fallback UserInfo.
   *
   * @param key clé utilisateur.
   * @param code code d'autorisation.
   * @param receivedState state reçu.
   * @param context contexte de session.
   * @return le résultat de l'authentification.
   * @throws IOException IOException
   * @throws InterruptedException InterruptedException
   * @throws ExecutionException ExecutionException
   */
  public OidcAuthResult finishAuthorization(
      K key, String code, String receivedState, AuthSessionContext context)
      throws IOException, InterruptedException, ExecutionException {

    // 1. Validation CSRF
    if (context.getState() == null || !context.getState().equals(receivedState)) {
      if (getListener() != null) {
        getListener().onCsrfDetected(key, receivedState, context.getState());
      }
      throw new SecurityException("State mismatch!");
    }

    // 2. Échange du code
    final AuthorizationCodeGrant grant = new AuthorizationCodeGrant(code);
    if (context.getPkce() != null) {
      grant.setPkceCodeVerifier(context.getPkce().getCodeVerifier());
    }

    final OAuth2AccessToken token = oidcService.getAccessToken(grant);

    // 3. Validation de l'ID Token et du Nonce
    final OidcNonce expectedNonce =
        context.getNonce() != null ? new OidcNonce(context.getNonce()) : null;
    final IdToken idToken = oidcService.validateIdToken(token, expectedNonce);

    // 4. Extraction des Claims et Fallback UserInfo
    StandardClaims claims = new StandardClaims(idToken.getClaims());
    if (!claims.getEmail().isPresent()) {
      claims = oidcService.getUserInfoAsync(token).get();
    }

    // 5. Persistance
    repository.save(key, new ExpiringTokenWrapper(token));

    return new OidcAuthResult(token, claims);
  }
}
