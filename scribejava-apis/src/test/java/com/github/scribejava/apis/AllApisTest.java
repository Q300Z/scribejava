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
package com.github.scribejava.apis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.github.scribejava.core.builder.api.DefaultApi20;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

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
      assertThat(api.getAccessTokenEndpoint())
          .as("Access Token Endpoint for " + api.getClass().getSimpleName())
          .isNotNull();
      assertDoesNotThrow(
          () -> URI.create(api.getAccessTokenEndpoint()),
          "Invalid Access Token URL for " + api.getClass().getSimpleName());

      assertThat(api.getAuthorizationBaseUrl())
          .as("Authorization Base URL for " + api.getClass().getSimpleName())
          .isNotNull();
      assertDoesNotThrow(
          () -> URI.create(api.getAuthorizationBaseUrl()),
          "Invalid Authorization URL for " + api.getClass().getSimpleName());
    }
  }
}
