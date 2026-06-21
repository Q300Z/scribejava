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

  private static final Pattern UNICODE_PATTERN = Pattern.compile("\\\\u([0-9a-fA-F]{4})");

  private static class Parser {
    private final String src;
    private int cursor;

    Parser(String src) {
      this.src = src;
      this.cursor = 0;
    }

    private void skipWhitespace() {
      while (cursor < src.length() && Character.isWhitespace(src.charAt(cursor))) {
        cursor++;
      }
    }

    Map<String, Object> parseObject(int depth) {
      if (depth > MAX_DEPTH) {
        throw new OAuthException("JSON nesting limit exceeded (max " + MAX_DEPTH + ")");
      }
      try {
        skipWhitespace();
        if (cursor >= src.length() || src.charAt(cursor) != '{') {
          return new LinkedHashMap<>();
        }
        cursor++; // skip '{'
        final Map<String, Object> map = new LinkedHashMap<>();
        while (true) {
          skipWhitespace();
          if (cursor < src.length() && src.charAt(cursor) == '}') {
            cursor++; // skip '}'
            break;
          }
          final String key = parseString();
          skipWhitespace();
          if (cursor >= src.length() || src.charAt(cursor) != ':') {
            break;
          }
          cursor++; // skip ':'
          final Object val = parseValue(depth);
          map.put(key, val);
          skipWhitespace();
          if (cursor < src.length() && src.charAt(cursor) == ',') {
            cursor++; // skip ','
          } else if (cursor < src.length() && src.charAt(cursor) == '}') {
            cursor++; // skip '}'
            break;
          } else {
            break;
          }
        }
        return map;
      } catch (Exception e) {
        if (e instanceof OAuthException && e.getMessage().contains("JSON nesting limit")) {
          throw (OAuthException) e;
        }
        return new LinkedHashMap<>();
      }
    }

    private String parseString() {
      skipWhitespace();
      if (cursor >= src.length() || src.charAt(cursor) != '"') {
        throw new OAuthException("Invalid JSON: expected '\"'");
      }
      cursor++; // skip '"'
      final StringBuilder sb = new StringBuilder();
      while (cursor < src.length()) {
        char c = src.charAt(cursor++);
        if (c == '"') {
          return unescape(sb.toString());
        }
        if (c == '\\') {
          if (cursor >= src.length()) {
            throw new OAuthException("Invalid JSON: escape character at EOF");
          }
          char escaped = src.charAt(cursor++);
          sb.append('\\').append(escaped);
        } else {
          sb.append(c);
        }
      }
      throw new OAuthException("Invalid JSON: unterminated string");
    }

    private Object parseValue(int depth) {
      skipWhitespace();
      if (cursor >= src.length()) {
        throw new OAuthException("Invalid JSON: unexpected EOF");
      }
      char c = src.charAt(cursor);
      if (c == '{') {
        return parseObject(depth + 1);
      }
      if (c == '[') {
        return parseArray(depth + 1);
      }
      if (c == '"') {
        return parseString();
      }
      if (c == 't' || c == 'f' || c == 'n') {
        return parseLiteral();
      }
      if (c == '-' || Character.isDigit(c)) {
        return parseNumber();
      }
      throw new OAuthException("Invalid JSON: unexpected character '" + c + "'");
    }

    private List<Object> parseArray(int depth) {
      if (depth > MAX_DEPTH) {
        throw new OAuthException("JSON nesting limit exceeded (max " + MAX_DEPTH + ")");
      }
      skipWhitespace();
      if (cursor >= src.length() || src.charAt(cursor) != '[') {
        throw new OAuthException("Invalid JSON: expected '['");
      }
      cursor++; // skip '['
      final List<Object> list = new ArrayList<>();
      while (true) {
        skipWhitespace();
        if (cursor < src.length() && src.charAt(cursor) == ']') {
          cursor++; // skip ']'
          break;
        }
        final Object val = parseValue(depth);
        list.add(val);
        skipWhitespace();
        if (cursor < src.length() && src.charAt(cursor) == ',') {
          cursor++; // skip ','
        } else if (cursor < src.length() && src.charAt(cursor) == ']') {
          cursor++; // skip ']'
          break;
        } else {
          throw new OAuthException("Invalid JSON: expected ',' or ']'");
        }
      }
      return list;
    }

    private Object parseLiteral() {
      if (src.startsWith("true", cursor)) {
        cursor += 4;
        return Boolean.TRUE;
      }
      if (src.startsWith("false", cursor)) {
        cursor += 5;
        return Boolean.FALSE;
      }
      if (src.startsWith("null", cursor)) {
        cursor += 4;
        return null;
      }
      throw new OAuthException("Invalid JSON: expected true, false or null");
    }

    private Object parseNumber() {
      final int start = cursor;
      if (src.charAt(cursor) == '-') {
        cursor++;
      }
      while (cursor < src.length()) {
        char c = src.charAt(cursor);
        if (Character.isDigit(c) || c == '.' || c == 'e' || c == 'E' || c == '+' || c == '-') {
          cursor++;
        } else {
          break;
        }
      }
      final String val = src.substring(start, cursor);
      try {
        if (val.contains(".") || val.contains("e") || val.contains("E")) {
          return Double.parseDouble(val);
        }
        return Long.parseLong(val);
      } catch (NumberFormatException e) {
        return val;
      }
    }
  }

  /**
   * Parse une chaîne JSON plate ou simple.
   *
   * @param json chaîne
   * @return Map
   */
  public static Map<String, Object> parse(String json) {
    if (json == null || json.trim().isEmpty()) {
      return new LinkedHashMap<>();
    }
    return new Parser(json).parseObject(0);
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
      final String replacement = new String(Character.toChars(code));
      matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
    }
    matcher.appendTail(sb);
    return sb.toString();
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
