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

import java.util.Map;

public class ByteArrayBodyPartPayload extends BodyPartPayload {

  private final byte[] payload;
  private final int off;
  private final int len;

  public ByteArrayBodyPartPayload(byte[] payload, int off, int len, Map<String, String> headers) {
    super(headers);
    this.payload = payload;
    this.off = off;
    this.len = len;
  }

  public ByteArrayBodyPartPayload(byte[] payload, Map<String, String> headers) {
    this(payload, 0, payload.length, headers);
  }

  public ByteArrayBodyPartPayload(byte[] payload, String contentType) {
    this(payload, convertContentTypeToHeaders(contentType));
  }

  public ByteArrayBodyPartPayload(byte[] payload, int off, int len, String contentType) {
    this(payload, off, len, convertContentTypeToHeaders(contentType));
  }

  public ByteArrayBodyPartPayload(byte[] payload) {
    this(payload, (Map<String, String>) null);
  }

  public ByteArrayBodyPartPayload(byte[] payload, int off, int len) {
    this(payload, off, len, (Map<String, String>) null);
  }

  public byte[] getPayload() {
    return payload;
  }

  public int getOff() {
    return off;
  }

  public int getLen() {
    return len;
  }
}
