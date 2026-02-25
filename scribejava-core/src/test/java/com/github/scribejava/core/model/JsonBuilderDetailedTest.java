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

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

/** Tests avancés pour JsonBuilder. */
public class JsonBuilderDetailedTest {

  @Test
  public void shouldGenerateEscapedJson() {
    final String json =
        new JsonBuilder()
            .add("name", "John \"The Guru\" Doe")
            .add("path", "C:\\Users")
            .add("age", 30)
            .build();

    assertThat(json).contains("\"name\":\"John \\\"The Guru\\\" Doe\"");
    assertThat(json).contains("\"path\":\"C:\\\\Users\"");
    assertThat(json).contains("\"age\":30");
  }

  @Test
  public void shouldHandleNestedObjects() {
    final String json =
        new JsonBuilder()
            .add("outer", "val")
            .add("inner", new JsonBuilder().add("key", "secret"))
            .build();

    assertThat(json).contains("\"inner\":{\"key\":\"secret\"}");
  }

  @Test
  public void shouldHandleListOfBuilders() {
    final String json =
        new JsonBuilder()
            .add(
                "keys",
                Arrays.asList(new JsonBuilder().add("kid", "1"), new JsonBuilder().add("kid", "2")))
            .build();

    assertThat(json).contains("\"keys\":[{\"kid\":\"1\"},{\"kid\":\"2\"}]");
  }

  @Test
  public void shouldHandleInstant() {
    final Instant now = Instant.ofEpochSecond(1700000000L);
    final String json = new JsonBuilder().add("exp", now).build();

    assertThat(json).contains("\"exp\":1700000000");
  }
}
