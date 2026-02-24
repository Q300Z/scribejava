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
package com.github.scribejava.core.integration;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registre centralisé pour gérer plusieurs fournisseurs d'identité (Multi-provider). Permet de
 * stocker et récupérer les AuthorizedClientService par identifiant (ex: "google", "github").
 *
 * @param <K> Type de la clé d'identification de l'utilisateur.
 */
public class OAuthServiceRegistry<K> {

  private final Map<String, AuthorizedClientService<K>> services = new ConcurrentHashMap<>();

  /**
   * Enregistre un service pour un fournisseur donné.
   *
   * @param providerId Identifiant unique du fournisseur (ex: "google").
   * @param service Le service configuré.
   */
  public void register(String providerId, AuthorizedClientService<K> service) {
    services.put(Objects.requireNonNull(providerId), Objects.requireNonNull(service));
  }

  /**
   * Récupère le service associé à un fournisseur.
   *
   * @param providerId Identifiant du fournisseur.
   * @return Le service.
   * @throws IllegalArgumentException si le fournisseur n'est pas enregistré.
   */
  public AuthorizedClientService<K> getService(String providerId) {
    final AuthorizedClientService<K> service = services.get(providerId);
    if (service == null) {
      throw new IllegalArgumentException("No OAuth service registered for provider: " + providerId);
    }
    return service;
  }

  /**
   * @return La liste de tous les fournisseurs enregistrés.
   */
  public Map<String, AuthorizedClientService<K>> getAllServices() {
    return services;
  }
}
