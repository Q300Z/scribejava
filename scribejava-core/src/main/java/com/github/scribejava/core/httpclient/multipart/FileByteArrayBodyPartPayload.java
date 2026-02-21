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
package com.github.scribejava.core.httpclient.multipart;

import com.github.scribejava.core.httpclient.HttpClient;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FileByteArrayBodyPartPayload extends ByteArrayBodyPartPayload {

  public FileByteArrayBodyPartPayload(byte[] payload) {
    this(payload, null);
  }

  public FileByteArrayBodyPartPayload(byte[] payload, int off, int len) {
    this(payload, off, len, null);
  }

  public FileByteArrayBodyPartPayload(byte[] payload, String name) {
    this(payload, name, null);
  }

  public FileByteArrayBodyPartPayload(byte[] payload, int off, int len, String name) {
    this(payload, off, len, name, null);
  }

  public FileByteArrayBodyPartPayload(byte[] payload, String name, String filename) {
    this(null, payload, name, filename);
  }

  public FileByteArrayBodyPartPayload(
      byte[] payload, int off, int len, String name, String filename) {
    this(null, payload, off, len, name, filename);
  }

  public FileByteArrayBodyPartPayload(String contentType, byte[] payload) {
    this(contentType, payload, null);
  }

  public FileByteArrayBodyPartPayload(String contentType, byte[] payload, int off, int len) {
    this(contentType, payload, off, len, null);
  }

  public FileByteArrayBodyPartPayload(String contentType, byte[] payload, String name) {
    this(contentType, payload, name, null);
  }

  public FileByteArrayBodyPartPayload(
      String contentType, byte[] payload, int off, int len, String name) {
    this(contentType, payload, off, len, name, null);
  }

  public FileByteArrayBodyPartPayload(
      String contentType, byte[] payload, String name, String filename) {
    super(payload, composeHeaders(contentType, name, filename));
  }

  public FileByteArrayBodyPartPayload(
      String contentType, byte[] payload, int off, int len, String name, String filename) {
    super(payload, off, len, composeHeaders(contentType, name, filename));
  }

  private static Map<String, String> composeHeaders(
      String contentType, String name, String filename) {

    String contentDispositionHeader = "form-data";
    if (name != null) {
      contentDispositionHeader += "; name=\"" + name + '"';
    }
    if (filename != null) {
      contentDispositionHeader += "; filename=\"" + filename + '"';
    }
    if (contentType == null) {
      return Collections.singletonMap("Content-Disposition", contentDispositionHeader);
    } else {
      final Map<String, String> headers = new HashMap<>();
      headers.put(HttpClient.CONTENT_TYPE, contentType);
      headers.put("Content-Disposition", contentDispositionHeader);
      return headers;
    }
  }
}
