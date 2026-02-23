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
package com.github.scribejava.httpclient.armeria;

import com.github.scribejava.core.httpclient.HttpClientConfig;
import com.linecorp.armeria.client.ClientFactory;
import com.linecorp.armeria.client.ClientOptions;
import com.linecorp.armeria.client.HttpClient;
import com.linecorp.armeria.client.logging.LoggingClient;
import com.linecorp.armeria.client.retry.RetryingClient;
import com.linecorp.armeria.common.SessionProtocol;
import java.util.function.Function;

/** Configuration pour le client HTTP Armeria. */
public class ArmeriaHttpClientConfig implements HttpClientConfig {

  private static final SessionProtocol DEFAULT_PROTOCOL_PREFERENCE = SessionProtocol.H1; // H1 or H2

  private final ClientOptions clientOptions;
  private final ClientFactory clientFactory;
  private SessionProtocol protocolPreference;
  private Function<? super HttpClient, RetryingClient> retry;
  private Function<? super HttpClient, LoggingClient> logging;

  /**
   * Creates new {@link ArmeriaHttpClientConfig} using provided {@link ClientOptions} and {@link
   * ClientFactory}.
   *
   * @param clientOptions clientOptions
   * @param clientFactory clientFactory
   */
  public ArmeriaHttpClientConfig(ClientOptions clientOptions, ClientFactory clientFactory) {
    this.clientOptions = clientOptions;
    this.clientFactory = clientFactory;
    protocolPreference = DEFAULT_PROTOCOL_PREFERENCE;
  }

  /**
   * Creates new {@link ArmeriaHttpClientConfig} using default settings.
   *
   * @return ArmeriaHttpClientConfig
   */
  public static ArmeriaHttpClientConfig defaultConfig() {
    return new ArmeriaHttpClientConfig(null, null);
  }

  /**
   * Définit la fonction de réessai (version fluide).
   *
   * @param retry La fonction de réessai.
   * @return L'instance de configuration.
   */
  public ArmeriaHttpClientConfig withRetry(Function<? super HttpClient, RetryingClient> retry) {
    this.retry = retry;
    return this;
  }

  /**
   * Définit la fonction de journalisation (logging).
   *
   * @param logging La fonction de journalisation.
   */
  public void setLogging(Function<? super HttpClient, LoggingClient> logging) {
    this.logging = logging;
  }

  ArmeriaWebClientBuilder createClientBuilder() {
    return new ArmeriaWebClientBuilder(
        clientOptions, clientFactory, protocolPreference, retry, logging);
  }
}
