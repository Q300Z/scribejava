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
package com.github.scribejava.oidc.dpop;

import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests pour {@link DefaultDPoPProofCreator}.
 */
public class DefaultDPoPProofCreatorTest {

    /**
     * @throws Exception Exception
     */
    @Test
    public void shouldGenerateRSAProofWithAth() throws Exception {
        final DefaultDPoPProofCreator creator = new DefaultDPoPProofCreator();
        final OAuthRequest request = new OAuthRequest(Verb.POST, "https://example.com/api");
        final String accessToken = "my-access-token";

        final String proof = creator.createDPoPProof(request, accessToken);
        assertThat(proof).isNotNull();

        final SignedJWT jwt = SignedJWT.parse(proof);
        assertThat(jwt.getHeader().getAlgorithm()).isEqualTo(JWSAlgorithm.RS256);
        assertThat(jwt.getJWTClaimsSet().getClaim("htm")).isEqualTo("POST");
        assertThat(jwt.getJWTClaimsSet().getClaim("htu")).isEqualTo("https://example.com/api");
        assertThat(jwt.getJWTClaimsSet().getClaim("ath")).isNotNull();
    }

    /**
     * @throws Exception Exception
     */
    @Test
    public void shouldGenerateECProof() throws Exception {
        final ECKey ecJWK =
                new ECKeyGenerator(com.nimbusds.jose.jwk.Curve.P_256).keyUse(KeyUse.SIGNATURE).generate();
        final DefaultDPoPProofCreator creator = new DefaultDPoPProofCreator(ecJWK, JWSAlgorithm.ES256);
        final OAuthRequest request = new OAuthRequest(Verb.GET, "https://example.com/api");

        final String proof = creator.createDPoPProof(request, null);

        final SignedJWT jwt = SignedJWT.parse(proof);
        assertThat(jwt.getHeader().getAlgorithm()).isEqualTo(JWSAlgorithm.ES256);
        assertThat(jwt.getJWTClaimsSet().getClaim("htm")).isEqualTo("GET");
    }

    /**
     * @throws Exception Exception
     */
    @Test
    public void shouldThrowOnUnsupportedJWK() throws Exception {
        final JWK mockJWK = mock(JWK.class);
        when(mockJWK.isPrivate()).thenReturn(true);
        final DefaultDPoPProofCreator creator =
                new DefaultDPoPProofCreator(mockJWK, JWSAlgorithm.RS256);

        final OAuthRequest request = new OAuthRequest(Verb.GET, "https://idp.com");
        assertThatThrownBy(() -> creator.createDPoPProof(request, null))
                .isInstanceOf(com.github.scribejava.core.exceptions.OAuthException.class)
                .hasMessageContaining("Unsupported JWK type");
    }
}
