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
package com.github.scribejava.core.builder;

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.httpclient.HttpClientConfig;
import com.github.scribejava.core.oauth.OAuth20Service;

/**
 * Interface pour le constructeur de services OAuth 2.0.
 *
 * <p>Fournit des méthodes fluides pour configurer et construire une instance de {@link
 * OAuth20Service}.
 */
public interface ServiceBuilderOAuth20 extends ServiceBuilderCommon {

  ServiceBuilderOAuth20 callback(String callback);

  ServiceBuilderOAuth20 httpClientConfig(HttpClientConfig httpClientConfig);

  ServiceBuilderOAuth20 userAgent(String userAgent);

  ServiceBuilderOAuth20 debug();

  /**
   * Définit le type de réponse (response_type).
   *
   * @param responseType Le type de réponse (ex: "code").
   * @return L'instance du constructeur.
   */
  ServiceBuilderOAuth20 responseType(String responseType);

  /**
   * Définit les portées (scopes) de manière fluide.
   *
   * @param scopes Liste des portées.
   * @return L'instance du constructeur.
   */
  ServiceBuilderOAuth20 scopes(String... scopes);

  /**
   * Construit le service OAuth 2.0.
   *
   * @param api L'instance de l'API.
   * @return Une nouvelle instance de {@link OAuth20Service}.
   */
  OAuth20Service build(DefaultApi20 api);
}
