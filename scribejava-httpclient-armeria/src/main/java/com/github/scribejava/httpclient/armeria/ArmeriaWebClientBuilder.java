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
package com.github.scribejava.httpclient.armeria;

import com.linecorp.armeria.client.*;
import com.linecorp.armeria.client.logging.LoggingClient;
import com.linecorp.armeria.client.retry.RetryingClient;
import com.linecorp.armeria.common.SessionProtocol;

import java.util.function.Function;

/**
 * A builder of {@link WebClient} using supplied configuration parameters.
 */
public class ArmeriaWebClientBuilder {

    private final ClientFactory clientFactory;
    private final ClientOptions clientOptions;
    private final SessionProtocol protocolPreference;
    private final Function<? super HttpClient, RetryingClient> retry;
    private final Function<? super HttpClient, LoggingClient> logging;

    ArmeriaWebClientBuilder(
            ClientOptions clientOptions,
            ClientFactory clientFactory,
            SessionProtocol protocolPreference,
            Function<? super HttpClient, RetryingClient> retry,
            Function<? super HttpClient, LoggingClient> logging) {
        this.clientOptions = clientOptions;
        this.clientFactory = clientFactory;
        this.protocolPreference = protocolPreference;
        this.retry = retry;
        this.logging = logging;
    }

    WebClient newWebClient(String scheme, String authority) {
        final SessionProtocol protocol = protocol(scheme);
        final Endpoint endpoint = Endpoint.parse(authority);
        final WebClientBuilder clientBuilder = WebClient.builder(protocol, endpoint);
        if (clientOptions != null) {
            clientBuilder.options(clientOptions);
        }
        if (clientFactory != null) {
            clientBuilder.factory(clientFactory);
        }
        if (retry != null) {
            clientBuilder.decorator(retry);
        }
        if (logging != null) {
            clientBuilder.decorator(logging);
        }
        return clientBuilder.build();
    }

    private SessionProtocol protocol(String scheme) {
        final SessionProtocol protocol = SessionProtocol.of(scheme);
        switch (protocol) {
            case HTTP:
                if (protocolPreference == SessionProtocol.H1) {
                    // enforce HTTP/1 protocol
                    return SessionProtocol.H1C;
                }
                break;
            case HTTPS:
                if (protocolPreference == SessionProtocol.H1) {
                    // enforce HTTP/1 protocol
                    return SessionProtocol.H1;
                }
                break;
            default:
                break;
        }
        return protocol;
    }
}
