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
package com.github.scribejava.oidc;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.scribejava.core.model.JsonBuilder;
import com.github.scribejava.core.oauth2.clientauthentication.HttpBasicAuthenticationScheme;
import com.github.scribejava.core.oauth2.clientauthentication.RequestBodyAuthenticationScheme;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class OidcProvidersTest {

  @Test
  public void shouldReturnCorrectGoogleOidcConfig() {
    final OidcGoogleApi20 api = OidcGoogleApi20.instance();
    assertThat(api.getIssuer()).isEqualTo("https://accounts.google.com");
    assertThat(api.getAccessTokenEndpoint()).isEqualTo("https://oauth2.googleapis.com/token");
    assertThat(api.getAuthorizationBaseUrl())
        .isEqualTo("https://accounts.google.com/o/oauth2/v2/auth");
  }

  @Test
  public void shouldReturnCorrectGitHubOidcConfig() {
    final OidcGitHubApi20 api = OidcGitHubApi20.instance();
    assertThat(api.getIssuer()).isEqualTo("https://token.actions.githubusercontent.com");
    assertThat(api.getAccessTokenEndpoint())
        .isEqualTo("https://github.com/login/oauth/access_token");
  }

  @Test
  public void shouldReturnCorrectMicrosoftOidcConfig() {
    final OidcMicrosoftAzureActiveDirectory20Api api =
        OidcMicrosoftAzureActiveDirectory20Api.instance();
    assertThat(api.getIssuer()).isEqualTo("https://login.microsoftonline.com/common/v2.0");

    final OidcMicrosoftAzureActiveDirectory20Api customApi =
        OidcMicrosoftAzureActiveDirectory20Api.custom("my-tenant");
    assertThat(customApi.getIssuer()).isEqualTo("https://login.microsoftonline.com/my-tenant/v2.0");
    assertThat(customApi.getAccessTokenEndpoint()).contains("my-tenant");
  }

  @Test
  public void shouldReturnCorrectFranceConnectOidcConfig() {
    final OidcFranceConnectApi20 prod = OidcFranceConnectApi20.instance();
    assertThat(prod.getIssuer()).isEqualTo("https://app.franceconnect.gouv.fr/api/v2");
    assertThat(prod.getAuthorizationBaseUrl())
        .isEqualTo("https://app.franceconnect.gouv.fr/api/v2/authorize");
    assertThat(prod.getAccessTokenEndpoint())
        .isEqualTo("https://app.franceconnect.gouv.fr/api/v2/token");
    assertThat(prod.getUserinfoEndpoint())
        .isEqualTo("https://app.franceconnect.gouv.fr/api/v2/userinfo");
    assertThat(prod.getJwksUri()).isEqualTo("https://app.franceconnect.gouv.fr/api/v2/jwks");
    assertThat(prod.getEndSessionEndpoint())
        .isEqualTo("https://app.franceconnect.gouv.fr/api/v2/session/end");
    assertThat(prod.getRevokeTokenEndpoint()).isNull();
    assertThat(prod.getClientAuthentication()).isSameAs(RequestBodyAuthenticationScheme.instance());

    final OidcFranceConnectApi20 sandbox = OidcFranceConnectApi20.sandbox();
    assertThat(sandbox.getIssuer()).isEqualTo("https://fcp-low.sbx.dev-franceconnect.fr/api/v2");
    assertThat(sandbox.getAuthorizationBaseUrl())
        .isEqualTo("https://fcp-low.sbx.dev-franceconnect.fr/api/v2/authorize");
    assertThat(sandbox.getAccessTokenEndpoint())
        .isEqualTo("https://fcp-low.sbx.dev-franceconnect.fr/api/v2/token");

    final OidcFranceConnectApi20 oidcProd =
        OidcFranceConnectApi20.custom(OidcFranceConnectApi20.OIDC_PROD_BASE_URL);
    assertThat(oidcProd.getAuthorizationBaseUrl())
        .isEqualTo("https://oidc.franceconnect.gouv.fr/api/v2/authorize");
  }

  @Test
  public void shouldApplyDefaultAndOverridableAcrValuesForFranceConnect() {
    final OidcService service =
        new OidcServiceBuilder("fc-client-id")
            .apiSecret("fc-secret")
            .callback("https://app.example.com/callback")
            .defaultScope("openid identite_pivot")
            .build(OidcFranceConnectApi20.sandbox());

    final OidcSessionState session = service.initSessionState();
    final String defaultUrl = service.getAuthorizationUrl(session);
    assertThat(defaultUrl).contains("acr_values=eidas1");

    final Map<String, String> overrideParams = new HashMap<>();
    overrideParams.put("acr_values", "eidas2");
    overrideParams.put("prompt", "login");
    final String overriddenUrl = service.getAuthorizationUrl(session, overrideParams);
    assertThat(overriddenUrl).contains("acr_values=eidas2");
    assertThat(overriddenUrl).doesNotContain("acr_values=eidas1");
    assertThat(overriddenUrl).contains("prompt=login");

    final String builderOverriddenUrl =
        service.createAuthorizationUrlBuilder().acrValues("eidas3").build();
    assertThat(builderOverriddenUrl).contains("acr_values=eidas3");
    assertThat(builderOverriddenUrl).doesNotContain("acr_values=eidas1");

    final String emptyAcrUrl =
        service
            .createAuthorizationUrlBuilder()
            .additionalParams(Collections.singletonMap("acr_values", ""))
            .build();
    assertThat(emptyAcrUrl).contains("acr_values=eidas1");

    final String blankAcrUrl =
        service
            .createAuthorizationUrlBuilder()
            .additionalParams(Collections.singletonMap("acr_values", "   "))
            .build();
    assertThat(blankAcrUrl).contains("acr_values=eidas1");
  }

  @Test
  public void shouldReturnCorrectProConnectOidcConfigAndAuthSchemes() throws Exception {
    final OidcProConnectApi20 prod = OidcProConnectApi20.instance();
    assertThat(prod.getIssuer()).isEqualTo("https://partenaires.proconnect.gouv.fr/api/v2");
    assertThat(prod.getAuthorizationBaseUrl())
        .isEqualTo("https://partenaires.proconnect.gouv.fr/api/v2/authorize");
    assertThat(prod.getAccessTokenEndpoint())
        .isEqualTo("https://partenaires.proconnect.gouv.fr/api/v2/token");
    assertThat(prod.getUserinfoEndpoint())
        .isEqualTo("https://partenaires.proconnect.gouv.fr/api/v2/userinfo");
    assertThat(prod.getJwksUri()).isEqualTo("https://partenaires.proconnect.gouv.fr/api/v2/jwks");
    assertThat(prod.getClientAuthentication()).isSameAs(RequestBodyAuthenticationScheme.instance());

    final OidcProConnectApi20 sandbox = OidcProConnectApi20.sandbox();
    assertThat(sandbox.getIssuer()).isEqualTo("https://fca.integ01.dev-agentconnect.fr/api/v2");
    assertThat(sandbox.getAuthorizationBaseUrl())
        .isEqualTo("https://fca.integ01.dev-agentconnect.fr/api/v2/authorize");
    assertThat(sandbox.getAccessTokenEndpoint())
        .isEqualTo("https://fca.integ01.dev-agentconnect.fr/api/v2/token");

    final OidcProConnectApi20 agentConnect =
        OidcProConnectApi20.custom(OidcProConnectApi20.AGENTCONNECT_PROD_DOMAIN);
    assertThat(agentConnect.getIssuer()).isEqualTo("https://auth.agentconnect.gouv.fr/api/v2");
    assertThat(agentConnect.getUserinfoEndpoint())
        .isEqualTo("https://auth.agentconnect.gouv.fr/api/v2/userinfo");

    final OidcProConnectApi20 localCustom =
        OidcProConnectApi20.custom("http://localhost:8080/api/v2/");
    assertThat(localCustom.getIssuer()).isEqualTo("http://localhost:8080/api/v2");
    assertThat(localCustom.getAccessTokenEndpoint())
        .isEqualTo("http://localhost:8080/api/v2/token");

    final String metadataJson =
        new JsonBuilder()
            .add("issuer", "https://fca.integ01.dev-agentconnect.fr/api/v2")
            .add(
                "authorization_endpoint",
                "https://fca.integ01.dev-agentconnect.fr/api/v2/authorize")
            .add("token_endpoint", "https://fca.integ01.dev-agentconnect.fr/api/v2/token")
            .add("jwks_uri", "https://fca.integ01.dev-agentconnect.fr/api/v2/jwks")
            .add("userinfo_endpoint", "https://fca.integ01.dev-agentconnect.fr/api/v2/userinfo")
            .add("response_types_supported", Collections.singletonList("code"))
            .add("subject_types_supported", Collections.singletonList("public"))
            .add("id_token_signing_alg_values_supported", Collections.singletonList("RS256"))
            .add(
                "token_endpoint_auth_methods_supported",
                Collections.singletonList("client_secret_basic"))
            .build();
    final OidcProConnectApi20 metaApi =
        OidcProConnectApi20.custom(OidcProConnectApi20.SANDBOX_DOMAIN);
    metaApi.setMetadata(OidcProviderMetadata.parse(metadataJson));
    assertThat(metaApi.getClientAuthentication())
        .isSameAs(HttpBasicAuthenticationScheme.instance());
    assertThat(OidcProConnectApi20.sandbox().getMetadata()).isNull();
  }
}
