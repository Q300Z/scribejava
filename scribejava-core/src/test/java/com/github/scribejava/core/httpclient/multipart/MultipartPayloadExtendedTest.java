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

import java.util.Collections;
import org.junit.jupiter.api.Test;

/** Tests étendus pour les payloads multipart. */
public class MultipartPayloadExtendedTest {

  /** Vérifie l'ajout d'une partie simple. */
  @Test
  public void shouldHandleBodyPart() {
    final MultipartPayload payload = new MultipartPayload();
    final BodyPartPayload part =
        new ByteArrayBodyPartPayload(
            new byte[0],
            Collections.singletonMap("Content-Disposition", "form-data; name=\"foo\""));
    payload.addBodyPart(part);

    assertThat(payload.getBodyParts()).hasSize(1);
    assertThat(payload.getBoundary()).isNotNull();
  }

  /** Vérifie le support des multiparts imbriqués. */
  @Test
  public void shouldHandleNestedMultipart() {
    final MultipartPayload parent = new MultipartPayload("parent_boundary");
    final MultipartPayload child = new MultipartPayload("child_boundary");

    parent.addBodyPart(child);
    assertThat(parent.getBodyParts()).contains(child);
  }
}
