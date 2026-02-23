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

import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.httpclient.HttpClientConfig;
import com.github.scribejava.core.httpclient.HttpClientProvider;
import com.github.scribejava.core.httpclient.jdk.JDKHttpClient;
import com.github.scribejava.core.httpclient.jdk.JDKHttpClientConfig;
import com.github.scribejava.core.model.OAuthAsyncRequestCallback;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Classe de base abstraite pour tous les services OAuth.
 *
 * <p>Cette classe fournit les fonctionnalités communes pour l'exécution des requêtes HTTP, la
 * gestion de la configuration du client et le support des intercepteurs.
 */
public abstract class OAuthService implements Closeable {

    private final String apiKey;
    private final String apiSecret;
    private final String callback;
    private final String userAgent;
    private final HttpClient httpClient;
    private final OutputStream debugStream;
    private final List<OAuthRequestInterceptor> interceptors = new ArrayList<>(); // ADDED

    /**
     * Constructeur.
     *
     * @param apiKey           La clé API du client.
     * @param apiSecret        Le secret API du client.
     * @param callback         L'URL de rappel.
     * @param debugStream      Flux pour les logs de débogage.
     * @param userAgent        Chaîne User-Agent.
     * @param httpClientConfig Configuration du client HTTP.
     * @param httpClient       L'implémentation du client HTTP.
     */
    public OAuthService(
            String apiKey,
            String apiSecret,
            String callback,
            OutputStream debugStream,
            String userAgent,
            HttpClientConfig httpClientConfig,
            HttpClient httpClient) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.callback = callback;
        this.debugStream = debugStream;
        this.userAgent = userAgent;

