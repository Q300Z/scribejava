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
package com.github.scribejava.core.oauth;

import com.github.scribejava.core.model.DeviceAuthorization;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuth2AccessTokenErrorResponse;
import com.github.scribejava.core.model.OAuthConstants;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth2.OAuth2Error;
import com.github.scribejava.core.oauth2.grant.DeviceCodeGrant;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Gère le flux de concession d'autorisation pour les appareils (Device Authorization Grant).
 */
public class OAuth20DeviceFlowHandler {

    private final OAuth20Service service;

    /**
     * @param service service
     */
    public OAuth20DeviceFlowHandler(OAuth20Service service) {
        this.service = service;
    }

    /**
     * @param scope scope
     * @return request
     */
    public OAuthRequest createDeviceAuthorizationCodesRequest(String scope) {
        final OAuthRequest request = new OAuthRequest(Verb.POST, service.getApi().getDeviceAuthorizationEndpoint());
        request.addParameter(OAuthConstants.CLIENT_ID, service.getApiKey());
        final String effectiveScope = scope != null ? scope : service.getDefaultScope();
        if (effectiveScope != null) {
            request.addParameter(OAuthConstants.SCOPE, effectiveScope);
        }
        return request;
    }

    /**
     * @param scope scope
     * @return codes
     * @throws InterruptedException InterruptedException
     * @throws ExecutionException ExecutionException
     * @throws IOException IOException
     */
    public DeviceAuthorization getDeviceAuthorizationCodes(String scope)
            throws InterruptedException, ExecutionException, IOException {
        final OAuthRequest request = createDeviceAuthorizationCodesRequest(scope);
        try (Response response = service.execute(request)) {
            return service.getApi().getDeviceAuthorizationExtractor().extract(response);
        }
    }

    /**
     * @param deviceAuthorization deviceAuthorization
     * @return token
     * @throws InterruptedException InterruptedException
     * @throws ExecutionException ExecutionException
     * @throws IOException IOException
     */
    public OAuth2AccessToken pollAccessTokenDeviceAuthorizationGrant(DeviceAuthorization deviceAuthorization)
            throws InterruptedException, ExecutionException, IOException {
        long intervalMillis = deviceAuthorization.getIntervalSeconds() * 1000;
        while (true) {
            try {
                return service.getAccessTokenAsync(new DeviceCodeGrant(deviceAuthorization.getDeviceCode())).get();
            } catch (ExecutionException e) {
                final Throwable cause = e.getCause();
                if (cause instanceof OAuth2AccessTokenErrorResponse) {
                    final OAuth2AccessTokenErrorResponse errorResponse = (OAuth2AccessTokenErrorResponse) cause;
                    if (errorResponse.getError() != OAuth2Error.AUTHORIZATION_PENDING) {
                        if (errorResponse.getError() == OAuth2Error.SLOW_DOWN) {
                            intervalMillis += 5000;
                        } else {
                            throw errorResponse;
                        }
                    }
                } else if (cause instanceof IOException) {
                    throw (IOException) cause;
                } else if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                } else {
                    throw e;
                }
            }
            Thread.sleep(intervalMillis);
        }
    }
}
