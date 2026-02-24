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

import java.util.Optional;

/**
 * Abstraction pour le stockage des jetons. Permet à l'intégrateur de brancher sa propre base de
 * données ou son cache (Redis, etc.).
 *
 * @param <K> Type de la clé d'identification (ex: String pour userId, Long, etc.).
 * @param <T> Type du jeton stocké.
 */
public interface TokenRepository<K, T> {

  /**
   * Récupère un jeton via sa clé.
   *
   * @param key clé unique.
   * @return Optional contenant le jeton ou vide.
   */
  Optional<T> findByKey(K key);

  /**
   * Sauvegarde ou met à jour un jeton.
   *
   * @param key clé unique.
   * @param token jeton à persister.
   */
  void save(K key, T token);

  /**
   * Supprime un jeton (ex: lors d'une déconnexion).
   *
   * @param key clé unique.
   */
  void deleteByKey(K key);
}
