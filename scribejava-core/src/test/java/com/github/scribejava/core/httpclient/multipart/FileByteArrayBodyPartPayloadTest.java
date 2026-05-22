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

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/** Tests unitaires pour FileByteArrayBodyPartPayload. */
public class FileByteArrayBodyPartPayloadTest {

  @Test
  public void testAllConstructorsAndHeaders() {
    final byte[] payload = "Hello World".getBytes(StandardCharsets.UTF_8);

    // Constructor 1
    final FileByteArrayBodyPartPayload p1 = new FileByteArrayBodyPartPayload(payload, "field1");
    assertThat(p1.getHeaders()).containsEntry("Content-Disposition", "form-data; name=\"field1\"");
    assertThat(p1.getPayload()).isEqualTo(payload);

    // Constructor 2
    final FileByteArrayBodyPartPayload p2 = new FileByteArrayBodyPartPayload(payload, 2, 5, "field2");
    assertThat(p2.getHeaders()).containsEntry("Content-Disposition", "form-data; name=\"field2\"");
    assertThat(p2.getPayload()).isEqualTo(payload);

    // Constructor 3
    final FileByteArrayBodyPartPayload p3 = new FileByteArrayBodyPartPayload(payload, "field3", "file.txt");
    assertThat(p3.getHeaders()).containsEntry("Content-Disposition", "form-data; name=\"field3\"; filename=\"file.txt\"");

    // Constructor 4
    final FileByteArrayBodyPartPayload p4 = new FileByteArrayBodyPartPayload(payload, 0, 11, "field4", "file4.txt");
    assertThat(p4.getHeaders()).containsEntry("Content-Disposition", "form-data; name=\"field4\"; filename=\"file4.txt\"");

    // Constructor 5
    final FileByteArrayBodyPartPayload p5 = new FileByteArrayBodyPartPayload("text/plain", payload, "field5");
    assertThat(p5.getHeaders()).containsEntry("Content-Disposition", "form-data; name=\"field5\"");
    assertThat(p5.getHeaders()).containsEntry("Content-Type", "text/plain");

    // Constructor 6
    final FileByteArrayBodyPartPayload p6 = new FileByteArrayBodyPartPayload("image/png", payload, 0, 11, "field6");
    assertThat(p6.getHeaders()).containsEntry("Content-Disposition", "form-data; name=\"field6\"");
    assertThat(p6.getHeaders()).containsEntry("Content-Type", "image/png");

    // Constructor 7
    final FileByteArrayBodyPartPayload p7 = new FileByteArrayBodyPartPayload("image/jpeg", payload, "field7", "pic.jpg");
    assertThat(p7.getHeaders()).containsEntry("Content-Disposition", "form-data; name=\"field7\"; filename=\"pic.jpg\"");
    assertThat(p7.getHeaders()).containsEntry("Content-Type", "image/jpeg");

    // Constructor 8
    final FileByteArrayBodyPartPayload p8 = new FileByteArrayBodyPartPayload("application/pdf", payload, 0, 11, "field8", "doc.pdf");
    assertThat(p8.getHeaders()).containsEntry("Content-Disposition", "form-data; name=\"field8\"; filename=\"doc.pdf\"");
    assertThat(p8.getHeaders()).containsEntry("Content-Type", "application/pdf");
  }
}
