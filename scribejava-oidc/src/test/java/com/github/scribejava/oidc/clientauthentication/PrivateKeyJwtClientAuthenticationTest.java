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
package com.github.scribejava.oidc.clientauthentication;

import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PrivateKeyJwtClientAuthenticationTest {

    @Test
    public void shouldAddClientAssertionToRequest() throws Exception {
        final RSAKey rsaJWK = new RSAKeyGenerator(2048).keyID("123").generate();
        final String clientId = "my-client-id";
        final String audience = "https://server.example.com/token";

        final PrivateKeyJwtClientAuthentication auth =
                new PrivateKeyJwtClientAuthentication(clientId, audience, rsaJWK, JWSAlgorithm.RS256);

        final OAuthRequest request = new OAuthRequest(Verb.POST, audience);
        auth.addClientAuthentication(request);

        assertThat(getParam(request, "client_assertion_type"))
                .isEqualTo("urn:ietf:params:oauth:client-assertion-type:jwt-bearer");

        final String assertion = getParam(request, "client_assertion");
        assertThat(assertion).isNotNull();

        final SignedJWT signedJWT = SignedJWT.parse(assertion);
        assertThat(signedJWT.getJWTClaimsSet().getSubject()).isEqualTo(clientId);
        assertThat(signedJWT.getJWTClaimsSet().getIssuer()).isEqualTo(clientId);
        assertThat(signedJWT.getJWTClaimsSet().getAudience()).contains(audience);
    }

    @Test
    public void shouldAddClientAssertionWithEC() throws Exception {
        final com.nimbusds.jose.jwk.ECKey ecJWK =
                new com.nimbusds.jose.jwk.gen.ECKeyGenerator(com.nimbusds.jose.jwk.Curve.P_256)
                        .keyID("456")
                        .generate();
        final String clientId = "ec-client";
        final String audience = "https://idp.com/token";

        final PrivateKeyJwtClientAuthentication auth =
                new PrivateKeyJwtClientAuthentication(clientId, audience, ecJWK, JWSAlgorithm.ES256);

        final OAuthRequest request = new OAuthRequest(Verb.POST, audience);
        auth.addClientAuthentication(request);

        final String assertion = getParam(request, "client_assertion");
        final SignedJWT signedJWT = SignedJWT.parse(assertion);
        assertThat(signedJWT.getHeader().getAlgorithm()).isEqualTo(JWSAlgorithm.ES256);
    }

    private String getParam(final OAuthRequest request, final String name) {
        return request.getBodyParams().getParams().stream()
                .filter(p -> p.getKey().equals(name))
                .map(com.github.scribejava.core.model.Parameter::getValue)
                .findFirst()
                .orElse(null);
    }
}
