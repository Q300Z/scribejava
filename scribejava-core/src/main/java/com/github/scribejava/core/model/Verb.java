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

/** Énumération contenant les verbes HTTP les plus courants. */
public enum Verb {
  GET(false),
  POST(true),
  PUT(true),
  DELETE(false, true),
  HEAD(false),
  OPTIONS(false),
  TRACE(false),
  PATCH(true);

  private final boolean requiresBody;
  private final boolean permitBody;

  Verb(boolean requiresBody) {
    this(requiresBody, requiresBody);
  }

  Verb(boolean requiresBody, boolean permitBody) {
    if (requiresBody && !permitBody) {
      throw new IllegalArgumentException();
    }
    this.requiresBody = requiresBody;
    this.permitBody = permitBody;
  }

  /**
   * @return true si le verbe nécessite obligatoirement un corps de requête.
   */
  public boolean isRequiresBody() {
    return requiresBody;
  }

  /**
   * @return true si le verbe autorise un corps de requête.
   */
  public boolean isPermitBody() {
    return permitBody;
  }
}
