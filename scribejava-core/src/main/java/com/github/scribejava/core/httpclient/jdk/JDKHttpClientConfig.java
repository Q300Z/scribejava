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
     * <p>
     * Defaults to true (follow redirects)
     *
     * @param followRedirects boolean
     * @see <a
     * href="http://docs.oracle.com/javase/6/docs/api/java/net/HttpURLConnection.html#setInstanceFollowRedirects(boolean)">http://docs.oracle.com/javase/6/docs/api/java/net/HttpURLConnection.html#setInstanceFollowRedirects(boolean)</a>
     */
    public void setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
    }

    /**
     * Sets whether the underlying Http Connection follows redirects or not.
     * <p>
     * Defaults to true (follow redirects)
     *
     * @param followRedirects boolean
     * @return this for chaining methods invocations
     * @see <a
     * href="http://docs.oracle.com/javase/6/docs/api/java/net/HttpURLConnection.html#setInstanceFollowRedirects(boolean)">http://docs.oracle.com/javase/6/docs/api/java/net/HttpURLConnection.html#setInstanceFollowRedirects(boolean)</a>
     */
    public JDKHttpClientConfig withFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
        return this;
    }
}
