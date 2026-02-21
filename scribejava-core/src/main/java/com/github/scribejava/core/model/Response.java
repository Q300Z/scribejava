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
package com.github.scribejava.core.model;

import com.github.scribejava.core.utils.StreamUtils;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * An HTTP response.
 *
 * <p>This response may contain a non-null body stream of the HttpUrlConnection. If so, this body
 * must be closed to avoid leaking resources. Use either {@link #getBody()} or {@link #close()} to
 * close the body.
 */
public class Response implements Closeable {

  private final int code;
  private final String message;
  private final Map<String, String> headers;
  private String body;
  private InputStream stream;
  private Closeable[] closeables;
  private boolean closed;

  private Response(int code, String message, Map<String, String> headers) {
    this.code = code;
    this.message = message;
    this.headers = headers;
  }

  public Response(
      int code,
      String message,
      Map<String, String> headers,
      InputStream stream,
      Closeable... closeables) {
    this(code, message, headers);
    this.stream = stream;
    this.closeables = closeables;
  }

  public Response(int code, String message, Map<String, String> headers, String body) {
    this(code, message, headers);
    this.body = body;
  }

  private String parseBodyContents() throws IOException {
    if (stream == null) {
      return null;
    }
    if ("gzip".equals(getHeader("Content-Encoding"))) {
      body = StreamUtils.getGzipStreamContents(stream);
    } else {
      body = StreamUtils.getStreamContents(stream);
    }
    return body;
  }

  public boolean isSuccessful() {
    return code >= 200 && code < 400;
  }

  /**
   * Returns the response body as a string, closing the stream that backs it. Idempotent.
   *
   * @return body as string
   * @throws IOException IO Exception
   */
  public String getBody() throws IOException {
    return body == null ? parseBodyContents() : body;
  }

  /**
   * Obtains the meaningful stream of the HttpUrlConnection, either inputStream or errorInputStream,
   * depending on the status code
   *
   * @return input stream / error stream
   */
  public InputStream getStream() {
    return stream;
  }

  /**
   * Obtains the HTTP status code
   *
   * @return the status code
   */
  public int getCode() {
    return code;
  }

  /**
   * Obtains the HTTP status message. Returns <code>null</code> if the message can not be discerned
   * from the response (not valid HTTP)
   *
   * @return the status message
   */
  public String getMessage() {
    return message;
  }

  /**
   * Obtains a {@link Map} containing the HTTP Response Headers
   *
   * @return headers
   */
  public Map<String, String> getHeaders() {
    return headers;
  }

  /**
   * Obtains a single HTTP Header value, or null if undefined
   *
   * @param name the header name.
   * @return header value or null.
   */
  public String getHeader(String name) {
    return headers.get(name);
  }

  @Override
  public String toString() {
    return "Response{"
        + "code="
        + code
        + ", message='"
        + message
        + '\''
        + ", body='"
        + body
        + '\''
        + ", headers="
        + headers
        + '}';
  }

  @Override
  public void close() throws IOException {
    if (closed) {
      return;
    }
    IOException ioException = null;
    if (closeables != null) {
      for (Closeable closeable : closeables) {
        if (closeable == null) {
          continue;
        }
        try {
          closeable.close();
        } catch (IOException ioE) {
          if (ioException != null) {
            ioException = ioE;
          }
        }
      }
    }
    if (ioException != null) {
      throw ioException;
    }
    closed = true;
  }
}
