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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

/** Utils to deal with Streams. */
public abstract class StreamUtils {

  /**
   * Returns the stream contents as an UTF-8 encoded string
   *
   * @param is input stream
   * @return string contents
   * @throws java.io.IOException in any. SocketTimeout in example
   */
  public static String getStreamContents(InputStream is) throws IOException {
    Preconditions.checkNotNull(is, "Cannot get String from a null object");
    final char[] buffer = new char[0x10000];
    final StringBuilder out = new StringBuilder();
    try (Reader in =
        new InputStreamReader(is, StandardCharsets.UTF_8)) { // USE StandardCharsets.UTF_8
      int read;
      do {
        read = in.read(buffer, 0, buffer.length);
        if (read > 0) {
          out.append(buffer, 0, read);
        }
      } while (read >= 0);
    }
    return out.toString();
  }

  /**
   * Return String content from a gzip stream
   *
   * @param is input stream
   * @return string contents
   * @throws java.io.IOException in any. SocketTimeout in example
   */
  public static String getGzipStreamContents(InputStream is) throws IOException {
    Preconditions.checkNotNull(is, "Cannot get String from a null object");
    final GZIPInputStream gis = new GZIPInputStream(is);
    return getStreamContents(gis);
  }
}
