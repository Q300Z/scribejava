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

import com.github.scribejava.core.exceptions.OAuthException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Utilitaire JSON natif robuste pour ScribeJava (Zéro Dépendance). */
public final class JsonUtils {

  private static final int MAX_DEPTH = 32;

  private JsonUtils() {}

  // Regex supportant les guillemets échappés dans les clés et les valeurs
  private static final Pattern JSON_TOKEN_PATTERN =
      Pattern.compile(
          "\"((?:\\\\\"|[^\"])*)\"\\s*:\\s*("
              + "\"((?:\\\\\"|[^\"])*)\"|"
              + "(-?\\d+(?:\\.\\d+)?)|"
              + "(true|false|null)|"
              + "(\\[[^\\]]*\\])|"
              + "(\\{[^}]*\\})"
              + ")");

  private static final Pattern UNICODE_PATTERN = Pattern.compile("\\\\u([0-9a-fA-F]{4})");

  /**
   * Parse une chaîne JSON plate ou simple.
   *
   * @param json chaîne
   * @return Map
   */
  public static Map<String, Object> parse(String json) {
    return parse(json, 0);
  }

  private static Map<String, Object> parse(String json, int depth) {
    if (depth > MAX_DEPTH) {
      throw new OAuthException("JSON nesting limit exceeded (max " + MAX_DEPTH + ")");
    }
    final Map<String, Object> result = new LinkedHashMap<>();
    if (json == null || json.trim().isEmpty()) {
      return result;
    }
    final Matcher matcher = JSON_TOKEN_PATTERN.matcher(json);
    while (matcher.find()) {
      final String key = unescape(matcher.group(1));
      final String fullVal = matcher.group(2).trim();

      if (fullVal.startsWith("\"")) {
        result.put(key, unescape(matcher.group(3)));
      } else if (matcher.group(4) != null) { // Number
        result.put(key, parseNumber(matcher.group(4)));
      } else if (matcher.group(5) != null) { // Boolean/Null
        result.put(key, parseLiteral(matcher.group(5)));
      } else if (matcher.group(6) != null) { // Array
        result.put(key, parseArray(matcher.group(6), depth + 1));
      } else if (matcher.group(7) != null) { // Object
        result.put(key, parse(matcher.group(7), depth + 1));
      }
    }
    return result;
  }

  private static String unescape(String val) {
    if (val == null) {
      return null;
    }
    String result = val.replace("\\\"", "\"").replace("\\\\", "\\");
    final Matcher matcher = UNICODE_PATTERN.matcher(result);
    final StringBuffer sb = new StringBuffer();
    while (matcher.find()) {
      final int code = Integer.parseInt(matcher.group(1), 16);
      matcher.appendReplacement(sb, new String(Character.toChars(code)));
    }
    matcher.appendTail(sb);
    return sb.toString();
  }

  private static Object parseLiteral(String val) {
    if ("true".equals(val)) {
      return Boolean.TRUE;
    }
    if ("false".equals(val)) {
      return Boolean.FALSE;
    }
    return null;
  }

  private static Object parseNumber(String val) {
    try {
      if (val.contains(".")) {
        return Double.parseDouble(val);
      }
      return Long.parseLong(val);
    } catch (NumberFormatException e) {
      return val;
    }
  }

  private static List<Object> parseArray(String val, int depth) {
    final List<Object> list = new ArrayList<>();
    final String content = val.substring(1, val.length() - 1).trim();
    if (content.isEmpty()) {
      return list;
    }

    // Pour supporter les objets dans les listes, on utilise une approche simple :
    // Si l'élément commence par '{', on tente un parse d'objet.
    // NOTE: Cette implémentation simplifiée suppose des éléments séparés par des virgules
    // sans virgules à l'intérieur des chaînes de caractères des objets (limitation regex actuelle).
    for (String part : splitJsonArray(content)) {
      final String p = part.trim();
      if (p.startsWith("{")) {
        list.add(parse(p, depth + 1));
      } else if (p.startsWith("[")) {
        list.add(parseArray(p, depth + 1));
      } else if (p.startsWith("\"")) {
        list.add(unescape(p.substring(1, p.length() - 1)));
      } else {
        list.add(parseLiteral(p));
      }
    }
    return list;
  }

  private static List<String> splitJsonArray(String content) {
    final List<String> parts = new ArrayList<>();
    int bracketLevel = 0;
    int braceLevel = 0;
    boolean inQuotes = false;
    int start = 0;
    for (int i = 0; i < content.length(); i++) {
      char c = content.charAt(i);
      if (c == '\"' && (i == 0 || content.charAt(i - 1) != '\\')) {
        inQuotes = !inQuotes;
      }
      if (!inQuotes) {
        if (c == '[') {
          bracketLevel++;
        } else if (c == ']') {
          bracketLevel--;
        } else if (c == '{') {
          braceLevel++;
        } else if (c == '}') {
          braceLevel--;
        } else if (c == ',' && bracketLevel == 0 && braceLevel == 0) {
          parts.add(content.substring(start, i));
          start = i + 1;
        }
      }
    }
    parts.add(content.substring(start));
    return parts;
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
      sb.append('"').append(escape(entry.getKey())).append("\":");
      appendValue(sb, entry.getValue());
      first = false;
    }
    sb.append('}');
    return sb.toString();
  }

  private static void appendValue(StringBuilder sb, Object val) {
    if (val == null) {
      sb.append("null");
    } else if (val instanceof String) {
      sb.append('"').append(escape(val.toString())).append('"');
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

  private static String escape(String val) {
    if (val == null) {
      return null;
    }
    return val.replace("\\", "\\\\").replace("\"", "\\\"");
  }
}
