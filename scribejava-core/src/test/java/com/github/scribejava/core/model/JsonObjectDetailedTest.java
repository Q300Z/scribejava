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
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Tests avancés pour JsonObject. */
public class JsonObjectDetailedTest {

  @Test
  public void shouldHandleTypeSafeGetters() {
    final Map<String, Object> data = new HashMap<>();
    data.put("str", "value");
    data.put("num", 123L);
    data.put("list", Arrays.asList("a", "b"));
    data.put("single_as_list", "c");

    final JsonObject json = new JsonObject(data);

    assertThat(json.getString("str")).isEqualTo("value");
    assertThat(json.getLong("num")).isEqualTo(123L);
    assertThat(json.getStringList("list")).containsExactly("a", "b");
    assertThat(json.getStringList("single_as_list")).containsExactly("c");
  }

  @Test
  public void shouldHandleInstant() {
    final Map<String, Object> data = new HashMap<>();
    data.put("ts", 1700000000L);
    data.put("ts_str", "1700000000");

    final JsonObject json = new JsonObject(data);
    final Instant expected = Instant.ofEpochSecond(1700000000L);

    assertThat(json.getInstant("ts")).isEqualTo(expected);
    assertThat(json.getInstant("ts_str")).isEqualTo(expected);
    assertThat(json.getInstant("absent")).isNull();
  }
}
