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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests unitaires pour la manipulation des listes de paramètres {@link ParameterList}.
 *
 * <p>Vérifie l'ajout, le tri et l'encodage des paramètres pour la QueryString ou le corps des
 * requêtes.
 */
public class ParameterListTest {

    private ParameterList params;

    /**
     * Initialisation d'une liste de paramètres vide avant chaque test.
     */
    @BeforeEach
    public void setUp() {
        this.params = new ParameterList();
    }

    /**
     * Vérifie que la tentative d'ajout à une URL nulle lève une exception.
     */
    @Test
    public void shouldThrowExceptionWhenAppendingNullMapToQuerystring() {
        assertThatThrownBy(() -> params.appendTo(null)).isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Vérifie qu'une liste vide n'altère pas l'URL d'origine.
     */
    @Test
    public void shouldAppendNothingToQuerystringIfGivenEmptyMap() {
        final String url = "http://www.example.com";
        assertThat(params.appendTo(url)).isEqualTo(url);
    }

    /**
     * Vérifie l'ajout correct de paramètres à une URL sans QueryString.
     */
    @Test
    public void shouldAppendParametersToSimpleUrl() {
        String url = "http://www.example.com";
        final String expectedUrl = "http://www.example.com?param1=value1&param2=value%20with%20spaces";

        params.add("param1", "value1");
        params.add("param2", "value with spaces");

        url = params.appendTo(url);
        assertThat(url).isEqualTo(expectedUrl);
    }

    /**
     * Vérifie l'ajout correct de paramètres à une URL possédant déjà une QueryString.
     */
    @Test
    public void shouldAppendParametersToUrlWithQuerystring() {
        String url = "http://www.example.com?already=present";
        final String expectedUrl =
                "http://www.example.com?already=present&param1=value1&param2=value%20with%20spaces";

        params.add("param1", "value1");
        params.add("param2", "value with spaces");

        url = params.appendTo(url);
        assertThat(url).isEqualTo(expectedUrl);
    }

    /**
     * Vérifie le tri lexicographique des paramètres (clé, puis valeur).
     */
    @Test
    public void shouldProperlySortParameters() {
        params.add("param1", "v1");
        params.add("param6", "v2");
        params.add("a_param", "v3");
        params.add("param2", "v4");
        assertThat(params.sort().asFormUrlEncodedString())
                .isEqualTo("a_param=v3&param1=v1&param2=v4&param6=v2");
    }

    /**
     * Vérifie le tri correct lorsque plusieurs paramètres ont le même nom.
     */
    @Test
    public void shouldProperlySortParametersWithTheSameName() {
        params.add("param1", "v1");
        params.add("param6", "v2");
        params.add("a_param", "v3");
        params.add("param1", "v4");
        assertThat(params.sort().asFormUrlEncodedString())
                .isEqualTo("a_param=v3&param1=v1&param1=v4&param6=v2");
    }

    /**
     * Vérifie que l'opération de tri retourne une nouvelle instance (immutabilité).
     */
    @Test
    public void shouldNotModifyTheOriginalParameterList() {
        params.add("param1", "v1");
        params.add("param6", "v2");

        assertThat(params.sort()).isNotSameAs(params);
    }
}
