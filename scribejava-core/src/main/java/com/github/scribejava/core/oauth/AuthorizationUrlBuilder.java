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

/**
 * Constructeur fluide pour les URLs d'autorisation OAuth 2.0.
 *
 * <p>Permet de configurer les différents paramètres de la requête d'autorisation (state, scope,
 * PKCE, PAR) de manière chaînée avant de générer l'URL finale.
 */
public class AuthorizationUrlBuilder {

  private final OAuth20Service oauth20Service;

  private String state;
  private Map<String, String> additionalParams;
  private PKCE pkce;
  private String scope;
  private boolean usePushedAuthorizationRequests;

  /**
   * Constructeur.
   *
   * @param oauth20Service Le service OAuth 2.0 associé.
   */
  public AuthorizationUrlBuilder(OAuth20Service oauth20Service) {
    this.oauth20Service = oauth20Service;
  }

  /**
   * Définit le paramètre d'état (state).
   *
   * @param state Valeur opaque pour la protection CSRF.
   * @return L'instance actuelle du builder.
   */
  public AuthorizationUrlBuilder state(String state) {
    this.state = state;
    return this;
  }

  /**
   * Ajoute des paramètres supplémentaires à la requête.
   *
   * @param additionalParams Dictionnaire de paramètres additionnels.
   * @return L'instance actuelle du builder.
   */
  public AuthorizationUrlBuilder additionalParams(Map<String, String> additionalParams) {
    this.additionalParams = additionalParams;
    return this;
  }

  /**
   * Définit l'objet PKCE à utiliser.
   *
   * @param pkce L'instance {@link PKCE}.
   * @return L'instance actuelle du builder.
   * @see <a href="https://tools.ietf.org/html/rfc7636">RFC 7636 (PKCE)</a>
   */
  public AuthorizationUrlBuilder pkce(PKCE pkce) {
    this.pkce = pkce;
    return this;
  }

  /**
   * Génère automatiquement une nouvelle paire de clés PKCE.
   *
   * @return L'instance actuelle du builder.
   */
  public AuthorizationUrlBuilder initPKCE() {
    this.pkce = PKCEService.defaultInstance().generatePKCE();
    return this;
  }

  /**
   * Définit la portée (scope) pour cette requête spécifique.
   *
   * @param scope La portée demandée.
   * @return L'instance actuelle du builder.
   */
  public AuthorizationUrlBuilder scope(String scope) {
    this.scope = scope;
    return this;
  }

  /**
   * Active l'utilisation des requêtes d'autorisation poussées (PAR).
   *
   * @return L'instance actuelle du builder.
   * @see <a href="https://tools.ietf.org/html/rfc9126">RFC 9126 (PAR)</a>
   */
  public AuthorizationUrlBuilder usePushedAuthorizationRequests() {
    this.usePushedAuthorizationRequests = true;
    return this;
  }

  /** @return L'objet PKCE configuré, ou null. */
  public PKCE getPkce() {
    return pkce;
  }

  /**
   * Construit l'URL d'autorisation finale.
   *
   * <p>Si PAR est activé, une requête POST est envoyée au serveur et l'URL contiendra un {@code
   * request_uri}. Sinon, l'URL contiendra tous les paramètres en clair (ou transformés par JAR si
   * configuré).
   *
   * @return L'URL d'autorisation prête à être utilisée pour la redirection.
   * @throws OAuthException en cas d'erreur lors de l'appel PAR.
   */
  public String build() {
    if (pkce == null) {
      initPKCE();
    }

    final Map<String, String> authorizationParams =
        additionalParams == null ? new HashMap<>() : new HashMap<>(additionalParams);

    if (pkce != null) {
      authorizationParams.putAll(pkce.getAuthorizationUrlParams());
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
                    authorizationParams)
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
      // 1. Collect all parameters
      final ParameterList parameters = new ParameterList(authorizationParams);
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

      // 2. Apply strategy (JAR, etc.)
      final Map<String, String> convertedParams =
          oauth20Service.getAuthorizationRequestConverter().convert(parameters.asMap());

      // 3. Rebuild ParameterList
      final ParameterList finalParameters = new ParameterList(convertedParams);

      return finalParameters.appendTo(oauth20Service.getApi().getAuthorizationBaseUrl());
    }
  }
}
