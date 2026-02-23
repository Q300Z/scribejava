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
package com.github.scribejava.core.pkce;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Méthodes de calcul du code_challenge pour PKCE.
 *
 * @see <a href="https://tools.ietf.org/html/rfc7636#section-4.2">RFC 7636, Section 4.2 (Client
 * Creates the Code Challenge)</a>
 */
public enum PKCECodeChallengeMethod {
    S256 {
        @Override
        public String transform2CodeChallenge(String codeVerifier) throws NoSuchAlgorithmException {
            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(
                            MessageDigest.getInstance("SHA-256")
                                    .digest(codeVerifier.getBytes(StandardCharsets.US_ASCII)));
        }
    },
    PLAIN {
        @Override
        public String transform2CodeChallenge(String codeVerifier) {
            return codeVerifier;
        }
    };

    /**
     * Transforme le code_verifier en code_challenge selon la méthode.
     *
     * @param codeVerifier Le code verifier d'origine.
     * @return Le code challenge calculé.
     * @throws NoSuchAlgorithmException si l'algorithme de hachage n'est pas disponible.
     */
    public abstract String transform2CodeChallenge(String codeVerifier)
            throws NoSuchAlgorithmException;
}
