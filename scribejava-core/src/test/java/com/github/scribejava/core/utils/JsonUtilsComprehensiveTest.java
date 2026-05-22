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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Tests complets de couverture pour JsonUtils. */
public class JsonUtilsComprehensiveTest {

  @Test
  public void testConstructorIsPrivate() throws NoSuchMethodException, IllegalAccessException,
      InstantiationException, InvocationTargetException {
    final Constructor<JsonUtils> constructor = JsonUtils.class.getDeclaredConstructor();
    assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
    constructor.setAccessible(true);
    try {
      constructor.newInstance();
    } catch (InvocationTargetException e) {
      // Ignoré, le constructeur privé est instanciable via réflexion
    }
  }

  @Test
  public void testParseDoubleAndNonNumber() {
    final String json = "{\"doubleVal\":12.34,\"invalidNumber\":9999999999999999999999999999999999999999}";
    final Map<String, Object> result = JsonUtils.parse(json);
    assertThat(result.get("doubleVal")).isEqualTo(12.34d);
    assertThat(result.get("invalidNumber")).isEqualTo("9999999999999999999999999999999999999999");
  }

  @Test
  public void testParseArrays() {
    final String json = "{\"list\":[\"abc\",true,false,null,{\"nested\":123}]}";
    final Map<String, Object> result = JsonUtils.parse(json);

    @SuppressWarnings("unchecked")
    final List<Object> list = (List<Object>) result.get("list");
    assertThat(list).hasSize(5);
    assertThat(list.get(0)).isEqualTo("abc");
    assertThat(list.get(1)).isEqualTo(Boolean.TRUE);
    assertThat(list.get(2)).isEqualTo(Boolean.FALSE);
    assertThat(list.get(3)).isNull();

    @SuppressWarnings("unchecked")
    final Map<String, Object> nested = (Map<String, Object>) list.get(4);
    assertThat(nested.get("nested")).isEqualTo(123L);
  }

  @Test
  public void testToJsonSerialization() {
    final Map<String, Object> map = new LinkedHashMap<>();
    map.put("stringKey", "stringVal");
    map.put("nullKey", null);
    map.put("booleanTrue", true);

    final Map<String, Object> nestedMap = new HashMap<>();
    nestedMap.put("a", "b");
    map.put("nestedMap", nestedMap);

    final List<Object> list = new ArrayList<>();
    list.add("hello");
    list.add(null);
    list.add(456L);
    map.put("listKey", list);

    final String json = JsonUtils.toJson(map);
    assertThat(json).contains("\"stringKey\":\"stringVal\"");
    assertThat(json).contains("\"nullKey\":null");
    assertThat(json).contains("\"booleanTrue\":true");
    assertThat(json).contains("\"nestedMap\":{\"a\":\"b\"}");
    assertThat(json).contains("\"listKey\":[\"hello\",null,456]");
  }
}
