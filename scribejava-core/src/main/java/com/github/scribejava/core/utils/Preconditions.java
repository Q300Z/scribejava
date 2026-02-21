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

/** Utils for checking preconditions and invariants */
public abstract class Preconditions {

  private static final String DEFAULT_MESSAGE = "Received an invalid parameter";

  /**
   * Checks that an object is not null.
   *
   * @param object any object
   * @param errorMsg error message
   * @throws IllegalArgumentException if the object is null
   */
  public static void checkNotNull(Object object, String errorMsg) {
    check(object != null, errorMsg);
  }

  /**
   * Checks that a string is not null or empty
   *
   * @param string any string
   * @param errorMsg error message
   * @throws IllegalArgumentException if the string is null or empty
   */
  public static void checkEmptyString(String string, String errorMsg) {
    check(hasText(string), errorMsg);
  }

  public static boolean hasText(String str) {
    if (str == null || str.isEmpty()) {
      return false;
    }
    final int strLen = str.length();
    for (int i = 0; i < strLen; i++) {
      if (!Character.isWhitespace(str.charAt(i))) {
        return true;
      }
    }
    return false;
  }

  private static void check(boolean requirements, String error) {
    if (!requirements) {
      throw new IllegalArgumentException(hasText(error) ? error : DEFAULT_MESSAGE);
    }
  }
}
