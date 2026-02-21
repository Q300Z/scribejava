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

public class JDKHttpClientConfig implements HttpClientConfig {

  private Integer connectTimeout;
  private Integer readTimeout;
  private boolean followRedirects = true;
  private Proxy proxy;
  private javax.net.ssl.SSLSocketFactory sslSocketFactory;

  public static JDKHttpClientConfig defaultConfig() {
    return new JDKHttpClientConfig();
  }

  @Override
  public JDKHttpClientConfig createDefaultConfig() {
    return defaultConfig();
  }

  public javax.net.ssl.SSLSocketFactory getSslSocketFactory() {
    return sslSocketFactory;
  }

  public void setSslSocketFactory(javax.net.ssl.SSLSocketFactory sslSocketFactory) {
    this.sslSocketFactory = sslSocketFactory;
  }

  public JDKHttpClientConfig withSslSocketFactory(javax.net.ssl.SSLSocketFactory sslSocketFactory) {
    this.sslSocketFactory = sslSocketFactory;
    return this;
  }

  public Integer getConnectTimeout() {
    return connectTimeout;
  }

  public void setConnectTimeout(Integer connectTimeout) {
    this.connectTimeout = connectTimeout;
  }

  public JDKHttpClientConfig withConnectTimeout(Integer connectTimeout) {
    this.connectTimeout = connectTimeout;
    return this;
  }

  public Integer getReadTimeout() {
    return readTimeout;
  }

  public void setReadTimeout(Integer readTimeout) {
    this.readTimeout = readTimeout;
  }

  public JDKHttpClientConfig withReadTimeout(Integer readTimeout) {
    this.readTimeout = readTimeout;
    return this;
  }

  public Proxy getProxy() {
    return proxy;
  }

  public void setProxy(Proxy proxy) {
    this.proxy = proxy;
  }

  public JDKHttpClientConfig withProxy(Proxy proxy) {
    this.proxy = proxy;
    return this;
  }

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

  /**
   * Sets whether the underlying Http Connection follows redirects or not.
   *
   * <p>Defaults to true (follow redirects)
   *
   * @param followRedirects boolean
   * @return this for chaining methods invocations
   * @see <a
   *     href="http://docs.oracle.com/javase/6/docs/api/java/net/HttpURLConnection.html#setInstanceFollowRedirects(boolean)">http://docs.oracle.com/javase/6/docs/api/java/net/HttpURLConnection.html#setInstanceFollowRedirects(boolean)</a>
   */
  public JDKHttpClientConfig withFollowRedirects(boolean followRedirects) {
    this.followRedirects = followRedirects;
    return this;
  }
}
