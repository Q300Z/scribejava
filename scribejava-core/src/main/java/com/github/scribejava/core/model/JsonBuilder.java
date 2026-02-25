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

import com.github.scribejava.core.utils.JsonUtils;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Builder fluide pour générer du JSON de manière sécurisée. */
public class JsonBuilder {

  private final Map<String, Object> data = new LinkedHashMap<>();

  /**
   * Ajoute une paire clé/valeur.
   *
   * @param key clé
   * @param value valeur (String, Number, List, Map, Instant ou JsonBuilder)
   * @return this
   */
  public JsonBuilder add(String key, Object value) {
    data.put(key, wrapValue(value));
    return this;
  }

  private Object wrapValue(Object value) {
    if (value instanceof JsonBuilder) {
      return ((JsonBuilder) value).asMap();
    }
    if (value instanceof Instant) {
      return ((Instant) value).getEpochSecond();
    }
    if (value instanceof List) {
      final List<?> rawList = (List<?>) value;
      final List<Object> wrappedList = new ArrayList<>(rawList.size());
      for (Object item : rawList) {
        wrappedList.add(wrapValue(item));
      }
      return wrappedList;
    }
    return value;
  }

  /**
   * @return Les données internes sous forme de Map.
   */
  public Map<String, Object> asMap() {
    return data;
  }

  /**
   * @return Le JSON final.
   */
  public String build() {
    return JsonUtils.toJson(data);
  }
}
