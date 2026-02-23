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
package com.github.scribejava.apis.google;

import com.github.scribejava.core.extractors.DeviceAuthorizationJsonExtractor;

/**
 * Extracteur JSON pour la réponse d'autorisation d'appareil (Device Authorization) de Google.
 *
 * @see <a href="https://tools.ietf.org/html/rfc8628">RFC 8628</a>
 */
public class GoogleDeviceAuthorizationJsonExtractor extends DeviceAuthorizationJsonExtractor {

  /** Constructeur protégé. */
  protected GoogleDeviceAuthorizationJsonExtractor() {}

  /**
   * Retourne l'instance unique (singleton) de l'extracteur.
   *
   * @return L'instance de {@link GoogleDeviceAuthorizationJsonExtractor}.
   */
  public static GoogleDeviceAuthorizationJsonExtractor instance() {
    return GoogleDeviceAuthorizationJsonExtractor.InstanceHolder.INSTANCE;
  }

  @Override
  protected String getVerificationUriParamName() {
    return "verification_url";
  }

  private static class InstanceHolder {

    private static final GoogleDeviceAuthorizationJsonExtractor INSTANCE =
        new GoogleDeviceAuthorizationJsonExtractor();
  }
}
