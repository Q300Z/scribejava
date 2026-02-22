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

import com.github.scribejava.core.utils.OAuthEncoder;
import com.github.scribejava.core.utils.Preconditions;
import java.util.*;
import java.util.stream.Collectors;

public class ParameterList {

  private static final char QUERY_STRING_SEPARATOR = '?';
  private static final String PARAM_SEPARATOR = "&";
  private static final String PAIR_SEPARATOR = "=";
  private static final String EMPTY_STRING = "";

  private final List<Parameter> params;

  public ParameterList() {
    params = new ArrayList<>();
  }

  ParameterList(List<Parameter> params) {
    this.params = new ArrayList<>(params);
  }

  public ParameterList(Map<String, String> map) {
    this();
    if (map != null && !map.isEmpty()) {
      map.forEach((key, value) -> params.add(new Parameter(key, value))); // USE Lambda
    }
  }

  public void add(String key, String value) {
    params.add(new Parameter(key, value));
  }

  public String appendTo(String url) {
    Preconditions.checkNotNull(url, "Cannot append to null URL");
    final String queryString = asFormUrlEncodedString();
    if (queryString.equals(EMPTY_STRING)) {
      return url;
    } else {
      return url
          + (url.indexOf(QUERY_STRING_SEPARATOR) == -1 ? QUERY_STRING_SEPARATOR : PARAM_SEPARATOR)
          + queryString;
    }
  }

  public String asOauthBaseString() {
    return OAuthEncoder.encode(asFormUrlEncodedString());
  }

  public String asFormUrlEncodedString() {
    return params.stream()
        .map(Parameter::asUrlEncodedPair)
        .collect(Collectors.joining(PARAM_SEPARATOR)); // USE Stream
  }

  public void addAll(ParameterList other) {
    params.addAll(other.getParams());
  }

  public void addQuerystring(String queryString) {
    if (queryString != null && !queryString.isEmpty()) {
      Arrays.stream(queryString.split(PARAM_SEPARATOR))
          .map(param -> param.split(PAIR_SEPARATOR))
          .forEach(
              pair -> {
                final String key = OAuthEncoder.decode(pair[0]);
                final String value = pair.length > 1 ? OAuthEncoder.decode(pair[1]) : EMPTY_STRING;
                params.add(new Parameter(key, value));
              }); // USE Stream
    }
  }

  public boolean contains(Parameter param) {
    return params.contains(param);
  }

  public int size() {
    return params.size();
  }

  public List<Parameter> getParams() {
    return params;
  }

  public ParameterList sort() {
    final ParameterList sorted = new ParameterList(params);
    Collections.sort(sorted.getParams());
    return sorted;
  }

  public Map<String, String> asMap() {
    final Map<String, String> map = new LinkedHashMap<>();
    for (Parameter param : params) {
      map.put(param.getKey(), param.getValue());
    }
    return map;
  }
}
