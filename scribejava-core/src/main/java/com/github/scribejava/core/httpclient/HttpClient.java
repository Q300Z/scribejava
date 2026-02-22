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

import com.github.scribejava.core.httpclient.multipart.MultipartPayload;
import com.github.scribejava.core.model.OAuthAsyncRequestCallback;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Interface définissant le contrat pour les clients HTTP utilisés par ScribeJava.
 *
 * <p>Cette abstraction permet à la bibliothèque d'utiliser différentes implémentations réseau (JDK
 * standard, OkHttp, Armeria, etc.) de manière transparente.
 */
public interface HttpClient extends Closeable {

  /** Type de contenu par défaut pour les requêtes OAuth. */
  String DEFAULT_CONTENT_TYPE = "application/x-www-form-urlencoded";
  /** En-tête HTTP Content-Type. */
  String CONTENT_TYPE = "Content-Type";
  /** En-tête HTTP Content-Length. */
  String CONTENT_LENGTH = "Content-Length";

  /**
   * Exécute une requête de manière asynchrone avec un corps sous forme de tableau d'octets.
   *
   * @param userAgent Chaîne User-Agent.
   * @param headers En-têtes HTTP.
   * @param httpVerb Verbe HTTP.
   * @param completeUrl URL complète.
   * @param bodyContents Contenu du corps.
   * @param callback Rappel optionnel.
   * @param converter Convertisseur de réponse.
   * @param <T> Type de l'objet retourné après conversion.
   * @return Un {@link CompletableFuture} de type T.
   */
  <T> CompletableFuture<T> executeAsync(
      String userAgent,
      Map<String, String> headers,
      Verb httpVerb,
      String completeUrl,
      byte[] bodyContents,
      OAuthAsyncRequestCallback<T> callback,
      OAuthRequest.ResponseConverter<T> converter);

  /**
   * Exécute une requête de manière asynchrone avec un corps multipart.
   *
   * @param userAgent Chaîne User-Agent.
   * @param headers En-têtes HTTP.
   * @param httpVerb Verbe HTTP.
   * @param completeUrl URL complète.
   * @param bodyContents Charge utile multipart.
   * @param callback Rappel optionnel.
   * @param converter Convertisseur de réponse.
   * @param <T> Type cible.
   * @return Un {@link CompletableFuture}.
   */
  <T> CompletableFuture<T> executeAsync(
      String userAgent,
      Map<String, String> headers,
      Verb httpVerb,
      String completeUrl,
      MultipartPayload bodyContents,
      OAuthAsyncRequestCallback<T> callback,
      OAuthRequest.ResponseConverter<T> converter);

  /**
   * Exécute une requête de manière asynchrone avec un corps textuel.
   *
   * @param userAgent Chaîne User-Agent.
   * @param headers En-têtes HTTP.
   * @param httpVerb Verbe HTTP.
   * @param completeUrl URL complète.
   * @param bodyContents Contenu texte.
   * @param callback Rappel optionnel.
   * @param converter Convertisseur de réponse.
   * @param <T> Type cible.
   * @return Un {@link CompletableFuture}.
   */
  <T> CompletableFuture<T> executeAsync(
      String userAgent,
      Map<String, String> headers,
      Verb httpVerb,
      String completeUrl,
      String bodyContents,
      OAuthAsyncRequestCallback<T> callback,
      OAuthRequest.ResponseConverter<T> converter);

  /**
   * Exécute une requête de manière asynchrone en envoyant un fichier.
   *
   * @param userAgent Chaîne User-Agent.
   * @param headers En-têtes HTTP.
   * @param httpVerb Verbe HTTP.
   * @param completeUrl URL complète.
   * @param bodyContents Fichier à envoyer.
   * @param callback Rappel optionnel.
   * @param converter Convertisseur de réponse.
   * @param <T> Type cible.
   * @return Un {@link CompletableFuture}.
   */
  <T> CompletableFuture<T> executeAsync(
      String userAgent,
      Map<String, String> headers,
      Verb httpVerb,
      String completeUrl,
      File bodyContents,
      OAuthAsyncRequestCallback<T> callback,
      OAuthRequest.ResponseConverter<T> converter);

  /**
   * Exécute une requête de manière synchrone avec un corps sous forme de tableau d'octets.
   *
   * @param userAgent Chaîne User-Agent.
   * @param headers En-têtes HTTP.
   * @param httpVerb Verbe HTTP.
   * @param completeUrl URL complète.
   * @param bodyContents Contenu du corps.
   * @return La réponse HTTP {@link Response}.
   * @throws InterruptedException si le thread est interrompu.
   * @throws ExecutionException si la requête échoue.
   * @throws IOException en cas d'erreur réseau.
   */
  Response execute(
      String userAgent,
      Map<String, String> headers,
      Verb httpVerb,
      String completeUrl,
      byte[] bodyContents)
      throws InterruptedException, ExecutionException, IOException;

  /**
   * Exécute une requête de manière synchrone avec un corps multipart.
   *
   * @param userAgent Chaîne User-Agent.
   * @param headers En-têtes HTTP.
   * @param httpVerb Verbe HTTP.
   * @param completeUrl URL complète.
   * @param bodyContents Charge utile multipart.
   * @return La réponse HTTP.
   * @throws InterruptedException si interruption.
   * @throws ExecutionException si échec.
   * @throws IOException si erreur réseau.
   */
  Response execute(
      String userAgent,
      Map<String, String> headers,
      Verb httpVerb,
      String completeUrl,
      MultipartPayload bodyContents)
      throws InterruptedException, ExecutionException, IOException;

  /**
   * Exécute une requête de manière synchrone avec un corps textuel.
   *
   * @param userAgent Chaîne User-Agent.
   * @param headers En-têtes HTTP.
   * @param httpVerb Verbe HTTP.
   * @param completeUrl URL complète.
   * @param bodyContents Contenu texte.
   * @return La réponse HTTP.
   * @throws InterruptedException si interruption.
   * @throws ExecutionException si échec.
   * @throws IOException si erreur réseau.
   */
  Response execute(
      String userAgent,
      Map<String, String> headers,
      Verb httpVerb,
      String completeUrl,
      String bodyContents)
      throws InterruptedException, ExecutionException, IOException;

  /**
   * Exécute une requête de manière synchrone en envoyant un fichier.
   *
   * @param userAgent Chaîne User-Agent.
   * @param headers En-têtes HTTP.
   * @param httpVerb Verbe HTTP.
   * @param completeUrl URL complète.
   * @param bodyContents Fichier à envoyer.
   * @return La réponse HTTP.
   * @throws InterruptedException si interruption.
   * @throws ExecutionException si échec.
   * @throws IOException si erreur réseau.
   */
  Response execute(
      String userAgent,
      Map<String, String> headers,
      Verb httpVerb,
      String completeUrl,
      File bodyContents)
      throws InterruptedException, ExecutionException, IOException;
}
