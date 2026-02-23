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
package com.github.scribejava.core.httpclient;

import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.httpclient.multipart.MultipartPayload;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Classe de base pour les clients HTTP fonctionnant uniquement en mode asynchrone.
 *
 * <p>Cette classe implémente les méthodes synchrones de l'interface {@link HttpClient} en bloquant
 * sur le résultat des méthodes asynchrones correspondantes.
 */
public abstract class AbstractAsyncOnlyHttpClient implements HttpClient {

  @Override
  public Response execute(
      String userAgent,
      Map<String, String> headers,
      Verb httpVerb,
      String completeUrl,
      byte[] bodyContents)
      throws InterruptedException, ExecutionException, IOException {
    return executeAsync(
            userAgent,
            headers,
            httpVerb,
            completeUrl,
            bodyContents,
            null,
            OAuthRequest.ResponseConverter.IDENTITY)
        .get();
  }

  @Override
  public Response execute(
      String userAgent,
      Map<String, String> headers,
      Verb httpVerb,
      String completeUrl,
      MultipartPayload bodyContents)
      throws InterruptedException, ExecutionException, IOException {
    return executeAsync(
            userAgent,
            headers,
            httpVerb,
            completeUrl,
            bodyContents,
            null,
            OAuthRequest.ResponseConverter.IDENTITY)
        .get();
  }

  @Override
  public Response execute(
      String userAgent,
      Map<String, String> headers,
      Verb httpVerb,
      String completeUrl,
      String bodyContents)
      throws InterruptedException, ExecutionException, IOException {
    return executeAsync(
            userAgent,
            headers,
            httpVerb,
            completeUrl,
            bodyContents,
            null,
            OAuthRequest.ResponseConverter.IDENTITY)
        .get();
  }

  @Override
  public Response execute(
      String userAgent,
      Map<String, String> headers,
      Verb httpVerb,
      String completeUrl,
      File bodyContents)
      throws InterruptedException, ExecutionException, IOException {
    return executeAsync(
            userAgent,
            headers,
            httpVerb,
            completeUrl,
            bodyContents,
            null,
            OAuthRequest.ResponseConverter.IDENTITY)
        .get();
  }
}
