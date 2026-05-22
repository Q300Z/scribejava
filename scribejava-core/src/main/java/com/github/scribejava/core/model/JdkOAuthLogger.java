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

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Adaptateur permettant de router les logs d'observabilité ScribeJava vers le framework standard de
 * logging JDK (JUL - java.util.logging). Délègue le formatage et le masquage des secrets à {@link
 * DefaultOAuthLogger}.
 */
public class JdkOAuthLogger implements OAuthLogger {

  private final Logger logger;
  private final Level level;

  /**
   * Constructeur par défaut utilisant un logger nommé
   * "com.github.scribejava.core.oauth.OAuthService" et le niveau {@link Level#INFO}.
   */
  public JdkOAuthLogger() {
    this(Logger.getLogger("com.github.scribejava.core.oauth.OAuthService"), Level.INFO);
  }

  /**
   * Constructeur avec Logger personnalisé et niveau INFO.
   *
   * @param logger Le logger JDK à utiliser.
   */
  public JdkOAuthLogger(Logger logger) {
    this(logger, Level.INFO);
  }

  /**
   * Constructeur complet.
   *
   * @param logger Le logger JDK à utiliser.
   * @param level Le niveau de log.
   */
  public JdkOAuthLogger(Logger logger, Level level) {
    this.logger = logger;
    this.level = level;
  }

  @Override
  public void logRequest(OAuthRequest request) {
    if (request == null || !logger.isLoggable(level)) {
      return;
    }
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final DefaultOAuthLogger delegate = new DefaultOAuthLogger(baos);
    delegate.logRequest(request);
    log(baos);
  }

  @Override
  public void logResponse(Response response) {
    if (response == null || !logger.isLoggable(level)) {
      return;
    }
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final DefaultOAuthLogger delegate = new DefaultOAuthLogger(baos);
    delegate.logResponse(response);
    log(baos);
  }

  private void log(ByteArrayOutputStream baos) {
    try {
      final String message = baos.toString(StandardCharsets.UTF_8.name());
      logger.log(level, message);
    } catch (UnsupportedEncodingException e) {
      logger.log(level, baos.toString());
    }
  }
}
