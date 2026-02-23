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
package com.github.scribejava.core.httpclient.jdk;

import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests de résilience et de configuration du client HTTP JDK.
 */
public class JDKHttpClientResilienceTest {

    private MockWebServer server;

    /**
     * Initialisation du serveur.
     */
    @BeforeEach
    public void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    /**
     * Arrêt du serveur.
     */
    @AfterEach
    public void tearDown() throws IOException {
        server.shutdown();
    }

    /**
     * Vérifie la gestion du dépassement de délai de lecture (read timeout).
     */
    @Test
    public void shouldHandleReadTimeout() {
        server.enqueue(new MockResponse().setBody("too late").setBodyDelay(1, TimeUnit.SECONDS));

        final JDKHttpClientConfig config = JDKHttpClientConfig.defaultConfig();
        config.setReadTimeout(10);
        final JDKHttpClient client = new JDKHttpClient(config);

        // We use a retry logic because some OS/JVM combinations are too fast for simple timeout tests
        boolean exceptionThrown = false;
        for (int i = 0; i < 3; i++) {
            try {
                client.execute(
                        "UA", Collections.emptyMap(), Verb.GET, server.url("/").toString(), (byte[]) null);
            } catch (SocketTimeoutException e) {
                exceptionThrown = true;
                break;
            } catch (Exception e) {
                // ignore other errors
            }
        }
        assertThat(exceptionThrown).isTrue();
    }

    /**
     * Vérifie la gestion d'un hôte inconnu.
     */
    @Test
    public void shouldHandleUnknownHost()
            throws InterruptedException, java.util.concurrent.ExecutionException {
        final JDKHttpClient client = new JDKHttpClient();
        assertThatThrownBy(
                () ->
                        client.execute(
                                "UA",
                                Collections.emptyMap(),
                                Verb.GET,
                                "http://bogus-domain-scribejava.invalid",
                                (byte[]) null))
                .isInstanceOf(OAuthException.class)
                .hasMessageContaining("The IP address of a host could not be determined.");
    }

    /**
     * Vérifie que la configuration du proxy est prise en compte.
     */
    @Test
    public void shouldWorkWithProxyConfiguration()
            throws IOException, InterruptedException, java.util.concurrent.ExecutionException {
        final Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 1));
        final JDKHttpClientConfig config = JDKHttpClientConfig.defaultConfig();
        config.setProxy(proxy);
        config.setConnectTimeout(100);
        final JDKHttpClient client = new JDKHttpClient(config);
        assertThatThrownBy(
                () ->
                        client.execute(
                                "UA", Collections.emptyMap(), Verb.GET, "http://google.com", (byte[]) null))
                .isInstanceOf(IOException.class);
    }

    /**
     * Vérifie le support du suivi des redirections.
     */
    @Test
    public void shouldSupportRedirectConfiguration()
            throws IOException, InterruptedException, java.util.concurrent.ExecutionException {
        server.enqueue(
                new MockResponse().setResponseCode(302).setHeader("Location", server.url("/new-location")));
        server.enqueue(new MockResponse().setResponseCode(200).setBody("Final Destination"));

        final JDKHttpClientConfig config = JDKHttpClientConfig.defaultConfig();
        config.setFollowRedirects(true);
        final JDKHttpClient client = new JDKHttpClient(config);

        final Response response =
                client.execute(
                        "UA", Collections.emptyMap(), Verb.GET, server.url("/").toString(), (byte[]) null);
        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("Final Destination");
    }

    /**
     * Vérifie la récupération correcte du flux d'erreur lors d'une réponse HTTP >= 400.
     */
    @Test
    public void shouldHandleErrorStreamWhenResponseCodeIsHigh()
            throws IOException, InterruptedException, java.util.concurrent.ExecutionException {
        server.enqueue(new MockResponse().setResponseCode(404).setBody("Not Found Content"));

        final JDKHttpClient client = new JDKHttpClient();
        final Response response =
                client.execute(
                        "UA", Collections.emptyMap(), Verb.GET, server.url("/").toString(), (byte[]) null);

        assertThat(response.getCode()).isEqualTo(404);
        assertThat(response.getBody()).isEqualTo("Not Found Content");
    }
}
