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
package com.github.scribejava.core.oauth2.grant;

import com.github.scribejava.core.model.OAuthConstants;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.oauth.OAuth20Service;

/**
 * Représente la concession de type "Device Code" (code d'appareil).
 *
 * <p>Ce type de concession est une extension permettant aux appareils connectés à Internet, mais
 * dépourvus de navigateur web ou ayant des capacités de saisie limitées, d'obtenir des jetons
 * d'accès.
 *
 * @see <a href="https://tools.ietf.org/html/rfc6749#section-4.5">RFC 6749, Section 4.5 (Extension
 * Grants)</a>
 */
public class DeviceCodeGrant implements OAuth20Grant {

    private final String deviceCode;

    /**
     * Constructeur.
     *
     * @param deviceCode Le code appareil obtenu lors de l'étape d'autorisation initiale.
     */
    public DeviceCodeGrant(String deviceCode) {
        this.deviceCode = deviceCode;
    }

    @Override
    public OAuthRequest createRequest(OAuth20Service service) {
        final OAuthRequest request =
                new OAuthRequest(
                        service.getApi().getAccessTokenVerb(), service.getApi().getAccessTokenEndpoint());
        request.addParameter(OAuthConstants.GRANT_TYPE, "urn:ietf:params:oauth:grant-type:device_code");
        request.addParameter("device_code", deviceCode);
        service
                .getApi()
                .getClientAuthentication()
                .addClientAuthentication(request, service.getApiKey(), service.getApiSecret());
        return request;
    }
}
