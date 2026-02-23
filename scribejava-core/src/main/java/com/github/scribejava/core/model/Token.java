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
package com.github.scribejava.core.model;

import java.io.Serializable;

/**
 * Represents an abstract OAuth (1 and 2) token (either request or access token)
 */
public abstract class Token implements Serializable {

    private static final long serialVersionUID = -8409640649946468092L;

    private final String rawResponse;

    protected Token(String rawResponse) {
        this.rawResponse = rawResponse;
    }

    /**
     * Retourne la réponse brute à partir de laquelle le jeton a été extrait.
     *
     * @return La réponse brute.
     * @throws IllegalStateException si le jeton n'a pas été construit avec une réponse brute.
     */
    public String getRawResponse() {
        if (rawResponse == null) {
            throw new IllegalStateException(
                    "This token object was not constructed by ScribeJava and does not have a rawResponse");
        }
        return rawResponse;
    }

    /**
     * Extrait un paramètre spécifique de la réponse brute.
     *
     * @param parameter Le nom du paramètre à extraire.
     * @return La valeur du paramètre ou null si non trouvé.
     */
    public String getParameter(String parameter) {
        String value = null;
        for (String str : rawResponse.split("&")) {
            if (str.startsWith(parameter + '=')) {
                final String[] part = str.split("=");
                if (part.length > 1) {
                    value = part[1].trim();
                }
                break;
            }
        }
        return value;
    }
}
