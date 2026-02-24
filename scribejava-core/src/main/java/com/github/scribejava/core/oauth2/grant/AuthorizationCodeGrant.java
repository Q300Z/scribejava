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
import com.github.scribejava.core.pkce.PKCE;
import java.util.HashMap;
import java.util.Map;

/**
 * Représente la concession de type "Authorization Code" (code d'autorisation).
 *
 * <p>Le code d'autorisation est obtenu en utilisant un serveur d'autorisation comme intermédiaire
 * entre le client et le propriétaire de la ressource.
 *
 * @see <a href="https://tools.ietf.org/html/rfc6749#section-1.3.1">RFC 6749, Section 1.3.1
 *     (Authorization Code)</a>
 * @see <a href="https://tools.ietf.org/html/rfc6749#section-4.1">RFC 6749, Section 4.1
 *     (Authorization Code Grant)</a>
 */
public class AuthorizationCodeGrant implements OAuth20Grant {

  private final String code;
  private final Map<String, String> extraParameters = new HashMap<>();
  private String pkceCodeVerifier;

  /**
   * Constructeur.
   *
   * @param code Le code d'autorisation reçu du serveur d'autorisation.
   */
  public AuthorizationCodeGrant(String code) {
    this.code = code;
  }

  /**
   * @return Le code d'autorisation.
   */
  public String getCode() {
    return code;
  }

  /**
   * Définit le vérificateur de code PKCE (code_verifier).
   *
   * @param pkceCodeVerifier La valeur brute du code_verifier.
   * @see <a href="https://tools.ietf.org/html/rfc7636">RFC 7636 (PKCE)</a>
   */
  public void setPkceCodeVerifier(String pkceCodeVerifier) {
    this.pkceCodeVerifier = pkceCodeVerifier;
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

    request.addParameter(OAuthConstants.CODE, code);
    final String callback = service.getCallback();
    if (callback != null) {
      request.addParameter(OAuthConstants.REDIRECT_URI, callback);
    }

    request.addParameter(OAuthConstants.GRANT_TYPE, OAuthConstants.AUTHORIZATION_CODE);

    if (pkceCodeVerifier != null) {
      request.addParameter(PKCE.PKCE_CODE_VERIFIER_PARAM, pkceCodeVerifier);
    }

    for (Map.Entry<String, String> entry : extraParameters.entrySet()) {
      request.addParameter(entry.getKey(), entry.getValue());
    }

    return request;
  }
}
