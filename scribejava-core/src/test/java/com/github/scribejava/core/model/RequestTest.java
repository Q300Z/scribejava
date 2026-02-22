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

import static org.junit.Assert.*;

import org.junit.Test;

/** Tests unitaires pour la manipulation des requêtes HTTP {@link OAuthRequest}. */
public class RequestTest {

  /** Vérifie l'extraction correcte des paramètres de la QueryString. */
  @Test
  public void shouldGetQueryStringParameters() {
    final OAuthRequest postRequest = new OAuthRequest(Verb.POST, "http://example.com");
    postRequest.addBodyParameter("param", "value");
    postRequest.addBodyParameter("param with spaces", "value with spaces");

    final OAuthRequest getRequest =
        new OAuthRequest(
            Verb.GET, "http://example.com?qsparam=value&other+param=value+with+spaces");

    assertEquals(2, getRequest.getQueryStringParams().size());
    assertEquals(0, postRequest.getQueryStringParams().size());
    assertTrue(getRequest.getQueryStringParams().contains(new Parameter("qsparam", "value")));
  }

  /** Vérifie la génération correcte du corps de requête encodé. */
  @Test
  public void shouldSetBodyParamsAndAddContentLength() {
    final OAuthRequest postRequest = new OAuthRequest(Verb.POST, "http://example.com");
    postRequest.addBodyParameter("param", "value");
    postRequest.addBodyParameter("param with spaces", "value with spaces");

    assertEquals(
        "param=value&param%20with%20spaces=value%20with%20spaces",
        new String(postRequest.getByteArrayPayload()));
  }

  /** Vérifie que la charge utile (payload) écrase les paramètres de corps si définie. */
  @Test
  public void shouldSetPayloadAndHeaders() {
    final OAuthRequest postRequest = new OAuthRequest(Verb.POST, "http://example.com");
    postRequest.addBodyParameter("param", "value");
    postRequest.addBodyParameter("param with spaces", "value with spaces");
    postRequest.setPayload("PAYLOAD");

    assertEquals("PAYLOAD", postRequest.getStringPayload());
  }

  /** Vérifie l'ajout de paramètres de requête après l'instanciation. */
  @Test
  public void shouldAllowAddingQuerystringParametersAfterCreation() {
    final OAuthRequest request = new OAuthRequest(Verb.GET, "http://example.com?one=val");
    request.addQuerystringParameter("two", "other val");
    request.addQuerystringParameter("more", "params");
    assertEquals(3, request.getQueryStringParams().size());
  }

  /** Vérifie la construction de l'URL complète avec tous les paramètres encodés. */
  @Test
  public void shouldReturnTheCompleteUrl() {
    final OAuthRequest request = new OAuthRequest(Verb.GET, "http://example.com?one=val");
    request.addQuerystringParameter("two", "other val");
    request.addQuerystringParameter("more", "params");
    assertEquals(
        "http://example.com?one=val&two=other%20val&more=params", request.getCompleteUrl());
  }

  /** Vérifie le décodage correct des espaces encodés avec '+' dans les URLs. */
  @Test
  public void shouldHandleQueryStringSpaceEncodingProperly() {
    final OAuthRequest getRequest =
        new OAuthRequest(
            Verb.GET, "http://example.com?qsparam=value&other+param=value+with+spaces");

    assertTrue(
        getRequest
            .getQueryStringParams()
            .contains(new Parameter("other param", "value with spaces")));
  }

  /** Vérifie que la charge utile textuelle brute n'est pas ré-encodée. */
  @Test
  public void shouldNotEncodeInStringPayload() throws Exception {
    final String requestBody = "~/!@#$%^&*( )_+//\r\n%2F&amp;";

    final OAuthRequest postRequest = new OAuthRequest(Verb.POST, "http://example.com");
    postRequest.setPayload(requestBody);

    assertEquals(requestBody, postRequest.getStringPayload());
  }

  /** Vérifie que la charge utile binaire brute n'est pas altérée. */
  @Test
  public void shouldNotEncodeInByteBodyPayload() throws Exception {
    final byte[] requestBody = "~/!@#$%^&*( )_+//\r\n%2F&amp;".getBytes();

    final OAuthRequest postRequest = new OAuthRequest(Verb.POST, "http://example.com");
    postRequest.setPayload(requestBody);

    assertArrayEquals(requestBody, postRequest.getByteArrayPayload());
  }

  /** Vérifie l'encodage strict des caractères spéciaux dans les paramètres de corps. */
  @Test
  public void shouldEncodeInBodyParamsPayload() throws Exception {
    final String expectedRequestBodyParamName = "~/!@#$%^&*( )_+//\r\n%2F&amp;name";
    final String expectedRequestBodyParamValue = "~/!@#$%^&*( )_+//\r\n%2F&amp;value";
    final String expectedRequestBody =
        "~%2F%21%40%23%24%25%5E%26%2A%28%20%29_%2B%2F%2F%0D%0A%252F%26amp%3Bname="
            + "~%2F%21%40%23%24%25%5E%26%2A%28%20%29_%2B%2F%2F%0D%0A%252F%26amp%3Bvalue";

    final OAuthRequest postRequest = new OAuthRequest(Verb.POST, "http://example.com");
    postRequest.addBodyParameter(expectedRequestBodyParamName, expectedRequestBodyParamValue);
    assertArrayEquals(expectedRequestBody.getBytes(), postRequest.getByteArrayPayload());
  }
}
