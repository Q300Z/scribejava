package com.github.scribejava.core.httpclient.jdk;

import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class JdkHttpClientVerbsTest {
    private MockWebServer server;
    private JDKHttpClient client;

    @Before
    public void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        client = new JDKHttpClient();
    }

    @After
    public void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    public void testAllVerbs() throws Exception {
        for (Verb verb : Verb.values()) {
            if (verb == Verb.PATCH) {
                continue; // PATCH is not supported by JDK HttpURLConnection
            }
            server.enqueue(new MockResponse().setResponseCode(200));
            final Response resp = client.execute("UA", Collections.emptyMap(), verb, server.url("/").toString(), "");
            assertEquals(200, resp.getCode());
            assertEquals(verb.name(), server.takeRequest().getMethod());
        }
    }
}
