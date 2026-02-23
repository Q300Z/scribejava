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
package com.github.scribejava.oidc.builder;

import com.github.scribejava.oidc.jar.JarAuthorizationRequestConverter;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests pour {@link OidcServiceBuilder}.
 */
public class OidcServiceBuilderTest {

    /**
     * @throws Exception Exception
     */
    @Test
    public void shouldConfigureJarConverter() throws Exception {
        final RSAKey rsaJWK = new RSAKeyGenerator(2048).generate();
        final OidcServiceBuilder builder = new OidcServiceBuilder("client-id");

        builder.jwtSecuredAuthorizationRequest("https://idp.com", rsaJWK, JWSAlgorithm.RS256);

        final JarAuthorizationRequestConverter converter =
                (JarAuthorizationRequestConverter) builder.getAuthorizationRequestConverter();
        assertThat(converter).isNotNull();

        final Map<String, String> params = new HashMap<>();
        params.put("client_id", "client-id");
        params.put("response_type", "code");

        final Map<String, String> converted = converter.convert(params);
        assertThat(converted).containsKey("request");
        assertThat(converted.get("client_id")).isEqualTo("client-id");
    }

    /**
     * @throws Exception Exception
     */
    @Test
    public void shouldConfigureJarConverterWithSupplier() throws Exception {
        final RSAKey rsaJWK = new RSAKeyGenerator(2048).generate();
        final OidcServiceBuilder builder = new OidcServiceBuilder("client-id");

        builder.jwtSecuredAuthorizationRequest("https://idp.com", () -> rsaJWK, JWSAlgorithm.RS256);

        assertThat(builder.getAuthorizationRequestConverter())
                .isInstanceOf(JarAuthorizationRequestConverter.class);
    }
}
