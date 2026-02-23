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

import com.github.scribejava.core.builder.ScopeBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Paramètres pour une requête d'obtention de jeton d'accès (Access Token).
 *
 * <p>Regroupe le code d'autorisation, le vérificateur PKCE et les éventuels paramètres
 * supplémentaires. Cette classe n'est pas thread-safe.
 */
public class AccessTokenRequestParams {

    private final String code;
    private String pkceCodeVerifier;
    private String scope;
    private Map<String, String> extraParameters;

    /**
     * Constructeur.
     *
     * @param code Le code d'autorisation reçu.
     */
    public AccessTokenRequestParams(String code) {
        this.code = code;
    }

    /**
     * Méthode statique de création.
     *
     * @param code Le code d'autorisation.
     * @return Une nouvelle instance de {@link AccessTokenRequestParams}.
     */
    public static AccessTokenRequestParams create(String code) {
        return new AccessTokenRequestParams(code);
    }

    /**
     * Définit le vérificateur de code PKCE.
     *
     * @param pkceCodeVerifier La valeur brute du code_verifier.
     * @return L'instance actuelle.
     * @see <a href="https://tools.ietf.org/html/rfc7636">RFC 7636 (PKCE)</a>
     */
    public AccessTokenRequestParams pkceCodeVerifier(String pkceCodeVerifier) {
        this.pkceCodeVerifier = pkceCodeVerifier;
        return this;
    }

    /**
     * Définit la portée (scope).
     *
     * @param scope La chaîne représentant la portée.
     * @return L'instance actuelle.
     */
    public AccessTokenRequestParams scope(String scope) {
        this.scope = scope;
        return this;
    }

    /**
     * Définit la portée via un builder.
     *
     * @param scope Le builder de portée.
     * @return L'instance actuelle.
     */
    public AccessTokenRequestParams scope(ScopeBuilder scope) {
        this.scope = scope.build();
        return this;
    }

    /**
     * Ajoute un dictionnaire de paramètres supplémentaires.
     *
     * @param extraParameters Les paramètres à ajouter.
     * @return L'instance actuelle.
     */
    public AccessTokenRequestParams addExtraParameters(Map<String, String> extraParameters) {
        if (extraParameters == null || extraParameters.isEmpty()) {
            return this;
        }
        if (this.extraParameters == null) {
            extraParameters = new HashMap<>();
        }
        this.extraParameters.putAll(extraParameters);
        return this;
    }

    /**
     * Ajoute un paramètre supplémentaire unique.
     *
     * @param name  Le nom du paramètre.
     * @param value La valeur du paramètre.
     * @return L'instance actuelle.
     */
    public AccessTokenRequestParams addExtraParameter(String name, String value) {
        if (this.extraParameters == null) {
            extraParameters = new HashMap<>();
        }
        this.extraParameters.put(name, value);
        return this;
    }

    /**
     * @return Le dictionnaire des paramètres supplémentaires.
     */
    public Map<String, String> getExtraParameters() {
        return extraParameters;
    }

    /**
     * Définit globalement les paramètres supplémentaires.
     *
     * @param extraParameters Le dictionnaire de paramètres.
     * @return L'instance actuelle.
     */
    public AccessTokenRequestParams setExtraParameters(Map<String, String> extraParameters) {
        this.extraParameters = extraParameters;
        return this;
    }

    /**
     * @return Le code d'autorisation.
     */
    public String getCode() {
        return code;
    }

    /**
     * @return Le vérificateur de code PKCE.
     */
    public String getPkceCodeVerifier() {
        return pkceCodeVerifier;
    }

    /**
     * @return La portée configurée.
     */
    public String getScope() {
        return scope;
    }
}
