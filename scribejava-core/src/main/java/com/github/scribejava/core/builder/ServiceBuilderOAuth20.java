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
import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.httpclient.HttpClientConfig;
import com.github.scribejava.core.oauth.OAuth20Service;
import java.io.OutputStream;

public interface ServiceBuilderOAuth20 extends ServiceBuilderCommon {

  @Override
  ServiceBuilderOAuth20 callback(String callback);

  @Override
  ServiceBuilderOAuth20 apiKey(String apiKey);

  @Override
  ServiceBuilderOAuth20 apiSecret(String apiSecret);

  @Override
  ServiceBuilderOAuth20 apiSecretIsEmptyStringUnsafe();

  @Override
  ServiceBuilderOAuth20 httpClientConfig(HttpClientConfig httpClientConfig);

  @Override
  ServiceBuilderOAuth20 httpClient(HttpClient httpClient);

  @Override
  ServiceBuilderOAuth20 userAgent(String userAgent);

  @Override
  ServiceBuilderOAuth20 debugStream(OutputStream debugStream);

  @Override
  ServiceBuilderOAuth20 debug();

  ServiceBuilderOAuth20 responseType(String responseType);

  /**
   * Configures the default OAuth 2.0 scope.<br>
   *
   * <p>You can request any uniq scope per each access token request using {@link
   * com.github.scribejava.core.oauth.AuthorizationUrlBuilder#scope(java.lang.String) }.<br>
   * <br>
   *
   * <p>In case you're requesting always the same scope,<br>
   * you can just set it here and do not provide scope param nowhere more.
   *
   * @param defaultScope The OAuth scope, used as deafult
   * @return the {@link ServiceBuilder} instance for method chaining
   */
  ServiceBuilderOAuth20 defaultScope(String defaultScope);

  ServiceBuilderOAuth20 defaultScope(ScopeBuilder scopeBuilder);

  OAuth20Service build(DefaultApi20 api);
}
