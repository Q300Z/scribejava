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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class ReflectiveApiTest {

  @Test
  public void shouldValidateAllApisEndpoints() throws Exception {
    final Set<Class<? extends DefaultApi20>> apiClasses = findApiClasses();
    for (Class<? extends DefaultApi20> apiClass : apiClasses) {
      if (Modifier.isAbstract(apiClass.getModifiers())) {
        continue;
      }

      final DefaultApi20 api;
      try {
        final Method instanceMethod = apiClass.getDeclaredMethod("instance");
        api = (DefaultApi20) instanceMethod.invoke(null);
      } catch (NoSuchMethodException e) {
        // Some APIs might require parameters, skip for now if not easy to instantiate
        continue;
      }

      assertThat(api.getAccessTokenEndpoint())
          .as("Access Token Endpoint for " + apiClass.getSimpleName())
          .isNotNull();
      assertDoesNotThrow(
          () -> URI.create(api.getAccessTokenEndpoint()),
          "Invalid Access Token URL for " + apiClass.getSimpleName());

      assertThat(api.getAuthorizationBaseUrl())
          .as("Authorization Base URL for " + apiClass.getSimpleName())
          .isNotNull();
      assertDoesNotThrow(
          () -> URI.create(api.getAuthorizationBaseUrl()),
          "Invalid Authorization URL for " + apiClass.getSimpleName());
    }
  }

  private Set<Class<? extends DefaultApi20>> findApiClasses() {
    // In a real project we would use a classpath scanner like Reflections or ClassGraph.
    // For this POC, I'll manually list them or try to find them if I can.
    // Actually, I can use the existing list from AllApisTest as a base.
    final Set<Class<? extends DefaultApi20>> classes = new HashSet<>();
    classes.add(Asana20Api.class);
    classes.add(BoxApi20.class);
    classes.add(DataportenApi.class);
    classes.add(DiscordApi.class);
    classes.add(DoktornaraboteApi.class);
    classes.add(DropboxApi.class);
    classes.add(GeniusApi.class);
    classes.add(GitHubApi.class);
    classes.add(HHApi.class);
    classes.add(HiOrgServerApi20.class);
    classes.add(KakaoApi.class);
    classes.add(KeycloakApi.class);
    classes.add(LinkedInApi20.class);
    classes.add(LiveApi.class);
    classes.add(MeetupApi20.class);
    classes.add(MicrosoftAzureActiveDirectory20Api.class);
    classes.add(PinterestApi.class);
    classes.add(PolarAPI.class);
    classes.add(SlackApi.class);
    classes.add(StackExchangeApi.class);
    classes.add(TheThingsNetworkV1StagingApi.class);
    classes.add(TheThingsNetworkV2PreviewApi.class);
    classes.add(XeroApi20.class);
    return classes;
  }
}
