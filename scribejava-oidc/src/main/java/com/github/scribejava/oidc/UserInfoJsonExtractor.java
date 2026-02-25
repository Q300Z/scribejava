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
package com.github.scribejava.oidc;

import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.utils.JsonUtils;
import java.io.IOException;
import java.util.Map;

/** Extracteur JSON natif pour UserInfo OpenID Connect. */
public class UserInfoJsonExtractor {

  protected UserInfoJsonExtractor() {}

  public static UserInfoJsonExtractor instance() {
    return InstanceHolder.INSTANCE;
  }

  /**
   * Extrait les revendications (Claims) à partir de la réponse HTTP.
   *
   * @param response La réponse HTTP.
   * @return Les revendications extraites.
   * @throws IOException si le corps ne peut pas être analysé.
   */
  public StandardClaims extract(final Response response) throws IOException {
    final Map<String, Object> claimsMap = JsonUtils.parse(response.getBody());
    return new StandardClaims(claimsMap);
  }

  private static class InstanceHolder {
    private static final UserInfoJsonExtractor INSTANCE = new UserInfoJsonExtractor();
  }
}
