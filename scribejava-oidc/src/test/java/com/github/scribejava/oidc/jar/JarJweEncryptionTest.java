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

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.SignedJWT;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class JarJweEncryptionTest {

  @Test
  public void shouldProduceNestedJwtWhenEncryptionIsEnabled() throws Exception {
    // 1. Generate Signing Key (Client Private Key)
    RSAKey signingKey = new RSAKeyGenerator(2048).keyID("sig-1").generate();

    // 2. Generate Encryption Key (Server Public Key)
    RSAKey encryptionKey = new RSAKeyGenerator(2048).keyID("enc-1").generate();

    // 3. Configure Converter with Encryption
    // Note: This constructor doesn't exist yet!
    JarAuthorizationRequestConverter converter =
        new JarAuthorizationRequestConverter(
            "client-id",
            "https://issuer",
            signingKey,
            JWSAlgorithm.RS256,
            encryptionKey.toPublicJWK(), // Encryption Key
            JWEAlgorithm.RSA_OAEP_256, // Key Algo
            EncryptionMethod.A128GCM // Content Algo
            );

    Map<String, String> params = new HashMap<>();
    params.put("foo", "bar");

    // 4. Convert
    Map<String, String> result = converter.convert(params);
    String requestJwt = result.get("request");

    // 5. Verify it is a JWE (5 parts)
    assertThat(requestJwt.split("\\.")).hasSize(5);

    // 6. Decrypt and Verify
    EncryptedJWT jwe = EncryptedJWT.parse(requestJwt);
    jwe.decrypt(new RSADecrypter(encryptionKey));

    SignedJWT jws = jwe.getPayload().toSignedJWT();
    assertThat(jws).isNotNull();
    assertThat(jws.verify(new RSASSAVerifier(signingKey.toPublicJWK()))).isTrue();
    assertThat(jws.getJWTClaimsSet().getStringClaim("foo")).isEqualTo("bar");
  }
}
