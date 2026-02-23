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
package com.github.scribejava.core.exceptions;

import com.github.scribejava.core.model.Response;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class ExceptionHierarchyTest {

    @Test
    public void shouldHandleRateLimitException() throws IOException {
        final Response response =
                new Response(429, "Too Many Requests", Collections.emptyMap(), "retry later");
        final OAuthRateLimitException ex = new OAuthRateLimitException(response);

        assertThat(ex.getResponse().getCode()).isEqualTo(429);
    }

    @Test
    public void shouldHandleProtocolException() {
        final OAuthProtocolException ex =
                new OAuthProtocolException("Malformed response", new RuntimeException("cause"));
        assertThat(ex.getMessage()).isEqualTo("Malformed response");
        assertThat(ex.getCause()).isInstanceOf(RuntimeException.class);
    }
}
