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
package com.github.scribejava.core;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.model.OAuthAsyncRequestCallback;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.oauth.OAuthService;
import com.github.scribejava.core.utils.StreamUtils;
import java.util.concurrent.TimeUnit;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Classe de base abstraite pour les tests unitaires des clients HTTP.
 *
 * <p>Définit une suite de tests standardisés que chaque implémentation de client doit valider.
 */
public abstract class AbstractClientTest {

  private OAuthService oAuthService;

  /** Configuration initiale avant chaque test. */
  @BeforeEach
  public void setUp() {
    oAuthService =
        new OAuth20Service(
            null, "test", "test", null, null, null, System.out, null, null, createNewClient());
  }

  /** Fermeture des ressources après chaque test. */
  @AfterEach
  public void shutDown() throws Exception {
    oAuthService.close();
  }

  /** @return Une nouvelle instance du client HTTP à tester. */
  protected abstract HttpClient createNewClient();

  /** Vérifie l'envoi correct d'une requête GET. */
  @Test
  public void shouldSendGetRequest() throws Exception {
    final String expectedResponseBody = "response body for test shouldSendGetRequest";

    final MockWebServer server = new MockWebServer();
    server.enqueue(new MockResponse().setBody(expectedResponseBody));
    server.start();

    final HttpUrl baseUrl = server.url("/testUrl");

    final OAuthRequest request = new OAuthRequest(Verb.GET, baseUrl.toString());

    try (Response response = oAuthService.execute(request, null).get(30, TimeUnit.SECONDS)) {
      assertThat(response.getBody()).isEqualTo(expectedResponseBody);
    }

    final RecordedRequest recordedRequest = server.takeRequest();
    assertThat(recordedRequest.getMethod()).isEqualTo("GET");

    server.shutdown();
  }

  /** Vérifie la présence de l'en-tête Content-Type par défaut pour les requêtes POST. */
  @Test
  public void shouldSendPostWithApplicationXWwwFormUrlencodedRequestContentTypeHeader()
      throws Exception {
    final MockWebServer server = new MockWebServer();
    server.enqueue(new MockResponse());
    server.start();

    final HttpUrl baseUrl = server.url("/testUrl");

    final OAuthRequest request = new OAuthRequest(Verb.POST, baseUrl.toString());
    oAuthService.execute(request, null).get(30, TimeUnit.SECONDS).close();

    final RecordedRequest recordedRequest = server.takeRequest();
    assertThat(recordedRequest.getMethod()).isEqualTo("POST");
    assertThat(recordedRequest.getHeader(HttpClient.CONTENT_TYPE))
        .isEqualTo(HttpClient.DEFAULT_CONTENT_TYPE);

    server.shutdown();
  }

  /** Vérifie l'envoi d'une requête POST avec un corps vide. */
  @Test
  public void shouldSendPostRequestWithEmptyBody() throws Exception {
    final String expectedResponseBody = "response body for test shouldSendPostRequest";
    final String expectedRequestBody = "";

    final MockWebServer server = new MockWebServer();
    server.enqueue(new MockResponse().setBody(expectedResponseBody));
    server.start();

    final HttpUrl baseUrl = server.url("/testUrl");

    final OAuthRequest request = new OAuthRequest(Verb.POST, baseUrl.toString());
    try (Response response = oAuthService.execute(request, null).get(30, TimeUnit.SECONDS)) {
      assertThat(response.getBody()).isEqualTo(expectedResponseBody);
    }

    final RecordedRequest recordedRequest = server.takeRequest();
    assertThat(recordedRequest.getMethod()).isEqualTo("POST");
    assertThat(recordedRequest.getBody().readUtf8()).isEqualTo(expectedRequestBody);
    assertThat(recordedRequest.getHeader(HttpClient.CONTENT_TYPE))
        .isEqualTo(HttpClient.DEFAULT_CONTENT_TYPE);

    server.shutdown();
  }

  /** Vérifie l'envoi d'une requête POST avec une charge utile textuelle. */
  @Test
  public void shouldSendPostRequestWithStringBody() throws Exception {
    final String expectedResponseBody = "response body for test shouldSendPostRequest";
    final String expectedRequestBody = "request body";

    final MockWebServer server = new MockWebServer();
    server.enqueue(new MockResponse().setBody(expectedResponseBody));
    server.start();

    final HttpUrl baseUrl = server.url("/testUrl");

    final OAuthRequest request = new OAuthRequest(Verb.POST, baseUrl.toString());
    request.setPayload(expectedRequestBody);
    try (Response response = oAuthService.execute(request, null).get(30, TimeUnit.SECONDS)) {
      assertThat(response.getBody()).isEqualTo(expectedResponseBody);
    }

    final RecordedRequest recordedRequest = server.takeRequest();
    assertThat(recordedRequest.getMethod()).isEqualTo("POST");
    assertThat(recordedRequest.getBody().readUtf8()).isEqualTo(expectedRequestBody);
    final String contentTypeHeader = recordedRequest.getHeader(HttpClient.CONTENT_TYPE);
    assertThat(contentTypeHeader).isNotNull();
    assertThat(contentTypeHeader).startsWith(HttpClient.DEFAULT_CONTENT_TYPE);

    server.shutdown();
  }

