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
package com.github.scribejava.core.extractors;

import com.github.scribejava.core.ObjectMother;
import com.github.scribejava.core.exceptions.OAuthParametersMissingException;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests unitaires pour l'implémentation par défaut de {@link BaseStringExtractor}.
 *
 * <p>Vérifie la génération correcte de la chaîne de base (Base String) utilisée pour la signature
 * OAuth 1.0a.
 */
public class BaseStringExtractorTest {

    private BaseStringExtractorImpl extractor;
    private OAuthRequest request;
    private OAuthRequest requestPort80;
    private OAuthRequest requestPort80v2;
    private OAuthRequest requestPort8080;
    private OAuthRequest requestPort443;
    private OAuthRequest requestPort443v2;

    /**
     * Initialisation des requêtes de test avec différents ports et URLs.
     */
    @BeforeEach
    public void setUp() {
        request = ObjectMother.createSampleOAuthRequest();
        requestPort80 = ObjectMother.createSampleOAuthRequestPort80();
        requestPort80v2 = ObjectMother.createSampleOAuthRequestPort80v2();
        requestPort8080 = ObjectMother.createSampleOAuthRequestPort8080();
        requestPort443 = ObjectMother.createSampleOAuthRequestPort443();
        requestPort443v2 = ObjectMother.createSampleOAuthRequestPort443v2();
        extractor = new BaseStringExtractorImpl();
    }

    /**
     * Vérifie l'extraction standard de la chaîne de base depuis une requête OAuth.
     */
    @Test
    public void shouldExtractBaseStringFromOAuthRequest() {
        final String expected =
                "GET&http%3A%2F%2Fexample.com&oauth_callback%3Dhttp%253A%252F%252Fexample%252Fcallback"
                        + "%26oauth_consumer_key%3DAS%2523%2524%255E%252A%2540%2526%26oauth_signature%3DOAuth-Signature"
                        + "%26oauth_timestamp%3D123456";
        final String baseString = extractor.extract(request);
        assertThat(baseString).isEqualTo(expected);
    }

    /**
     * Vérifie que le port 80 (HTTP par défaut) est bien exclu de l'URL normalisée.
     */
    @Test
    public void shouldExcludePort80() {
        final String expected =
                "GET&http%3A%2F%2Fexample.com&oauth_callback%3Dhttp%253A%252F%252Fexample%252Fcallback"
                        + "%26oauth_consumer_key%3DAS%2523%2524%255E%252A%2540%2526%26oauth_signature%3DOAuth-Signature"
                        + "%26oauth_timestamp%3D123456";
        final String baseString = extractor.extract(requestPort80);
        assertThat(baseString).isEqualTo(expected);
    }

    /**
     * Vérifie l'exclusion du port 80 avec un chemin d'URL.
     */
    @Test
    public void shouldExcludePort80v2() {
        final String expected =
                "GET&http%3A%2F%2Fexample.com%2Ftest&oauth_callback%3Dhttp%253A%252F%252Fexample"
                        + "%252Fcallback%26oauth_consumer_key%3DAS%2523%2524%255E%252A%2540%2526%26oauth_signature"
                        + "%3DOAuth-Signature%26oauth_timestamp%3D123456";
        final String baseString = extractor.extract(requestPort80v2);
        assertThat(baseString).isEqualTo(expected);
    }

    /**
     * Vérifie que les ports non standards (ex: 8080) sont bien inclus dans l'URL normalisée.
     */
    @Test
    public void shouldIncludePort8080() {
        final String expected =
                "GET&http%3A%2F%2Fexample.com%3A8080&oauth_callback%3Dhttp%253A%252F%252Fexample"
                        + "%252Fcallback%26oauth_consumer_key%3DAS%2523%2524%255E%252A%2540%2526%26oauth_signature"
                        + "%3DOAuth-Signature%26oauth_timestamp%3D123456";
        final String baseString = extractor.extract(requestPort8080);
        assertThat(baseString).isEqualTo(expected);
    }

    /**
     * Vérifie que le port 443 (HTTPS par défaut) est bien exclu de l'URL normalisée.
     */
    @Test
    public void shouldExcludePort443() {
        final String expected =
                "GET&https%3A%2F%2Fexample.com&oauth_callback%3Dhttp%253A%252F%252Fexample%252Fcallback"
                        + "%26oauth_consumer_key%3DAS%2523%2524%255E%252A%2540%2526%26oauth_signature%3DOAuth-Signature"
                        + "%26oauth_timestamp%3D123456";
        final String baseString = extractor.extract(requestPort443);
        assertThat(baseString).isEqualTo(expected);
    }

    /**
     * Vérifie l'exclusion du port 443 avec un chemin d'URL.
     */
    @Test
    public void shouldExcludePort443v2() {
        final String expected =
                "GET&https%3A%2F%2Fexample.com%2Ftest&oauth_callback%3Dhttp%253A%252F%252Fexample"
                        + "%252Fcallback%26oauth_consumer_key%3DAS%2523%2524%255E%252A%2540%2526%26oauth_signature"
                        + "%3DOAuth-Signature%26oauth_timestamp%3D123456";
        final String baseString = extractor.extract(requestPort443v2);
        assertThat(baseString).isEqualTo(expected);
    }

    /**
     * Vérifie que le passage d'une requête nulle lève une exception.
     */
    @Test
    public void shouldThrowExceptionIfRequestIsNull() {
        assertThatThrownBy(() -> extractor.extract(null)).isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Vérifie le comportement si la requête ne contient aucun paramètre OAuth obligatoire.
     */
    @Test
    public void shouldThrowExceptionIfRequestHasNoOAuthParameters() {
        final OAuthRequest request = new OAuthRequest(Verb.GET, "http://example.com");
        assertThatThrownBy(() -> extractor.extract(request))
                .isInstanceOf(OAuthParametersMissingException.class);
    }

    /**
     * Vérifie l'encodage correct des espaces dans les paramètres.
     */
    @Test
    public void shouldProperlyEncodeSpaces() {
        final String expected =
                "GET&http%3A%2F%2Fexample.com&body%3Dthis%2520param%2520has%2520whitespace"
                        + "%26oauth_callback%3Dhttp%253A%252F%252Fexample%252Fcallback%26oauth_consumer_key"
                        + "%3DAS%2523%2524%255E%252A%2540%2526%26oauth_signature%3DOAuth-Signature%26oauth_timestamp%3D123456";
        request.addBodyParameter("body", "this param has whitespace");
        assertThat(extractor.extract(request)).isEqualTo(expected);
    }
}
