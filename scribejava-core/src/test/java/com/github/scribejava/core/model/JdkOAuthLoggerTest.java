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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Tests unitaires pour {@link JdkOAuthLogger}. */
public class JdkOAuthLoggerTest {

  private Logger testLogger;
  private TestHandler testHandler;

  /** Initialise l'environnement de test avant chaque exécution. */
  @Before
  public void setUp() {
    testLogger = Logger.getLogger("com.github.scribejava.test.logger");
    testLogger.setLevel(Level.ALL);
    testLogger.setUseParentHandlers(false);
    testHandler = new TestHandler();
    testLogger.addHandler(testHandler);
  }

  /** Nettoie l'environnement de test après chaque exécution. */
  @After
  public void tearDown() {
    testLogger.removeHandler(testHandler);
  }

  /** Vérifie la bonne écriture d'une requête HTTP vers le logger JUL. */
  @Test
  public void shouldLogRequestToJul() {
    final JdkOAuthLogger logger = new JdkOAuthLogger(testLogger, Level.INFO);

    final OAuthRequest request = new OAuthRequest(Verb.GET, "http://example.com");
    logger.logRequest(request);

    assertEquals(1, testHandler.records.size());
    final LogRecord record = testHandler.records.get(0);
    assertEquals(Level.INFO, record.getLevel());
    assertTrue(record.getMessage().contains("GET http://example.com"));
  }

  /** Vérifie la bonne écriture d'une réponse HTTP vers le logger JUL. */
  @Test
  public void shouldLogResponseToJul() {
    final JdkOAuthLogger logger = new JdkOAuthLogger(testLogger, Level.WARNING);

    final Response response = new Response(200, "OK", new HashMap<String, String>(), "some_body");
    logger.logResponse(response);

    assertEquals(1, testHandler.records.size());
    final LogRecord record = testHandler.records.get(0);
    assertEquals(Level.WARNING, record.getLevel());
    assertTrue(record.getMessage().contains("Status Code: 200"));
    assertTrue(record.getMessage().contains("some_body"));
  }

  /** Vérifie que le logger ne produit aucun log si son niveau de log configuré est inférieur. */
  @Test
  public void shouldNotLogIfLoggerLevelIsHigherThanConfigured() {
    testLogger.setLevel(Level.WARNING);
    final JdkOAuthLogger logger = new JdkOAuthLogger(testLogger, Level.INFO);

    final OAuthRequest request = new OAuthRequest(Verb.GET, "http://example.com");
    logger.logRequest(request);

    assertTrue(testHandler.records.isEmpty());
  }

  /** Handler de test personnalisé capturant les enregistrements de log. */
  private static class TestHandler extends Handler {
    /** Liste des LogRecord capturés. */
    final List<LogRecord> records = new ArrayList<>();

    @Override
    public void publish(LogRecord record) {
      records.add(record);
    }

    @Override
    public void flush() {}

    @Override
    public void close() throws SecurityException {}
  }
}
