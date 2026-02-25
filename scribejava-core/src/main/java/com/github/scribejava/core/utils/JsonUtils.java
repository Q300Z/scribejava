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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Utilitaire JSON natif pour ScribeJava (Zéro Dépendance). */
public final class JsonUtils {

  private JsonUtils() {}

  private static final Pattern JSON_TOKEN_PATTERN =
      Pattern.compile(
          "\"([^\"]*)\"\\s*:\\s*("
              + "\"[^\"]*\"|"
              + "-?\\d+(?:\\.\\d+)?|"
              + "true|false|null|"
              + "\\[[^\\]]*\\]|"
              + "\\{[^}]*\\}"
              + ")");

  /**
   * Parse une chaîne JSON plate.
   *
   * @param json chaîne
   * @return Map
   */
  public static Map<String, Object> parse(String json) {
    final Map<String, Object> result = new LinkedHashMap<>();
    if (json == null) {
      return result;
    }
    final Matcher matcher = JSON_TOKEN_PATTERN.matcher(json);
    while (matcher.find()) {
      final String key = matcher.group(1);
      final String val = matcher.group(2).trim();
      result.put(key, parseValue(val));
    }
    return result;
  }

  private static Object parseValue(String val) {
    if (val.startsWith("\"")) {
      return val.substring(1, val.length() - 1);
    } else if ("true".equals(val)) {
      return Boolean.TRUE;
    } else if ("false".equals(val)) {
      return Boolean.FALSE;
    } else if ("null".equals(val)) {
      return null;
    } else if (val.startsWith("[")) {
      return parseArray(val);
    } else if (val.startsWith("{")) {
      return parse(val);
    } else {
      try {
        if (val.contains(".")) {
          return Double.parseDouble(val);
        }
        return Long.parseLong(val);
      } catch (NumberFormatException e) {
        return val;
      }
    }
  }

  private static List<Object> parseArray(String val) {
    final List<Object> list = new ArrayList<>();
    final String content = val.substring(1, val.length() - 1).trim();
    if (content.isEmpty()) {
      return list;
    }
    for (String part : content.split(",")) {
      list.add(parseValue(part.trim()));
    }
    return list;
  }

  /**
   * Génère du JSON.
   *
   * @param map données
   * @return JSON
   */
  public static String toJson(Map<String, Object> map) {
    final StringBuilder sb = new StringBuilder();
    sb.append('{');
    boolean first = true;
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      if (!first) {
        sb.append(',');
      }
      sb.append('"').append(entry.getKey()).append("\":");
      appendValue(sb, entry.getValue());
      first = false;
    }
    sb.append('}');
    return sb.toString();
  }

  private static void appendValue(StringBuilder sb, Object val) {
    if (val instanceof String) {
      sb.append('"').append(val.toString().replace("\"", "\\\"")).append('"');
    } else if (val instanceof Map) {
      sb.append(toJson((Map<String, Object>) val));
    } else if (val instanceof List) {
      sb.append('[');
      boolean first = true;
      for (Object item : (List<?>) val) {
        if (!first) {
          sb.append(',');
        }
        appendValue(sb, item);
        first = false;
      }
      sb.append(']');
    } else {
      sb.append(val);
    }
  }
}
