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
package com.github.scribejava.core.httpclient.jdk;

import com.github.scribejava.core.httpclient.HttpClientConfig;
import java.net.Proxy;

/** Configuration pour le client HTTP par défaut du JDK (HttpURLConnection). */
public class JDKHttpClientConfig implements HttpClientConfig {

  private Integer connectTimeout;
  private Integer readTimeout;
  private boolean followRedirects = true;
  private Proxy proxy;
  private javax.net.ssl.SSLSocketFactory sslSocketFactory;
  private javax.net.ssl.HostnameVerifier hostnameVerifier;

  /**
   * Crée une configuration par défaut.
   *
   * @return Une instance de {@link JDKHttpClientConfig}.
   */
  public static JDKHttpClientConfig defaultConfig() {
    return new JDKHttpClientConfig();
  }

  /**
   * Retourne la fabrique de sockets SSL.
   *
   * @return La {@link javax.net.ssl.SSLSocketFactory}.
   */
  public javax.net.ssl.SSLSocketFactory getSslSocketFactory() {
    return sslSocketFactory;
  }

  /**
   * Retourne le vérificateur de nom d'hôte (HostnameVerifier).
   *
   * @return Le {@link javax.net.ssl.HostnameVerifier}.
   */
  public javax.net.ssl.HostnameVerifier getHostnameVerifier() {
    return hostnameVerifier;
  }

  /**
   * Définit le vérificateur de nom d'hôte (version fluide).
   *
   * @param hostnameVerifier Le vérificateur.
   * @return L'instance de configuration.
   */
  public JDKHttpClientConfig withHostnameVerifier(javax.net.ssl.HostnameVerifier hostnameVerifier) {
    this.hostnameVerifier = hostnameVerifier;
    return this;
  }

  /**
   * Retourne le délai d'attente de connexion.
   *
   * @return Le délai en millisecondes.
   */
  public Integer getConnectTimeout() {
    return connectTimeout;
  }

  /**
   * Définit le délai d'attente de connexion.
   *
   * @param connectTimeout Le délai en millisecondes.
   */
  public void setConnectTimeout(Integer connectTimeout) {
    this.connectTimeout = connectTimeout;
  }

  /**
   * Retourne le délai d'attente de lecture.
   *
   * @return Le délai en millisecondes.
   */
  public Integer getReadTimeout() {
    return readTimeout;
  }

  /**
   * Définit le délai d'attente de lecture.
   *
   * @param readTimeout Le délai en millisecondes.
   */
  public void setReadTimeout(Integer readTimeout) {
    this.readTimeout = readTimeout;
  }

  /**
   * Retourne le proxy configuré.
   *
   * @return Le {@link Proxy}.
   */
  public Proxy getProxy() {
    return proxy;
  }

  /**
   * Définit le proxy.
   *
   * @param proxy Le proxy à utiliser.
   */
  public void setProxy(Proxy proxy) {
    this.proxy = proxy;
  }

  /**
   * Indique si les redirections doivent être suivies.
   *
   * @return true si activé, false sinon.
   */
  public boolean isFollowRedirects() {
    return followRedirects;
  }

  /**
   * Sets whether the underlying Http Connection follows redirects or not.
   *
   * <p>Defaults to true (follow redirects)
   *
   * @param followRedirects boolean
   * @see <a
   *     href="http://docs.oracle.com/javase/6/docs/api/java/net/HttpURLConnection.html#setInstanceFollowRedirects(boolean)">http://docs.oracle.com/javase/6/docs/api/java/net/HttpURLConnection.html#setInstanceFollowRedirects(boolean)</a>
   */
  public void setFollowRedirects(boolean followRedirects) {
    this.followRedirects = followRedirects;
  }
}
