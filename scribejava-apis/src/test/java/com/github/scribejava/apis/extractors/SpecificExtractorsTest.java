package com.github.scribejava.apis.extractors;

import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.apis.GitHubApi;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class SpecificExtractorsTest {

    @Test
    public void shouldExtractGitHubAccessToken() throws IOException {
        // GitHub sends token in a format like access_token=...&scope=...&token_type=...
        final String body = "access_token=gh_token&scope=user&token_type=bearer";
        final Response response = new Response(200, "OK", Collections.emptyMap(), body);
        final OAuth2AccessToken token = GitHubApi.instance().getAccessTokenExtractor().extract(response);
        assertThat(token.getAccessToken()).isEqualTo("gh_token");
        assertThat(token.getScope()).isEqualTo("user");
    }
}
