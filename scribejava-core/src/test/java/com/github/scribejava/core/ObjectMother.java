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
package com.github.scribejava.core;

import com.github.scribejava.core.model.OAuthConstants;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;

/**
 * Classe utilitaire pour la création d'objets de test.
 */
public abstract class ObjectMother {

    /**
     * Crée une requête OAuth échantillon.
     *
     * @return Une instance de {@link OAuthRequest}.
     */
    public static OAuthRequest createSampleOAuthRequest() {
        final OAuthRequest request = new OAuthRequest(Verb.GET, "http://example.com");
        request.addOAuthParameter(OAuthConstants.TIMESTAMP, "123456");
        request.addOAuthParameter(OAuthConstants.CONSUMER_KEY, "AS#$^*@&");
        request.addOAuthParameter(OAuthConstants.CALLBACK, "http://example/callback");
        request.addOAuthParameter(OAuthConstants.SIGNATURE, "OAuth-Signature");
        return request;
    }

    /**
     * Crée une requête OAuth échantillon sur le port 80.
     *
     * @return Une instance de {@link OAuthRequest}.
     */
    public static OAuthRequest createSampleOAuthRequestPort80() {
        final OAuthRequest request = new OAuthRequest(Verb.GET, "http://example.com:80");
        request.addOAuthParameter(OAuthConstants.TIMESTAMP, "123456");
        request.addOAuthParameter(OAuthConstants.CONSUMER_KEY, "AS#$^*@&");
        request.addOAuthParameter(OAuthConstants.CALLBACK, "http://example/callback");
        request.addOAuthParameter(OAuthConstants.SIGNATURE, "OAuth-Signature");
        return request;
    }

    /**
     * Crée une requête OAuth échantillon sur le port 80 avec un chemin.
     *
     * @return Une instance de {@link OAuthRequest}.
     */
    public static OAuthRequest createSampleOAuthRequestPort80v2() {
        final OAuthRequest request = new OAuthRequest(Verb.GET, "http://example.com:80/test");
        request.addOAuthParameter(OAuthConstants.TIMESTAMP, "123456");
        request.addOAuthParameter(OAuthConstants.CONSUMER_KEY, "AS#$^*@&");
        request.addOAuthParameter(OAuthConstants.CALLBACK, "http://example/callback");
        request.addOAuthParameter(OAuthConstants.SIGNATURE, "OAuth-Signature");
        return request;
    }

    /**
     * Crée une requête OAuth échantillon sur le port 8080.
     *
     * @return Une instance de {@link OAuthRequest}.
     */
    public static OAuthRequest createSampleOAuthRequestPort8080() {
        final OAuthRequest request = new OAuthRequest(Verb.GET, "http://example.com:8080");
        request.addOAuthParameter(OAuthConstants.TIMESTAMP, "123456");
        request.addOAuthParameter(OAuthConstants.CONSUMER_KEY, "AS#$^*@&");
        request.addOAuthParameter(OAuthConstants.CALLBACK, "http://example/callback");
        request.addOAuthParameter(OAuthConstants.SIGNATURE, "OAuth-Signature");
        return request;
    }

    /**
     * Crée une requête OAuth échantillon sur le port 443 (HTTPS).
     *
     * @return Une instance de {@link OAuthRequest}.
     */
    public static OAuthRequest createSampleOAuthRequestPort443() {
        final OAuthRequest request = new OAuthRequest(Verb.GET, "https://example.com:443");
        request.addOAuthParameter(OAuthConstants.TIMESTAMP, "123456");
        request.addOAuthParameter(OAuthConstants.CONSUMER_KEY, "AS#$^*@&");
        request.addOAuthParameter(OAuthConstants.CALLBACK, "http://example/callback");
        request.addOAuthParameter(OAuthConstants.SIGNATURE, "OAuth-Signature");
        return request;
    }

    /**
     * Crée une requête OAuth échantillon sur le port 443 avec un chemin.
     *
     * @return Une instance de {@link OAuthRequest}.
     */
    public static OAuthRequest createSampleOAuthRequestPort443v2() {
        final OAuthRequest request = new OAuthRequest(Verb.GET, "https://example.com:443/test");
        request.addOAuthParameter(OAuthConstants.TIMESTAMP, "123456");
        request.addOAuthParameter(OAuthConstants.CONSUMER_KEY, "AS#$^*@&");
        request.addOAuthParameter(OAuthConstants.CALLBACK, "http://example/callback");
        request.addOAuthParameter(OAuthConstants.SIGNATURE, "OAuth-Signature");
        return request;
    }
}
