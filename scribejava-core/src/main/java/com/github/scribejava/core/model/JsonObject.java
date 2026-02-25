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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Wrapper de Map pour un accès sécurisé aux données JSON. */
public class JsonObject {

  private final Map<String, Object> data;

  /**
   * @param data données
   */
  public JsonObject(Map<String, Object> data) {
    this.data = data != null ? data : Collections.emptyMap();
  }

  /**
   * @param key clé
   * @return String
   */
  public String getString(String key) {
    final Object val = data.get(key);
    return val != null ? val.toString() : null;
  }

  /**
   * @param key clé
   * @return Long
   */
  public Long getLong(String key) {
    final Object val = data.get(key);
    if (val instanceof Number) {
      return ((Number) val).longValue();
    }
    if (val instanceof String) {
      try {
        return Long.parseLong((String) val);
      } catch (NumberFormatException e) {
        return null;
      }
    }
    return null;
  }

  /**
   * Récupère un Instant à partir d'un timestamp en secondes.
   *
   * @param key clé
   * @return Instant ou null
   */
  public Instant getInstant(String key) {
    final Long val = getLong(key);
    return val != null ? Instant.ofEpochSecond(val) : null;
  }

  /**
   * @param key clé
   * @return List de String
   */
  @SuppressWarnings("unchecked")
  public List<String> getStringList(String key) {
    final Object val = data.get(key);
    if (val == null) {
      return Collections.emptyList();
    }
    if (val instanceof List) {
      final List<?> rawList = (List<?>) val;
      final List<String> result = new ArrayList<>(rawList.size());
      for (Object item : rawList) {
        if (item != null) {
          result.add(item.toString());
        }
      }
      return result;
    }
    // Gérer le cas où un seul élément est envoyé au lieu d'une liste
    return Collections.singletonList(val.toString());
  }

  /**
   * @param key clé
   * @return Map ou null
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> getMap(String key) {
    final Object val = data.get(key);
    return val instanceof Map ? (Map<String, Object>) val : null;
  }

  /**
   * @return Les données internes sous forme de Map.
   */
  public Map<String, Object> asMap() {
    return data;
  }
}
