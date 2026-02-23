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
package com.github.scribejava.core.httpclient.multipart;

import com.github.scribejava.core.httpclient.HttpClient;

import java.util.Collections;
import java.util.Map;

/**
 * Classe de base abstraite pour le contenu d'une partie d'une requête Multipart.
 *
 * <p>Cette classe gère les en-têtes spécifiques à une partie du corps de la requête.
 */
public abstract class BodyPartPayload {

    private final Map<String, String> headers;

    /**
     * Constructeur par défaut sans en-tête.
     */
    public BodyPartPayload() {
        this((Map<String, String>) null);
    }

    /**
     * Constructeur avec un type de contenu (Content-Type).
     *
     * @param contentType Le type de contenu de cette partie.
     */
    public BodyPartPayload(String contentType) {
        this(convertContentTypeToHeaders(contentType));
    }

    /**
     * Constructeur avec une map d'en-têtes.
     *
     * @param headers Les en-têtes de cette partie.
     */
    public BodyPartPayload(Map<String, String> headers) {
        this.headers = headers;
    }

    /**
     * Convertit un type de contenu en une map d'en-têtes.
     *
     * @param contentType Le type de contenu.
     * @return Une map contenant l'en-tête Content-Type.
     */
    protected static Map<String, String> convertContentTypeToHeaders(String contentType) {
        return contentType == null
                ? null
                : Collections.singletonMap(HttpClient.CONTENT_TYPE, contentType);
    }

    /**
     * Retourne les en-têtes de cette partie.
     *
     * @return La map des en-têtes.
     */
    public Map<String, String> getHeaders() {
        return headers;
    }
}
