package com.github.scribejava.core.oauth;

import com.github.scribejava.core.extractors.OAuth2AccessTokenJsonExtractor;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.revoke.TokenTypeHint;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Handles OAuth 2.0 token revocation.
 */
public class OAuth20RevocationHandler {

    private final OAuth20Service service;

    public OAuth20RevocationHandler(OAuth20Service service) {
        this.service = service;
    }

    public OAuthRequest createRevokeTokenRequest(String tokenToRevoke, TokenTypeHint tokenTypeHint) {
        final OAuthRequest request = new OAuthRequest(Verb.POST, service.getApi().getRevokeTokenEndpoint());

        service.getApi().getClientAuthentication().addClientAuthentication(request, service.getApiKey(),
                service.getApiSecret());

        request.addParameter("token", tokenToRevoke);
        if (tokenTypeHint != null) {
            request.addParameter("token_type_hint", tokenTypeHint.getValue());
        }

        return request;
    }

    public void revokeToken(String tokenToRevoke, TokenTypeHint tokenTypeHint)
            throws IOException, InterruptedException, ExecutionException {
        final OAuthRequest request = createRevokeTokenRequest(tokenToRevoke, tokenTypeHint);

        try (Response response = service.execute(request)) {
            checkForError(response);
        }
    }

    public void checkForError(Response response) throws IOException {
        if (response.getCode() != 200) {
            OAuth2AccessTokenJsonExtractor.instance().generateError(response);
        }
    }
}
