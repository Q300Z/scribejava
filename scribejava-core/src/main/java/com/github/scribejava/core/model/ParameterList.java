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

/** Représente une liste de paramètres HTTP (QueryString ou corps de formulaire). */
public class ParameterList {

  private static final char QUERY_STRING_SEPARATOR = '?';
  private static final String PARAM_SEPARATOR = "&";
  private static final String PAIR_SEPARATOR = "=";
  private static final String EMPTY_STRING = "";

  private final List<Parameter> params;

  /** Constructeur par défaut. */
  public ParameterList() {
    params = new ArrayList<>();
  }

  /**
   * Constructeur interne.
   *
   * @param params Liste initiale de paramètres.
   */
  ParameterList(List<Parameter> params) {
    this.params = new ArrayList<>(params);
  }

  /**
   * Constructeur à partir d'une map.
   *
   * @param map Dictionnaire de paramètres.
   */
  public ParameterList(Map<String, String> map) {
    this();
    if (map != null && !map.isEmpty()) {
      map.forEach((key, value) -> params.add(new Parameter(key, value))); // USE Lambda
    }
  }

  /**
   * Ajoute un nouveau paramètre.
   *
   * @param key La clé.
   * @param value La valeur.
   */
  public void add(String key, String value) {
    params.add(new Parameter(key, value));
  }

  /**
   * Ajoute les paramètres à une URL existante.
   *
   * @param url L'URL de base.
   * @return L'URL avec la QueryString ajoutée.
   */
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

  /**
   * Retourne la liste sous forme de chaîne encodée pour la base OAuth (OAuth Base String).
   *
   * @return La chaîne encodée.
   */
  public String asOauthBaseString() {
    return OAuthEncoder.encode(asFormUrlEncodedString());
  }

  /**
   * Retourne la liste sous forme de chaîne encodée application/x-www-form-urlencoded.
   *
   * @return La chaîne résultante.
   */
  public String asFormUrlEncodedString() {
    return params.stream()
        .map(Parameter::asUrlEncodedPair)
        .collect(Collectors.joining(PARAM_SEPARATOR)); // USE Stream
  }

  /**
   * Ajoute tous les paramètres d'une autre liste.
   *
   * @param other L'autre liste de paramètres.
   */
  public void addAll(ParameterList other) {
    params.addAll(other.getParams());
  }

  /**
   * Analyse une QueryString et ajoute les paramètres extraits à la liste.
   *
   * @param queryString La chaîne de requête (ex: "a=1&b=2").
   */
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

  /**
   * Vérifie si la liste contient un paramètre spécifique.
   *
   * @param param Le paramètre à rechercher.
   * @return true si présent, false sinon.
   */
  public boolean contains(Parameter param) {
    return params.contains(param);
  }

  /**
   * Retourne le nombre de paramètres dans la liste.
   *
   * @return La taille de la liste.
   */
  public int size() {
    return params.size();
  }

  /**
   * Retourne la liste brute des paramètres.
   *
   * @return La liste de {@link Parameter}.
   */
  public List<Parameter> getParams() {
    return params;
  }

  /**
   * Retourne une nouvelle liste triée par clé.
   *
   * @return Une instance triée de {@link ParameterList}.
   */
  public ParameterList sort() {
    final ParameterList sorted = new ParameterList(params);
    Collections.sort(sorted.getParams());
    return sorted;
  }

  /**
   * Retourne les paramètres sous forme de Map.
   *
   * @return Une {@link Map} représentant les paramètres.
   */
  public Map<String, String> asMap() {
    final Map<String, String> map = new LinkedHashMap<>();
    for (Parameter param : params) {
      map.put(param.getKey(), param.getValue());
    }
    return map;
  }
}
