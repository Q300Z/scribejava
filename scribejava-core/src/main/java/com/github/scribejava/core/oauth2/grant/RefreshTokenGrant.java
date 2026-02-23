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
package com.github.scribejava.core.oauth2.grant;

import com.github.scribejava.core.model.OAuthConstants;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.oauth.OAuth20Service;

/**
 * Représente la concession de type "Refresh Token" (jeton de renouvellement).
 *
 * <p>Les jetons de renouvellement sont des identifiants utilisés pour obtenir de nouveaux jetons
 * d'accès lorsque le jeton d'accès actuel devient invalide ou expire, ou pour obtenir des jetons
 * d'accès additionnels.
 *
 * @see <a href="https://tools.ietf.org/html/rfc6749#section-1.5">RFC 6749, Section 1.5 (Refresh
 *     Token)</a>
 * @see <a href="https://tools.ietf.org/html/rfc6749#section-6">RFC 6749, Section 6 (Refreshing an
 *     Access Token)</a>
 */
public class RefreshTokenGrant implements OAuth20Grant {

  private final String refreshToken;
  private final String scope;

  /**
   * Constructeur simple.
   *
   * @param refreshToken Le jeton de renouvellement délivré au client.
   */
  public RefreshTokenGrant(String refreshToken) {
    this(refreshToken, null);
  }

  /**
   * Constructeur avec portée (scope) spécifique.
   *
   * @param refreshToken Le jeton de renouvellement délivré au client.
   * @param scope La portée de la demande d'accès (doit être identique ou plus restreinte que celle
   *     d'origine).
   */
  public RefreshTokenGrant(String refreshToken, String scope) {
    this.refreshToken = refreshToken;
    this.scope = scope;
  }

  @Override
  public OAuthRequest createRequest(OAuth20Service service) {
    final OAuthRequest request =
        new OAuthRequest(
            service.getApi().getAccessTokenVerb(), service.getApi().getAccessTokenEndpoint());

    service
        .getApi()
        .getClientAuthentication()
        .addClientAuthentication(request, service.getApiKey(), service.getApiSecret());

    request.addParameter(OAuthConstants.REFRESH_TOKEN, refreshToken);

    if (scope != null) {
      request.addParameter(OAuthConstants.SCOPE, scope);
    } else if (service.getDefaultScope() != null) {
      request.addParameter(OAuthConstants.SCOPE, service.getDefaultScope());
    }

    request.addParameter(OAuthConstants.GRANT_TYPE, OAuthConstants.REFRESH_TOKEN);

    return request;
  }
}
