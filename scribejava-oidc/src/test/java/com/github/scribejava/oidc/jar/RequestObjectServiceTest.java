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

        final RequestObjectService service = new RequestObjectService(clientId, audience, rsaJWK, JWSAlgorithm.RS256);

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
