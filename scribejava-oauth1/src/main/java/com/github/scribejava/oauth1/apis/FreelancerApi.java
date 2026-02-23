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
package com.github.scribejava.oauth1.apis;

import com.github.scribejava.core.model.Verb;
import com.github.scribejava.oauth1.builder.api.DefaultApi10a;
import com.github.scribejava.oauth1.builder.api.OAuth1SignatureType;

/**
 * API OAuth 1.0a pour FreelancerApi.
 */
public class FreelancerApi extends DefaultApi10a {

    private static final String AUTHORIZATION_URL =
            "http://www.freelancer.com/users/api-token/auth.php";

    protected FreelancerApi() {
    }

    /**
     * Retourne l'instance unique de l'API.
     *
     * @return L'instance {@link FreelancerApi}.
     */
    public static FreelancerApi instance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public OAuth1SignatureType getSignatureType() {
        return OAuth1SignatureType.QUERY_STRING;
    }

    @Override
    public String getAccessTokenEndpoint() {
        return "http://api.freelancer.com/RequestAccessToken/requestAccessToken.xml?";
    }

    @Override
    public String getRequestTokenEndpoint() {
        return "http://api.freelancer.com/RequestRequestToken/requestRequestToken.xml";
    }

    @Override
    public Verb getAccessTokenVerb() {
        return Verb.GET;
    }

    @Override
    public Verb getRequestTokenVerb() {
        return Verb.GET;
    }

    @Override
    public String getAuthorizationBaseUrl() {
        return AUTHORIZATION_URL;
    }

    private static class InstanceHolder {
        private static final FreelancerApi INSTANCE = new FreelancerApi();
    }

    /**
     * Version Sandbox de l'API Freelancer.
     */
    public static class Sandbox extends FreelancerApi {

        private static final String SANDBOX_AUTHORIZATION_URL =
                "http://www.sandbox.freelancer.com/users/api-token/auth.php";

        private Sandbox() {
        }

        /**
         * Retourne l'instance unique de la Sandbox.
         *
         * @return L'instance {@link Sandbox}.
         */
        public static Sandbox instance() {
            return InstanceHolder.INSTANCE;
        }

        @Override
        public String getRequestTokenEndpoint() {
            return "http://api.sandbox.freelancer.com/RequestRequestToken/requestRequestToken.xml";
        }

        @Override
        public String getAccessTokenEndpoint() {
            return "http://api.sandbox.freelancer.com/RequestAccessToken/requestAccessToken.xml?";
        }

        @Override
        public String getAuthorizationBaseUrl() {
            return SANDBOX_AUTHORIZATION_URL;
        }

        private static class InstanceHolder {
            private static final Sandbox INSTANCE = new Sandbox();
        }
    }
}
