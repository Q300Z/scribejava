package com.github.scribejava.core.oauth;

import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.model.OAuthAsyncRequestCallback;
import com.github.scribejava.core.model.OAuthConstants;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.PushedAuthorizationResponse;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Handles OAuth 2.0 Pushed Authorization Requests (PAR).
 */
public class OAuth20PushedAuthHandler {

    private final OAuth20Service service;

    public OAuth20PushedAuthHandler(OAuth20Service service) {
        this.service = service;
    }

    public OAuthRequest createPushedAuthorizationRequest(String responseType, String apiKey, String callback,
                                                         String scope, String state, Map<String, String> additionalParams) {
        final OAuthRequest request = new OAuthRequest(Verb.POST, service.getApi().getPushedAuthorizationRequestEndpoint());
        request.addParameter(OAuthConstants.RESPONSE_TYPE, responseType);
        request.addParameter(OAuthConstants.CLIENT_ID, apiKey);
        if (callback != null) {
            request.addParameter(OAuthConstants.REDIRECT_URI, callback);
        }
        if (scope != null) {
            request.addParameter(OAuthConstants.SCOPE, scope);
        }
        if (state != null) {
            request.addParameter(OAuthConstants.STATE, state);
        }
        if (additionalParams != null) {
            additionalParams.forEach(request::addParameter);
        }
        service.getApi().getClientAuthentication().addClientAuthentication(request, service.getApiKey(),
                service.getApiSecret());
        return request;
    }

    public CompletableFuture<PushedAuthorizationResponse> pushAuthorizationRequestAsync(String responseType,
                                                                                        String apiKey, String callback, String scope, String state, Map<String, String> additionalParams,
                                                                                        OAuthAsyncRequestCallback<PushedAuthorizationResponse> callbackConsumer) {
        final String parEndpoint = service.getApi().getPushedAuthorizationRequestEndpoint();
        if (parEndpoint == null) {
            final CompletableFuture<PushedAuthorizationResponse> future = new CompletableFuture<>();
            future.completeExceptionally(new UnsupportedOperationException(
                    "This API doesn't support Pushed Authorization Requests"));
            return future;
        }

        final OAuthRequest request = createPushedAuthorizationRequest(responseType, apiKey, callback, scope, state, additionalParams);

        return service.execute(request, callbackConsumer, response -> {
            try (Response resp = response) {
                if (resp.getCode() != 201 && resp.getCode() != 200) {
                    throw new OAuthException("Failed to push authorization request. Status: " + resp.getCode()
                            + ", Body: " + resp.getBody());
                }
                return PushedAuthorizationResponse.parse(resp.getBody());
            }
        });
    }
}
