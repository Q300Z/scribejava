package com.github.scribejava.oidc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service for Dynamic Client Registration (RFC 7591 & OIDC Registration).
 */
public class OidcRegistrationService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final HttpClient httpClient;
    private final String userAgent;

    public OidcRegistrationService(final HttpClient httpClient, final String userAgent) {
        this.httpClient = httpClient;
        this.userAgent = userAgent;
    }

    public CompletableFuture<JsonNode> registerClientAsync(final String registrationEndpoint,
                                                           final List<String> redirectUris, final String clientName, final String tokenEndpointAuthMethod) {
        final ObjectNode registrationRequest = OBJECT_MAPPER.createObjectNode();
        final ArrayNode redirectUrisNode = registrationRequest.putArray("redirect_uris");
        redirectUris.forEach(redirectUrisNode::add);
        registrationRequest.put("client_name", clientName);
        if (tokenEndpointAuthMethod != null) {
            registrationRequest.put("token_endpoint_auth_method", tokenEndpointAuthMethod);
        }

        final OAuthRequest request = new OAuthRequest(Verb.POST, registrationEndpoint);
        request.setPayload(registrationRequest.toString());
        request.addHeader("Content-Type", "application/json");

        return httpClient.executeAsync(userAgent, request.getHeaders(), request.getVerb(), request.getCompleteUrl(),
                request.getStringPayload(), null, response -> {
                    try (Response resp = response) {
                        if (resp.getCode() != 201 && resp.getCode() != 200) {
                            throw new OAuthException("Client registration failed. Status: " + resp.getCode()
                                    + ", Body: " + resp.getBody());
                        }
                        return OBJECT_MAPPER.readTree(resp.getBody());
                    } catch (final IOException e) {
                        throw new OAuthException("Error parsing registration response", e);
                    }
                });
    }
}
