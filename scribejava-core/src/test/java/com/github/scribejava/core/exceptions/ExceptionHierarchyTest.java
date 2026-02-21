package com.github.scribejava.core.exceptions;

import com.github.scribejava.core.model.Response;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.Collections;
import static org.assertj.core.api.Assertions.assertThat;

public class ExceptionHierarchyTest {

    @Test
    public void shouldHandleRateLimitException() throws IOException {
        final Response response = new Response(429, "Too Many Requests", Collections.emptyMap(), "retry later");
        final OAuthRateLimitException ex = new OAuthRateLimitException(response);

        assertThat(ex.getResponse().getCode()).isEqualTo(429);
    }

    @Test
    public void shouldHandleProtocolException() {
        final OAuthProtocolException ex = new OAuthProtocolException("Malformed response", new RuntimeException("cause"));
        assertThat(ex.getMessage()).isEqualTo("Malformed response");
        assertThat(ex.getCause()).isInstanceOf(RuntimeException.class);
    }
}
