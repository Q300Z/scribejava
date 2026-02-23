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
package com.github.scribejava.core.builder;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Constructeur de portée (Scope Builder) pour OAuth 2.0.
 *
 * <p>Permet de spécifier plusieurs portées une par une et de les combiner en une seule chaîne
 * délimitée par des espaces, conformément au standard OAuth 2.0.
 *
 * @see <a href="https://tools.ietf.org/html/rfc6749#section-3.3">RFC 6749, Section 3.3 (Access
 * Token Scope)</a>
 */
public class ScopeBuilder {

    private final Set<String> scopes = new HashSet<>();

    /**
     * Constructeur par défaut.
     */
    public ScopeBuilder() {
    }

    /**
     * Constructeur avec une portée initiale.
     *
     * @param scope La portée initiale.
     */
    public ScopeBuilder(String scope) {
        withScope(scope);
    }

    /**
     * Constructeur avec plusieurs portées initiales.
     *
     * @param scopes Tableau de portées.
     */
    public ScopeBuilder(String... scopes) {
        withScopes(scopes);
    }

    /**
     * Constructeur avec une collection de portées initiales.
     *
     * @param scopes Collection de portées.
     */
    public ScopeBuilder(Collection<String> scopes) {
        withScopes(scopes);
    }

    /**
     * Ajoute une portée individuelle.
     *
     * @param scope La portée à ajouter.
     * @return L'instance actuelle.
     */
    public final ScopeBuilder withScope(String scope) {
        scopes.add(scope);
        return this;
    }

    /**
     * Ajoute plusieurs portées.
     *
     * @param scopes Tableau de portées.
     * @return L'instance actuelle.
     */
    public final ScopeBuilder withScopes(String... scopes) {
        this.scopes.addAll(Arrays.asList(scopes));
        return this;
    }

    /**
     * Ajoute une collection de portées.
     *
     * @param scopes Collection de portées.
     * @return L'instance actuelle.
     */
    public final ScopeBuilder withScopes(Collection<String> scopes) {
        this.scopes.addAll(scopes);
        return this;
    }

    /**
     * Construit la chaîne de portée finale.
     *
     * @return Une chaîne de caractères contenant toutes les portées séparées par des espaces.
     */
    public String build() {
        final StringBuilder scopeBuilder = new StringBuilder();
        for (String scope : scopes) {
            scopeBuilder.append(' ').append(scope);
        }
        return scopeBuilder.substring(1);
    }
}
