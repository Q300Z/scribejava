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

        final PrivateKeyJwtClientAuthentication auth = new PrivateKeyJwtClientAuthentication(
                clientId, audience, rsaJWK, JWSAlgorithm.RS256);

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

    private String getParam(final OAuthRequest request, final String name) {
        return request.getBodyParams().getParams().stream()
                .filter(p -> p.getKey().equals(name))
                .map(com.github.scribejava.core.model.Parameter::getValue)
                .findFirst()
                .orElse(null);
    }
}
