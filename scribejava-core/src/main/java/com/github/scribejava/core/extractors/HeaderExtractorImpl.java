package com.github.scribejava.core.extractors;

import java.util.stream.Collectors; // ADDED
import com.github.scribejava.core.exceptions.OAuthParametersMissingException;
import com.github.scribejava.core.model.OAuthConstants;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.utils.OAuthEncoder;
import com.github.scribejava.core.utils.Preconditions;

/**
 * Default implementation of {@link HeaderExtractor}. Conforms to OAuth 1.0a
 */
public class HeaderExtractorImpl implements HeaderExtractor {

    private static final String PARAM_SEPARATOR = ", ";
    private static final String PREAMBLE = "OAuth ";

    /**
     * {@inheritDoc}
     */
    @Override
    public String extract(OAuthRequest request) {
        checkPreconditions(request);

        final String oauthParams = request.getOauthParameters().entrySet().stream()
                .map(entry -> entry.getKey() + "=\"" + OAuthEncoder.encode(entry.getValue()) + "\"")
                .collect(Collectors.joining(PARAM_SEPARATOR)); // USE Stream

        final StringBuilder header = new StringBuilder(PREAMBLE).append(oauthParams);

        if (request.getRealm() != null && !request.getRealm().isEmpty()) {
            header.append(PARAM_SEPARATOR)
                    .append(OAuthConstants.REALM)
                    .append("=\"")
                    .append(request.getRealm())
                    .append('"');
        }
        return header.toString();
    }

    private void checkPreconditions(OAuthRequest request) {
        Preconditions.checkNotNull(request, "Cannot extract a header from a null object");

        if (request.getOauthParameters() == null || request.getOauthParameters().isEmpty()) {
            throw new OAuthParametersMissingException(request);
        }
    }

}
