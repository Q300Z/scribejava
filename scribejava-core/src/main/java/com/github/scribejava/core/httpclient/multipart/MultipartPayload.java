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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Représente un corps de requête Multipart.
 *
 * <p>Cette classe permet de composer des requêtes complexes composées de plusieurs parties,
 * potentiellement imbriquées.
 */
public class MultipartPayload extends BodyPartPayload {

    private static final String DEFAULT_SUBTYPE = "form-data";

    private final String boundary;
    private final List<BodyPartPayload> bodyParts = new ArrayList<>();
    private String preamble;
    private String epilogue;

    /**
     * Constructeur par défaut avec un séparateur (boundary) généré.
     */
    public MultipartPayload() {
        this(null, MultipartUtils.generateDefaultBoundary(), null);
    }

    /**
     * Constructeur avec un séparateur spécifique.
     *
     * @param boundary Le séparateur à utiliser.
     */
    public MultipartPayload(String boundary) {
        this(null, boundary, null);
    }

    /**
     * Constructeur avec un sous-type et un séparateur.
     *
     * @param subtype  Le sous-type (ex: "form-data", "mixed").
     * @param boundary Le séparateur.
     */
    public MultipartPayload(String subtype, String boundary) {
        this(subtype, boundary, null);
    }

    /**
     * Constructeur avec une map d'en-têtes.
     *
     * @param headers Les en-têtes.
     */
    public MultipartPayload(Map<String, String> headers) {
        this(null, parseOrGenerateBoundary(headers), headers);
    }

    /**
     * Constructeur avec un séparateur et des en-têtes.
     *
     * @param boundary Le séparateur.
     * @param headers  Les en-têtes.
     */
    public MultipartPayload(String boundary, Map<String, String> headers) {
        this(null, boundary, headers);
    }

    /**
     * Constructeur complet.
     *
     * @param subtype  Le sous-type.
     * @param boundary Le séparateur.
     * @param headers  Les en-têtes.
     */
    public MultipartPayload(String subtype, String boundary, Map<String, String> headers) {
        super(composeHeaders(subtype, boundary, headers));
        this.boundary = boundary;
    }

    private static Map<String, String> composeHeaders(
            String subtype, String boundary, Map<String, String> headersIn)
            throws IllegalArgumentException {
        MultipartUtils.checkBoundarySyntax(boundary);
        final Map<String, String> headersOut;
        String contentTypeHeader = headersIn == null ? null : headersIn.get(HttpClient.CONTENT_TYPE);
        if (contentTypeHeader == null) {
            contentTypeHeader =
                    "multipart/"
                            + (subtype == null ? DEFAULT_SUBTYPE : subtype)
                            + "; boundary=\""
                            + boundary
                            + '"';
            if (headersIn == null) {
                headersOut = Collections.singletonMap(HttpClient.CONTENT_TYPE, contentTypeHeader);
            } else {
                headersOut = headersIn;
                headersOut.put(HttpClient.CONTENT_TYPE, contentTypeHeader);
            }
        } else {
            headersOut = headersIn;
            final String parsedBoundary = MultipartUtils.parseBoundaryFromHeader(contentTypeHeader);
            if (parsedBoundary == null) {
                headersOut.put(
                        HttpClient.CONTENT_TYPE, contentTypeHeader + "; boundary=\"" + boundary + '"');
            } else if (!parsedBoundary.equals(boundary)) {
                throw new IllegalArgumentException(
                        "Different boundaries was passed in constructors. One as argument, second as header");
            }
        }
        return headersOut;
    }

    private static String parseOrGenerateBoundary(Map<String, String> headers) {
        final String parsedBoundary =
                MultipartUtils.parseBoundaryFromHeader(headers.get(HttpClient.CONTENT_TYPE));
        return parsedBoundary == null ? MultipartUtils.generateDefaultBoundary() : parsedBoundary;
    }

    /**
     * Ajoute une partie au corps Multipart.
     *
     * @param bodyPartPayload La partie à ajouter.
     */
    public void addBodyPart(BodyPartPayload bodyPartPayload) {
        bodyParts.add(bodyPartPayload);
    }

    /**
     * Ajoute un corps Multipart imbriqué.
     *
     * @param multipartPayload Le corps Multipart à imbriquer.
     */
    public void addBodyPart(MultipartPayload multipartPayload) {
        if (multipartPayload.getBoundary().equals(boundary)) {
            throw new IllegalArgumentException(
                    "{'boundary'}={'" + boundary + "'} is the same for parent MultipartPayload and child");
        }
        bodyParts.add(multipartPayload);
    }

    /**
     * Retourne la liste des parties.
     *
     * @return La liste des {@link BodyPartPayload}.
     */
    public List<BodyPartPayload> getBodyParts() {
        return bodyParts;
    }

    /**
     * Retourne le séparateur.
     *
     * @return Le séparateur (boundary).
     */
    public String getBoundary() {
        return boundary;
    }

    /**
     * Retourne le préambule.
     *
     * @return Le préambule.
     */
    public String getPreamble() {
        return preamble;
    }

    /**
     * Définit le préambule.
     *
     * @param preamble Le préambule.
     */
    public void setPreamble(String preamble) {
        this.preamble = preamble;
    }

    /**
     * Retourne l'épilogue.
     *
     * @return L'épilogue.
     */
    public String getEpilogue() {
        return epilogue;
    }

    /**
     * Définit l'épilogue.
     *
     * @param epilogue L'épilogue.
     */
    public void setEpilogue(String epilogue) {
        this.epilogue = epilogue;
    }
}
