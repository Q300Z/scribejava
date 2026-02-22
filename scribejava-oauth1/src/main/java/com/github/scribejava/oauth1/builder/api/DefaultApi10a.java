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
package com.github.scribejava.oauth1.builder.api;

import com.github.scribejava.core.extractors.*;
import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.httpclient.HttpClientConfig;
import com.github.scribejava.core.model.OAuthConstants;
import com.github.scribejava.core.model.ParameterList;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.services.SignatureService;
import com.github.scribejava.core.services.TimestampService;
import com.github.scribejava.core.services.TimestampServiceImpl;
import com.github.scribejava.oauth1.extractors.OAuth1AccessTokenExtractor;
import com.github.scribejava.oauth1.extractors.OAuth1RequestTokenExtractor;
import com.github.scribejava.oauth1.model.OAuth1AccessToken;
import com.github.scribejava.oauth1.model.OAuth1RequestToken;
import com.github.scribejava.oauth1.oauth.OAuth10aService;
import com.github.scribejava.oauth1.services.HMACSha1SignatureService;
import java.io.OutputStream;

/**
 * Implémentation par défaut du protocole OAuth, version 1.0a.
 *
 * <p>Cette classe doit être étendue par les implémentations concrètes d'API, fournissant les points
 * de terminaison et les verbes HTTP correspondants. Elle définit également les extracteurs et
 * services de signature par défaut.
 *
 * @see <a href="https://tools.ietf.org/html/rfc5849">RFC 5849 (The OAuth 1.0 Protocol)</a>
 */
public abstract class DefaultApi10a {

  /**
   * Retourne l'extracteur de jeton d'accès (Access Token).
   *
   * @return L'instance de {@link TokenExtractor} pour {@link OAuth1AccessToken}.
   */
  public TokenExtractor<OAuth1AccessToken> getAccessTokenExtractor() {
    return OAuth1AccessTokenExtractor.instance();
  }

  /**
   * Retourne l'extracteur de chaîne de base pour la signature.
   *
   * @return L'instance de {@link BaseStringExtractor}.
   */
  public BaseStringExtractor getBaseStringExtractor() {
    return new BaseStringExtractorImpl();
  }

  /**
   * Retourne l'extracteur d'en-tête d'autorisation.
   *
   * @return L'instance de {@link HeaderExtractor}.
   */
  public HeaderExtractor getHeaderExtractor() {
    return new HeaderExtractorImpl();
  }

  /**
   * Retourne l'extracteur de jeton de requête (Request Token).
   *
   * @return L'instance de {@link TokenExtractor} pour {@link OAuth1RequestToken}.
   */
  public TokenExtractor<OAuth1RequestToken> getRequestTokenExtractor() {
    return OAuth1RequestTokenExtractor.instance();
  }

  /**
   * Retourne le service de signature (HMAC-SHA1 par défaut).
   *
   * @return L'instance de {@link SignatureService}.
   */
  public SignatureService getSignatureService() {
    return new HMACSha1SignatureService();
  }

  /**
   * Retourne le type de signature utilisé (En-tête par défaut).
   *
   * @return Le {@link OAuth1SignatureType} souhaité.
   */
  public OAuth1SignatureType getSignatureType() {
    return OAuth1SignatureType.HEADER;
  }

  /**
   * Retourne le service de marquage temporel (Timestamp).
   *
   * @return L'instance de {@link TimestampService}.
   */
  public TimestampService getTimestampService() {
    return new TimestampServiceImpl();
  }

  /**
   * Retourne le verbe HTTP pour le point de terminaison de jeton d'accès (POST par défaut).
   *
   * @return Le verbe {@link Verb}.
   */
  public Verb getAccessTokenVerb() {
    return Verb.POST;
  }

  /**
   * Retourne le verbe HTTP pour le point de terminaison de jeton de requête (POST par défaut).
   *
   * @return Le verbe {@link Verb}.
   */
  public Verb getRequestTokenVerb() {
    return Verb.POST;
  }

  /**
   * Retourne l'URL pour l'obtention du jeton de requête (Request Token).
   *
   * @return L'URL du point de terminaison.
   */
  public abstract String getRequestTokenEndpoint();

  /**
   * Retourne l'URL pour l'obtention du jeton d'accès (Access Token).
   *
   * @return L'URL du point de terminaison.
   */
  public abstract String getAccessTokenEndpoint();

  /** @return L'URL de base pour l'autorisation de l'utilisateur. */
  protected abstract String getAuthorizationBaseUrl();

  /**
   * Retourne l'URL vers laquelle rediriger l'utilisateur pour autoriser l'application.
   *
   * @param requestToken Le jeton de requête à autoriser.
   * @return L'URL d'autorisation complète.
   */
  public String getAuthorizationUrl(OAuth1RequestToken requestToken) {
    final ParameterList parameters = new ParameterList();
    parameters.add(OAuthConstants.TOKEN, requestToken.getToken());
    return parameters.appendTo(getAuthorizationBaseUrl());
  }

  /**
   * Crée l'instance de service OAuth 1.0a.
   *
   * @param apiKey Clé API.
   * @param apiSecret Secret API.
   * @param callback URL de rappel.
   * @param scope Portée optionnelle.
   * @param debugStream Flux de débogage.
   * @param userAgent User-Agent.
   * @param httpClientConfig Configuration HTTP.
   * @param httpClient Client HTTP.
   * @return Une instance de {@link OAuth10aService}.
   */
  public OAuth10aService createService(
      String apiKey,
      String apiSecret,
      String callback,
      String scope,
      OutputStream debugStream,
      String userAgent,
      HttpClientConfig httpClientConfig,
      HttpClient httpClient) {
    return new OAuth10aService(
        this,
        apiKey,
        apiSecret,
        callback,
        scope,
        debugStream,
        userAgent,
        httpClientConfig,
        httpClient);
  }

  /**
   * Indique si le paramètre "oauth_token" vide est obligatoire dans la requête.
   *
   * <p>La RFC 5849 indique que ce paramètre peut être omis s'il est vide, mais certains serveurs
   * l'exigent.
   *
   * @return true si le paramètre doit être inclus même s'il est vide.
   * @see <a href="https://tools.ietf.org/html/rfc5849#section-3.5">RFC 5849, Section 3.5</a>
   */
  public boolean isEmptyOAuthTokenParamIsRequired() {
    return false;
  }
}
