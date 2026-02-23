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

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests détaillés de la classe {@link ParameterList}.
 */
public class ParameterListDetailedTest {

    /**
     * Vérifie que le tri des paramètres s'effectue correctement par clé puis par valeur.
     */
    @Test
    public void shouldSortParametersCorrectly() {
        final ParameterList list = new ParameterList();
        list.add("z", "last");
        list.add("a", "first");
        list.add("m", "middle");
        list.add("a", "alpha"); // Same key, different value

        final ParameterList sorted = list.sort();

        assertThat(sorted.getParams().get(0).getKey()).isEqualTo("a");
        assertThat(sorted.getParams().get(0).getValue()).isEqualTo("alpha");
        assertThat(sorted.getParams().get(1).getValue()).isEqualTo("first");
        assertThat(sorted.getParams().get(2).getKey()).isEqualTo("m");
        assertThat(sorted.getParams().get(3).getKey()).isEqualTo("z");
    }

    /**
     * Vérifie la fusion de deux listes de paramètres.
     */
    @Test
    public void shouldAddAllFromAnotherList() {
        final ParameterList list1 = new ParameterList();
        list1.add("k1", "v1");

        final ParameterList list2 = new ParameterList();
        list2.add("k2", "v2");

        list1.addAll(list2);

        assertThat(list1.size()).isEqualTo(2);
        assertThat(list1.asFormUrlEncodedString()).contains("k1=v1").contains("k2=v2");
    }

    /**
     * Vérifie l'initialisation d'une liste à partir d'une {@link Map}.
     */
    @Test
    public void shouldInitializeFromMap() {
        final Map<String, String> map = new HashMap<>();
        map.put("key1", "val1");
        map.put("key2", "val2");

        final ParameterList list = new ParameterList(map);
        assertThat(list.size()).isEqualTo(2);
    }

    /**
     * Vérifie que l'ajout à une URL nulle lève une exception.
     */
    @Test
    public void shouldHandleNullUrlOnAppend() {
        final ParameterList list = new ParameterList();
        assertThatThrownBy(() -> list.appendTo(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot append to null URL");
    }

    /**
     * Vérifie l'ajout correct de paramètres à une URL possédant déjà une QueryString.
     */
    @Test
    public void shouldCorrectelyAppendToUrlWithExistingQuery() {
        final ParameterList list = new ParameterList();
        list.add("new", "param");

        assertThat(list.appendTo("http://example.com?old=val"))
                .isEqualTo("http://example.com?old=val&new=param");
    }
}