        if (httpClientConfig == null && httpClient == null) {
            this.httpClient = new JDKHttpClient(JDKHttpClientConfig.defaultConfig());
        } else {
            this.httpClient = httpClient == null ? getClient(httpClientConfig) : httpClient;
        }
    }

    private static HttpClient getClient(HttpClientConfig config) {
        for (HttpClientProvider provider : ServiceLoader.load(HttpClientProvider.class)) {
            final HttpClient client = provider.createClient(config);
            if (client != null) {
                return client;
            }
        }
        return null;
    }

    /**
     * Ajoute un intercepteur de requête.
     *
     * @param interceptor L'intercepteur à ajouter.
     */
    public void addInterceptor(OAuthRequestInterceptor interceptor) { // ADDED
        interceptors.add(interceptor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        httpClient.close();
    }

    /**
     * Retourne la clé API (Client ID).
     *
     * @return La clé API.
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Retourne le secret API (Client Secret).
     *
     * @return Le secret API.
     */
    public String getApiSecret() {
        return apiSecret;
    }

    /**
     * Retourne l'URL de rappel (Redirect URI).
     *
     * @return L'URL de rappel.
     */
    public String getCallback() {
        return callback;
    }

    /**
     * Retourne la version du protocole OAuth supportée par ce service.
     *
     * @return La version du protocole (ex: "1.0", "2.0").
     */
    public abstract String getVersion();

    /**
     * Exécute une requête OAuth de manière asynchrone.
     *
     * @param request La requête à exécuter.
     * @return Un {@link CompletableFuture} résolvant vers la réponse.
     */
    public CompletableFuture<Response> executeAsync(OAuthRequest request) {
        return execute(request, null);
    }

    /**
     * Exécute une requête OAuth de manière asynchrone avec un rappel.
     *
     * @param request  La requête à exécuter.
     * @param callback Le rappel à invoquer une fois la réponse reçue.
     * @return Un {@link CompletableFuture}.
     */
    public CompletableFuture<Response> execute(
            OAuthRequest request, OAuthAsyncRequestCallback<Response> callback) {
        return execute(request, callback, null);
    }

    /**
     * Exécute une requête OAuth de manière asynchrone avec un rappel et un convertisseur de réponse.
     *
     * @param <R>       Le type de l'objet converti.
     * @param request   La requête.
     * @param callback  Le rappel.
     * @param converter Le convertisseur de réponse.
     * @return Un {@link CompletableFuture}.
     */
    public <R> CompletableFuture<R> execute(
            OAuthRequest request,
            OAuthAsyncRequestCallback<R> callback,
            OAuthRequest.ResponseConverter<R> converter) {

        interceptors.forEach(
                interceptor -> interceptor.intercept(request)); // ADDED: Execute interceptors

        final File filePayload = request.getFilePayload();
        if (filePayload != null) {
            return httpClient.executeAsync(
                    userAgent,
                    request.getHeaders(),
                    request.getVerb(),
                    request.getCompleteUrl(),
                    filePayload,
                    callback,
                    converter);
        } else if (request.getStringPayload() != null) {
            return httpClient.executeAsync(
                    userAgent,
                    request.getHeaders(),
                    request.getVerb(),
                    request.getCompleteUrl(),
                    request.getStringPayload(),
                    callback,
                    converter);
        } else if (request.getMultipartPayload() != null) {
            return httpClient.executeAsync(
                    userAgent,
                    request.getHeaders(),
                    request.getVerb(),
                    request.getCompleteUrl(),
                    request.getMultipartPayload(),
                    callback,
                    converter);
        } else {
            return httpClient.executeAsync(
                    userAgent,
                    request.getHeaders(),
                    request.getVerb(),
                    request.getCompleteUrl(),
                    request.getByteArrayPayload(),
                    callback,
                    converter);
        }
    }

    /**
     * Exécute une requête OAuth de manière synchrone.
     *
     * @param request La requête à exécuter.
     * @return La réponse reçue.
     * @throws InterruptedException si le thread est interrompu.
     * @throws ExecutionException   si l'exécution échoue.
     * @throws IOException          en cas d'erreur réseau.
     */
    public Response execute(OAuthRequest request)
            throws InterruptedException, ExecutionException, IOException {
        interceptors.forEach(
                interceptor -> interceptor.intercept(request)); // ADDED: Execute interceptors

        final File filePayload = request.getFilePayload();
        if (filePayload != null) {
            return httpClient.execute(
                    userAgent,
                    request.getHeaders(),
                    request.getVerb(),
                    request.getCompleteUrl(),
                    filePayload);
        } else if (request.getStringPayload() != null) {
            return httpClient.execute(
                    userAgent,
                    request.getHeaders(),
                    request.getVerb(),
                    request.getCompleteUrl(),
                    request.getStringPayload());
        } else if (request.getMultipartPayload() != null) {
            return httpClient.execute(
                    userAgent,
                    request.getHeaders(),
                    request.getVerb(),
                    request.getCompleteUrl(),
                    request.getMultipartPayload());
        } else {
            return httpClient.execute(
                    userAgent,
                    request.getHeaders(),
                    request.getVerb(),
                    request.getCompleteUrl(),
                    request.getByteArrayPayload());
        }
    }

    /**
     * Loggue un message dans le flux de débogage.
     *
     * @param message Le message à logguer.
     */
    public void log(String message) {
        if (debugStream != null) {
            log(message, (Object[]) null);
        }
    }

    /**
     * Loggue un message formaté dans le flux de débogage.
     *
     * @param messagePattern Le motif du message.
     * @param params         Les paramètres du formatage.
     */
    public void log(String messagePattern, Object... params) {
        final String message =
                params == null || params.length == 0
                        ? messagePattern
                        : String.format(messagePattern, params);
        final String messageWithNewline = message + '\n';
        try {
            debugStream.write(messageWithNewline.getBytes(StandardCharsets.UTF_8));
        } catch (IOException | RuntimeException e) {
            throw new RuntimeException("there were problems while writting to the debug stream", e);
        }
    }

    /**
     * Indique si le mode débogage est activé.
     *
     * @return true si activé, false sinon.
     */
    protected boolean isDebug() {
        return debugStream != null;
    }
}
