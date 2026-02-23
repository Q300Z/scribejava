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
package com.github.scribejava.oidc.jar;

import static org.assertj.core.api.Assertions.assertThat;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Tests pour {@link JarAuthorizationRequestConverter}. */
public class JarAuthorizationRequestConverterTest {

  /**
   * @throws Exception Exception
   */
  @Test
  public void shouldConvertParamsToJwt() throws Exception {
    final RSAKey rsaJWK = new RSAKeyGenerator(2048).generate();
    final JarAuthorizationRequestConverter converter =
        new JarAuthorizationRequestConverter(
            "client-id", "https://idp.com", rsaJWK, JWSAlgorithm.RS256);

    final Map<String, String> params = new HashMap<>();
    params.put("scope", "openid profile");
    params.put("client_id", "client-id");

    final Map<String, String> result = converter.convert(params);
    assertThat(result).containsKey("request");
    assertThat(result.get("client_id")).isEqualTo("client-id");
  }

  /**
   * @throws Exception Exception
   */
  @Test
  public void shouldSupportFullConstructor() throws Exception {
    final RSAKey signingKey = new RSAKeyGenerator(2048).keyID("sig").generate();
    final RSAKey encryptionKey = new RSAKeyGenerator(2048).keyID("enc").generate().toPublicJWK();

    final JarAuthorizationRequestConverter converter =
        new JarAuthorizationRequestConverter(
            "client-id",
            "https://idp.com",
            signingKey,
            JWSAlgorithm.RS256,
            encryptionKey,
            com.nimbusds.jose.JWEAlgorithm.RSA_OAEP_256,
            com.nimbusds.jose.EncryptionMethod.A128GCM);

    assertThat(converter).isNotNull();
  }
}
