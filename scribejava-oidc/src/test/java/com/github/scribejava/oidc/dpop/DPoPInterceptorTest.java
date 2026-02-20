package com.github.scribejava.oidc.dpop;

import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DPoPInterceptorTest {

    @Test
    public void shouldCreateValidDPoPProof() throws Exception {
        final DefaultDPoPProofCreator proofCreator = new DefaultDPoPProofCreator();
        final com.github.scribejava.core.dpop.DPoPInterceptor interceptor =
                new com.github.scribejava.core.dpop.DPoPInterceptor(proofCreator);

        final OAuthRequest request = new OAuthRequest(Verb.POST, "https://resource.example.com/api/user");
        interceptor.intercept(request);

        final String dpopHeader = request.getHeaders().get("DPoP");
        assertThat(dpopHeader).isNotNull();

        final SignedJWT signedJWT = SignedJWT.parse(dpopHeader);
        assertThat(signedJWT.getJWTClaimsSet().getStringClaim("htm")).isEqualTo("POST");
        assertThat(signedJWT.getJWTClaimsSet().getStringClaim("htu"))
                .isEqualTo("https://resource.example.com/api/user");
        assertThat(signedJWT.getJWTClaimsSet().getJWTID()).isNotNull();
        assertThat(signedJWT.getHeader().getJWK()).isNotNull();
    }
}
