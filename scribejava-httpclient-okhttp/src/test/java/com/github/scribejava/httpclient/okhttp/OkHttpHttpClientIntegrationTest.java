package com.github.scribejava.httpclient.okhttp;

import com.github.scribejava.core.model.Verb;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class OkHttpHttpClientIntegrationTest {

    private MockWebServer server;
    private OkHttpHttpClient client;

    @BeforeEach
    public void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        client = new OkHttpHttpClient();
    }

    @AfterEach
    public void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    public void shouldExecuteAsync() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("OK"));
        final String result = client.executeAsync("UA", Collections.emptyMap(), Verb.GET, server.url("/").toString(),
                (byte[]) null, null, response -> response.getBody()).get();
        assertThat(result).isEqualTo("OK");
    }

    @Test
    public void shouldCancelAsyncRequest() throws Exception {
        server.enqueue(new MockResponse().setBody("OK").setBodyDelay(5, java.util.concurrent.TimeUnit.SECONDS));
        final java.util.concurrent.CompletableFuture<String> future = client.executeAsync("UA", Collections.emptyMap(),
                Verb.GET, server.url("/").toString(), (byte[]) null, null, response -> response.getBody());

        future.cancel(true);
        assertThat(future.isCancelled()).isTrue();
    }
}