  /** Vérifie l'envoi d'une requête POST avec une charge utile binaire. */
  @Test
  public void shouldSendPostRequestWithByteBodyBody() throws Exception {
    final String expectedResponseBody = "response body for test shouldSendPostRequest";
    final String expectedRequestBody = "request body";

    final MockWebServer server = new MockWebServer();
    server.enqueue(new MockResponse().setBody(expectedResponseBody));
    server.start();

    final HttpUrl baseUrl = server.url("/testUrl");

    final OAuthRequest request = new OAuthRequest(Verb.POST, baseUrl.toString());
    request.setPayload(expectedRequestBody.getBytes());
    try (Response response = oAuthService.execute(request, null).get(30, TimeUnit.SECONDS)) {
      assertThat(response.getBody()).isEqualTo(expectedResponseBody);
    }

    final RecordedRequest recordedRequest = server.takeRequest();
    assertThat(recordedRequest.getMethod()).isEqualTo("POST");
    assertThat(recordedRequest.getBody().readUtf8()).isEqualTo(expectedRequestBody);
    assertThat(recordedRequest.getHeader(HttpClient.CONTENT_TYPE))
        .isEqualTo(HttpClient.DEFAULT_CONTENT_TYPE);

    server.shutdown();
  }

  /** Vérifie l'envoi d'une requête POST avec des paramètres de corps. */
  @Test
  public void shouldSendPostRequestWithBodyParamsBody() throws Exception {
    final String expectedResponseBody = "response body for test shouldSendPostRequest";
    final String expectedRequestBodyParamName = "request_body_param_name";
    final String expectedRequestBodyParamValue = "request_body_param_value";
    final String expectedRequestBody =
        expectedRequestBodyParamName + '=' + expectedRequestBodyParamValue;

    final MockWebServer server = new MockWebServer();
    server.enqueue(new MockResponse().setBody(expectedResponseBody));
    server.start();

    final HttpUrl baseUrl = server.url("/testUrl");

    final OAuthRequest request = new OAuthRequest(Verb.POST, baseUrl.toString());
    request.addBodyParameter(expectedRequestBodyParamName, expectedRequestBodyParamValue);
    try (Response response = oAuthService.execute(request, null).get(30, TimeUnit.SECONDS)) {
      assertThat(response.getBody()).isEqualTo(expectedResponseBody);
    }

    final RecordedRequest recordedRequest = server.takeRequest();
    assertThat(recordedRequest.getMethod()).isEqualTo("POST");
    assertThat(recordedRequest.getBody().readUtf8()).isEqualTo(expectedRequestBody);
    assertThat(recordedRequest.getHeader(HttpClient.CONTENT_TYPE))
        .isEqualTo(HttpClient.DEFAULT_CONTENT_TYPE);

    server.shutdown();
  }

  /** Vérifie la capacité à lire le flux de données de la réponse brute. */
  @Test
  public void shouldReadResponseStream() throws Exception {
    final String expectedResponseBody = "response body for test shouldReadResponseStream";

    final MockWebServer server = new MockWebServer();
    server.enqueue(new MockResponse().setBody(expectedResponseBody));
    server.start();

    final HttpUrl baseUrl = server.url("/testUrl");

    final OAuthRequest request = new OAuthRequest(Verb.GET, baseUrl.toString());
    try (Response response = oAuthService.execute(request, null).get(30, TimeUnit.SECONDS)) {
      assertThat(StreamUtils.getStreamContents(response.getStream()))
          .isEqualTo(expectedResponseBody);
    }

    final RecordedRequest recordedRequest = server.takeRequest();
    assertThat(recordedRequest.getMethod()).isEqualTo("GET");

    server.shutdown();
  }

  /** Vérifie que le rappel (callback) est correctement invoqué lors d'une requête réussie. */
  @Test
  public void shouldCallCallback() throws Exception {
    final String expectedResponseBody = "response body for test shouldCallCallback";

    final MockWebServer server = new MockWebServer();
    server.enqueue(new MockResponse().setBody(expectedResponseBody));
    server.start();

    final HttpUrl baseUrl = server.url("/testUrl");

    final OAuthRequest request = new OAuthRequest(Verb.GET, baseUrl.toString());

    final TestCallback callback = new TestCallback();
    oAuthService.execute(request, callback).get();

    assertThat(callback.getResponse().getBody()).isEqualTo(expectedResponseBody);

    server.shutdown();
  }

  /** Vérifie la propagation correcte des codes d'erreur HTTP. */
  @Test
  public void shouldPassErrors() throws Exception {

    final MockWebServer server = new MockWebServer();
    server.enqueue(new MockResponse().setResponseCode(500));
    server.start();

    final HttpUrl baseUrl = server.url("/testUrl");

    final OAuthRequest request = new OAuthRequest(Verb.GET, baseUrl.toString());

    final TestCallback callback = new TestCallback();
    try (Response response = oAuthService.execute(request, callback).get()) {

      assertThat(response.getCode()).isEqualTo(500);
      assertThat(callback.getResponse().getCode()).isEqualTo(500);
    }

    server.shutdown();
  }

  /** Implémentation de test pour le rappel asynchrone. */
  private static class TestCallback implements OAuthAsyncRequestCallback<Response> {

    private Response response;

    @Override
    public void onCompleted(Response response) {
      this.response = response;
    }

    @Override
    public void onThrowable(Throwable throwable) {}

    public Response getResponse() {
      return response;
    }
  }
}
