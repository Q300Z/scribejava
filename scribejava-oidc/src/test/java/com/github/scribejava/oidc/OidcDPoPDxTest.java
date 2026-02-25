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

import static org.mockito.Mockito.mock;

import com.github.scribejava.core.dpop.DPoPProofCreator;
import org.junit.jupiter.api.Test;

/** Tests DX : Support DPoP simplifié. */
public class OidcDPoPDxTest {

  /** Vérifie la configuration fluide de DPoP. */
  @Test
  public void shouldConfigureDPoPFluently() {
    final DPoPProofCreator mockCreator = mock(DPoPProofCreator.class);

    // Point 4 : DX DPoP
    final OidcServiceBuilder builder = new OidcServiceBuilder("client-id");
    builder.dpop(mockCreator);

    // Le test passe si la compilation et l'exécution de l'API fluide fonctionnent
  }
}
