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
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Tests de robustesse pour JsonUtils (TDD). */
public class JsonUtilsRobustnessTest {

  @Test
  public void shouldHandleExoticWhitespace() {
    final String json = "{\n\t\"key\"  : \r \"value\"\n}";
    final Map<String, Object> result = JsonUtils.parse(json);
    assertThat(result).containsEntry("key", "value");
  }

  @Test
  public void shouldHandleEscapedQuotes() {
    final String json = "{\"message\":\"Hello \\\"World\\\"\"}";
    final Map<String, Object> result = JsonUtils.parse(json);
    assertThat(result.get("message")).isEqualTo("Hello \"World\"");
  }

  @Test
  public void shouldHandleNullValues() {
    final String json = "{\"empty\":null}";
    final Map<String, Object> result = JsonUtils.parse(json);
    assertThat(result).containsKey("empty");
    assertThat(result.get("empty")).isNull();
  }

  @Test
  public void shouldHandleLargeNumbers() {
    final String json = "{\"large\":9223372036854775807}";
    final Map<String, Object> result = JsonUtils.parse(json);
    assertThat(result.get("large")).isEqualTo(Long.MAX_VALUE);
  }

  @Test
  public void shouldReturnEmptyMapOnNullOrEmptyInput() {
    assertThat(JsonUtils.parse(null)).isEmpty();
    assertThat(JsonUtils.parse("")).isEmpty();
    assertThat(JsonUtils.parse("   ")).isEmpty();
  }

  @Test
  public void shouldHandleSimpleJsonGenerationWithEscapes() {
    final Map<String, Object> data = Collections.singletonMap("key", "val\"ue");
    final String json = JsonUtils.toJson(data);
    assertThat(json).isEqualTo("{\"key\":\"val\\\"ue\"}");
  }

  @Test
  public void shouldNotThrowOnMalformedJsonButReturnPartialOrEmpty() {
    assertThatCode(() -> JsonUtils.parse("{invalid")).doesNotThrowAnyException();
  }
}
