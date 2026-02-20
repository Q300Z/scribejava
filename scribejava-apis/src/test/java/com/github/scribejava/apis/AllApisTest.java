package com.github.scribejava.apis;

import com.github.scribejava.core.builder.api.DefaultApi20;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class AllApisTest {

    @Test
    public void shouldBeAbleToInstantiateAllApis() {
        final List<DefaultApi20> apis = new ArrayList<>();
        apis.add(Asana20Api.instance());
        apis.add(BoxApi20.instance());
        apis.add(DataportenApi.instance());
        apis.add(DiscordApi.instance());
        apis.add(DoktornaraboteApi.instance());
        apis.add(DropboxApi.instance());
        apis.add(FrappeApi.instance("http://frappe.example.com"));
        apis.add(GeniusApi.instance());
        apis.add(GitHubApi.instance());
        apis.add(HHApi.instance());
        apis.add(HiOrgServerApi20.instance());
        apis.add(KakaoApi.instance());
        apis.add(KeycloakApi.instance());
        apis.add(LinkedInApi20.instance());
        apis.add(LiveApi.instance());
        apis.add(MeetupApi20.instance());
        apis.add(MicrosoftAzureActiveDirectory20Api.instance());
        apis.add(PinterestApi.instance());
        apis.add(PolarAPI.instance());
        apis.add(SlackApi.instance());
        apis.add(StackExchangeApi.instance());
        apis.add(TheThingsNetworkV1StagingApi.instance());
        apis.add(TheThingsNetworkV2PreviewApi.instance());
        apis.add(XeroApi20.instance());

        for (final DefaultApi20 api : apis) {
            assertThat(api.getAccessTokenEndpoint()).as("Access Token Endpoint for " + api.getClass().getSimpleName())
                    .isNotNull();
            assertDoesNotThrow(() -> URI.create(api.getAccessTokenEndpoint()),
                    "Invalid Access Token URL for " + api.getClass().getSimpleName());

            assertThat(api.getAuthorizationBaseUrl()).as("Authorization Base URL for " + api.getClass().getSimpleName())
                    .isNotNull();
            assertDoesNotThrow(() -> URI.create(api.getAuthorizationBaseUrl()),
                    "Invalid Authorization URL for " + api.getClass().getSimpleName());
        }
    }
}
