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
package com.github.scribejava.core.oauth2.clientauthentication;

import com.github.scribejava.core.model.OAuthConstants;
import com.github.scribejava.core.model.OAuthRequest;

/**
 * 2.3. Client Authentication<br>
 * 2.3.1. Client Password<br>
 * <a href="https://tools.ietf.org/html/rfc6749#section-2.3.1">...</a> <br>
 * request-body authentication scheme
 */
public class RequestBodyAuthenticationScheme implements ClientAuthentication {

  protected RequestBodyAuthenticationScheme() {}

  /**
   * Retourne l'instance unique (singleton) de ce type d'authentification.
   *
   * @return L'instance de {@link RequestBodyAuthenticationScheme}.
   */
  public static RequestBodyAuthenticationScheme instance() {
    return InstanceHolder.INSTANCE;
  }

  @Override
  public void addClientAuthentication(OAuthRequest request, String apiKey, String apiSecret) {
    request.addParameter(OAuthConstants.CLIENT_ID, apiKey);
    if (apiSecret != null) {
      request.addParameter(OAuthConstants.CLIENT_SECRET, apiSecret);
    }
  }

  private static class InstanceHolder {

    private static final RequestBodyAuthenticationScheme INSTANCE =
        new RequestBodyAuthenticationScheme();
  }
}
