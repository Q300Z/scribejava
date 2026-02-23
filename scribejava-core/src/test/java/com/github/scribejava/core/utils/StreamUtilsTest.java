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
package com.github.scribejava.core.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;
import org.junit.jupiter.api.Test;

public class StreamUtilsTest {

  @Test
  public void shouldGetStreamContents() throws IOException {
    final String contents = "hello world";
    final InputStream is = new ByteArrayInputStream(contents.getBytes(StandardCharsets.UTF_8));
    assertThat(StreamUtils.getStreamContents(is)).isEqualTo(contents);
  }

  @Test
  public void shouldGetGzipStreamContents() throws IOException {
    final String contents = "hello world gzip";
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (GZIPOutputStream gzos = new GZIPOutputStream(baos)) {
      gzos.write(contents.getBytes(StandardCharsets.UTF_8));
    }
    final InputStream is = new ByteArrayInputStream(baos.toByteArray());
    assertThat(StreamUtils.getGzipStreamContents(is)).isEqualTo(contents);
  }

  @Test
  public void shouldThrowExceptionOnNullStream() {
    assertThrows(IllegalArgumentException.class, () -> StreamUtils.getStreamContents(null));
    assertThrows(IllegalArgumentException.class, () -> StreamUtils.getGzipStreamContents(null));
  }
}
