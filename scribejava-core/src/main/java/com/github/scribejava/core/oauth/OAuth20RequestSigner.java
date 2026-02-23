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

/**
 * Responsable de la signature des requêtes OAuth 2.0, incluant la création de preuves DPoP.
 *
 * <p>Cette classe applique les mécanismes de signature configurés pour sécuriser l'accès aux
 * ressources.
 *
 * @see <a href="https://tools.ietf.org/html/rfc6750">RFC 6750 (Bearer Token)</a>
 * @see <a href="https://tools.ietf.org/html/rfc9449">RFC 9449 (DPoP)</a>
 */
public class OAuth20RequestSigner {

  private final DefaultApi20 api;
  private final DPoPProofCreator dpopProofCreator;

  /**
   * Constructeur.
   *
   * @param api L'implémentation de l'API.
   * @param dpopProofCreator Le créateur de preuves DPoP (optionnel).
   */
  public OAuth20RequestSigner(DefaultApi20 api, DPoPProofCreator dpopProofCreator) {
    this.api = api;
    this.dpopProofCreator = dpopProofCreator;
  }

  /**
   * Signe une requête avec un jeton d'accès brut.
   *
   * @param accessToken Le jeton d'accès.
   * @param request La requête à signer.
   */
  public void signRequest(String accessToken, OAuthRequest request) {
    if (dpopProofCreator != null) {
      request.setDPoPProof(dpopProofCreator.createDPoPProof(request, accessToken));
    }
    api.getBearerSignature().signRequest(accessToken, request);
  }

  /**
   * Signe une requête avec un objet jeton d'accès.
   *
   * @param accessToken L'objet {@link OAuth2AccessToken}.
   * @param request La requête à signer.
   */
  public void signRequest(OAuth2AccessToken accessToken, OAuthRequest request) {
    signRequest(accessToken == null ? null : accessToken.getAccessToken(), request);
  }
}
