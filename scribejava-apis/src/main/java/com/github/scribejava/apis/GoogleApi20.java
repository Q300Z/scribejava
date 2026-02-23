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
package com.github.scribejava.apis;

import com.github.scribejava.apis.google.GoogleDeviceAuthorizationJsonExtractor;
import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.extractors.DeviceAuthorizationJsonExtractor;

/**
 * API Google OAuth 2.0 avec support OpenID Connect.
 *
 * @see <a href="https://developers.google.com/identity/protocols/oauth2">Google OAuth 2.0
 * Documentation</a>
 */
public class GoogleApi20 extends DefaultApi20 {

    /**
     * Constructeur protégé.
     */
    protected GoogleApi20() {
    }

    /**
     * Retourne l'instance unique (singleton) de l'API Google.
     *
     * @return L'instance de {@link GoogleApi20}.
     */
    public static GoogleApi20 instance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public String getAccessTokenEndpoint() {
        return "https://oauth2.googleapis.com/token";
    }

    @Override
    public String getAuthorizationBaseUrl() {
        return "https://accounts.google.com/o/oauth2/v2/auth";
    }

    @Override
    public String getRevokeTokenEndpoint() {
        return "https://oauth2.googleapis.com/revoke";
    }

    @Override
    public DeviceAuthorizationJsonExtractor getDeviceAuthorizationExtractor() {
        return GoogleDeviceAuthorizationJsonExtractor.instance();
    }

    @Override
    public String getDeviceAuthorizationEndpoint() {
        return "https://oauth2.googleapis.com/device/code";
    }

    private static class InstanceHolder {
        private static final GoogleApi20 INSTANCE = new GoogleApi20();
    }
}
