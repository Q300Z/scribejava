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

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class RequestObjectServiceTest {

    @Test
    public void shouldCreateSignedRequestObject() throws Exception {
        final RSAKey rsaJWK = new RSAKeyGenerator(2048).keyID("123").generate();
        final String clientId = "my-client-id";
        final String audience = "https://server.example.com";

        final RequestObjectService service =
                new RequestObjectService(clientId, audience, rsaJWK, JWSAlgorithm.RS256);

        final Map<String, String> params = new HashMap<>();
        params.put("response_type", "code");
        params.put("scope", "openid profile");
        params.put("state", "mystate");

        final String requestObject = service.createRequestObject(params);
        assertThat(requestObject).isNotNull();

        final SignedJWT signedJWT = SignedJWT.parse(requestObject);
        assertThat(signedJWT.getJWTClaimsSet().getClaim("client_id")).isEqualTo(clientId);
        assertThat(signedJWT.getJWTClaimsSet().getClaim("response_type")).isEqualTo("code");
        assertThat(signedJWT.getJWTClaimsSet().getClaim("scope")).isEqualTo("openid profile");
        assertThat(signedJWT.getJWTClaimsSet().getAudience().get(0)).isEqualTo(audience);
    }
}
