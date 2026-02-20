package com.github.scribejava.core.httpclient.jdk;

import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.httpclient.multipart.MultipartPayload;
import com.github.scribejava.core.httpclient.multipart.ByteArrayBodyPartPayload;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JDKHttpClientExtendedTest {

    private MockWebServer server;
    private JDKHttpClient client;

    @BeforeEach
    public void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        client = new JDKHttpClient();
    }

    @AfterEach
    public void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    public void shouldExecuteWithPayloadAsString() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("OK"));
        final Response resp = client.execute("UA", Collections.emptyMap(), Verb.POST, server.url("/").toString(),
                "payload-string");
        assertThat(resp.getCode()).isEqualTo(200);
        assertThat(server.takeRequest().getBody().readUtf8()).isEqualTo("payload-string");
    }

    @Test
    public void shouldExecuteAsyncWithPayloadAsString() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("OK"));
        final String result = client.executeAsync("UA", Collections.emptyMap(), Verb.POST, server.url("/").toString(),
                "payload-string", null, response -> response.getBody()).get();
        assertThat(result).isEqualTo("OK");
    }

    @Test
    public void shouldExecuteWithMultipartPayload() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200));
        final MultipartPayload multipart = new MultipartPayload();
        multipart.addBodyPart(new ByteArrayBodyPartPayload("content1".getBytes()));

        final Response resp = client.execute("UA", Collections.emptyMap(), Verb.POST, server.url("/").toString(),
                multipart);
        assertThat(resp.getCode()).isEqualTo(200);
    }

    @Test
    public void shouldThrowExceptionOnFileSync() {
        assertThrows(UnsupportedOperationException.class, () -> client.execute("UA", Collections.emptyMap(), Verb.POST,
                server.url("/").toString(), new java.io.File("dummy")));
    }

    @Test
    public void shouldThrowExceptionOnFileAsync() {
        assertThrows(UnsupportedOperationException.class, () -> client.executeAsync("UA", Collections.emptyMap(),
                Verb.POST, server.url("/").toString(), new java.io.File("dummy"), null, null));
    }
}
