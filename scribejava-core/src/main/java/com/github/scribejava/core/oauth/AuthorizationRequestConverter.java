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

import java.util.Map;

/**
 * Strategy to convert authorization request parameters. Used for JAR (JWT Secured Authorization
 * Request) or PAR (Pushed Authorization Request).
 */
public interface AuthorizationRequestConverter {

    /**
     * Convertit les paramètres de la requête d'autorisation.
     *
     * <p>Utilisé pour transformer des paramètres classiques en un objet de requête sécurisé (JAR) ou
     * pour adapter la requête pour PAR.
     *
     * @param params Les paramètres d'autorisation d'origine.
     * @return Les paramètres convertis (ex: un dictionnaire contenant uniquement le paramètre
     * 'request').
     */
    Map<String, String> convert(Map<String, String> params);
}
