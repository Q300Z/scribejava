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

import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.httpclient.HttpClientConfig;
import java.io.OutputStream;

/**
 * Implementation of the Builder pattern, with a fluent interface that creates a {@link
 * com.github.scribejava.core.oauth.OAuthService}
 */
public interface ServiceBuilderCommon {

  /**
   * Adds an OAuth callback url
   *
   * @param callback callback url. Must be a valid url or 'oob' ({@link
   *     com.github.scribejava.core.model.OAuthConstants#OOB} for out of band OAuth
   * @return the {@link ServiceBuilder} instance for method chaining
   */
  ServiceBuilderCommon callback(String callback);

  /**
   * Configures the api key
   *
   * @param apiKey The api key for your application
   * @return the {@link ServiceBuilder} instance for method chaining
   */
  ServiceBuilderCommon apiKey(String apiKey);

  /**
   * Configures the api secret
   *
   * @param apiSecret The api secret for your application
   * @return the {@link ServiceBuilder} instance for method chaining
   */
  ServiceBuilderCommon apiSecret(String apiSecret);

  /**
   * Configures the api secret as "" (empty string).
   *
   * <p>Used usually for a test environments or another strange cases. Not all providers support
   * empty string as api key and will throw an Exception in such cases.
   *
   * @return the {@link ServiceBuilder} instance for method chaining
   */
  ServiceBuilderCommon apiSecretIsEmptyStringUnsafe();

  /**
   * Définit la configuration du client HTTP.
   *
   * @param httpClientConfig La configuration à utiliser.
   * @return L'instance du constructeur.
   */
  ServiceBuilderCommon httpClientConfig(HttpClientConfig httpClientConfig);

  /**
   * takes precedence over httpClientConfig
   *
   * @param httpClient externally created HTTP client
   * @return the {@link ServiceBuilder} instance for method chaining
   */
  ServiceBuilderCommon httpClient(HttpClient httpClient);

  /**
   * Définit la chaîne User-Agent à utiliser pour les requêtes HTTP.
   *
   * @param userAgent La chaîne User-Agent.
   * @return L'instance du constructeur.
   */
  ServiceBuilderCommon userAgent(String userAgent);

  /**
   * Définit le flux de sortie pour le débogage.
   *
   * @param debugStream Le flux {@link OutputStream}.
   * @return L'instance du constructeur.
   */
  ServiceBuilderCommon debugStream(OutputStream debugStream);

  /**
   * Active le mode débogage en utilisant {@code System.out}.
   *
   * @return L'instance du constructeur.
   */
  ServiceBuilderCommon debug();
}
