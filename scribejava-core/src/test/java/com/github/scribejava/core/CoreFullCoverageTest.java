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

import com.github.scribejava.core.utils.Preconditions;
import com.github.scribejava.core.utils.StreamUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CoreFullCoverageTest {

    @Test
    public void testPreconditions() {
        Preconditions.checkNotNull("not null", "error");
        Preconditions.checkEmptyString("not empty", "error");

        final IllegalArgumentException ex1 =
                assertThrows(
                        IllegalArgumentException.class, () -> Preconditions.checkNotNull(null, "is null"));
        assertThat(ex1.getMessage()).isEqualTo("is null");

        final IllegalArgumentException ex2 =
                assertThrows(
                        IllegalArgumentException.class, () -> Preconditions.checkEmptyString("", "is empty"));
        assertThat(ex2.getMessage()).isEqualTo("is empty");

        final IllegalArgumentException ex3 =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> Preconditions.checkEmptyString(null, "is null string"));
        assertThat(ex3.getMessage()).isEqualTo("is null string");
    }

    @Test
    public void testStreamUtils() throws Exception {
        final String data = "hello world";
        try (InputStream is = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8))) {
            assertThat(StreamUtils.getStreamContents(is)).isEqualTo(data);
        }
    }
}
