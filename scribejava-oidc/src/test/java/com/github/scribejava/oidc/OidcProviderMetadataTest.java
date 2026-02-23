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

import static org.junit.Assert.*;

import java.io.IOException;
import org.junit.Test;

public class OidcProviderMetadataTest {

  private static final String JSON_CONFIG =
      "{"
          + "\"issuer\":\"https://server.example.com\","
          + "\"authorization_endpoint\":\"https://server.example.com/authorize\","
          + "\"token_endpoint\":\"https://server.example.com/token\","
          + "\"jwks_uri\":\"https://server.example.com/jwks.json\","
          + "\"response_types_supported\":[\"code\",\"id_token\"],"
          + "\"subject_types_supported\":[\"public\"],"
          + "\"id_token_signing_alg_values_supported\":[\"RS256\"]"
          + "}";

  @Test
  public void shouldParseMetadataFromJson() throws IOException {
    final OidcProviderMetadata metadata = OidcProviderMetadata.parse(JSON_CONFIG);

    assertNotNull(metadata);
    assertEquals("https://server.example.com", metadata.getIssuer());
    assertEquals("https://server.example.com/authorize", metadata.getAuthorizationEndpoint());
    assertEquals("https://server.example.com/token", metadata.getTokenEndpoint());
    assertEquals("https://server.example.com/jwks.json", metadata.getJwksUri());
    assertTrue(metadata.getResponseTypesSupported().contains("code"));
    assertTrue(metadata.getSubjectTypesSupported().contains("public"));
    assertTrue(metadata.getIdTokenSigningAlgValuesSupported().contains("RS256"));
  }
}
