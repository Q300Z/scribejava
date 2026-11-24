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

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.httpclient.HttpClientConfig;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthAsyncRequestCallback;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.oauth2.grant.OAuth20Grant;
import com.github.scribejava.core.utils.JsonUtils;
import com.github.scribejava.oidc.model.JweDecryptor;
import com.github.scribejava.oidc.model.JwksParser;
import com.github.scribejava.oidc.model.Jwt;
import com.github.scribejava.oidc.model.OidcKey;
import com.github.scribejava.oidc.model.OidcNonce;
import java.io.IOException;
import java.io.OutputStream;
import java.security.PrivateKey;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

/** Service OIDC natif. */
public class OidcService extends OAuth20Service {

  private IdTokenValidator idTokenValidator;
  private OidcSessionStateStore sessionStateStore = new DefaultOidcSessionStateStore();
  private PrivateKey decryptionKey;
  private Map<String, PrivateKey> decryptionKeys;
  private final JweDecryptor jweDecryptor = new JweDecryptor();

  public OidcService(
      DefaultApi20 api,
      String apiKey,
      String apiSecret,
      String callback,
      String scope,
      String responseType,
      OutputStream debugStream,
      String userAgent,
      HttpClientConfig httpClientConfig,
      HttpClient httpClient) {
    super(
        api,
        apiKey,
        apiSecret,
        callback,
        scope,
        responseType,
        debugStream,
        userAgent,
        httpClientConfig,
        httpClient);
  }

  @Override
  protected CompletableFuture<OAuth2AccessToken> sendAccessTokenRequestAsync(
      OAuthRequest request, OAuthAsyncRequestCallback<OAuth2AccessToken> callback) {
    if ((getApiSecret() == null || getApiSecret().isEmpty())
        && !request
            .getHeaders()
            .containsKey(com.github.scribejava.core.model.OAuthConstants.HEADER)) {
      final boolean hasClientId =
          request.getBodyParams().getParams().stream().anyMatch(p -> "client_id".equals(p.getKey()))
              || request.getQueryStringParams().getParams().stream()
                  .anyMatch(p -> "client_id".equals(p.getKey()));
      if (!hasClientId) {
        request.addParameter(
            com.github.scribejava.core.model.OAuthConstants.CLIENT_ID, getApiKey());
      }
    }
    return super.sendAccessTokenRequestAsync(request, callback)
        .thenApply(
            token -> {
              validateIdToken(token);
              return token;
            });
  }

  private void validateIdToken(OAuth2AccessToken token) {
    String idTokenRaw = null;
    boolean isJson = false;
    try {
      final Map<String, Object> response = JsonUtils.parse(token.getRawResponse());
      isJson = true;
      final Object idTokenObj = response.get("id_token");
      idTokenRaw = idTokenObj instanceof String ? (String) idTokenObj : null;
    } catch (Exception e) {
      // ignore, not a JSON response
    }
    if (isJson && idTokenRaw == null) {
      return;
    }
    if (idTokenRaw == null) {
      idTokenRaw = token.getRawResponse();
    }

    idTokenRaw = decryptIfJwe(idTokenRaw);

    if (idTokenValidator != null) {
      idTokenValidator.validate(idTokenRaw, null, 0);
    } else {
      if (idTokenRaw != null) {
        new IdToken(idTokenRaw);
      }
    }
  }

  /**
   * @param token token
   * @param nonce nonce (optionnel)
   * @return IdToken validé
   */
  public IdToken validateIdToken(OAuth2AccessToken token, OidcNonce nonce) {
    String idTokenRaw = null;
    try {
      final Map<String, Object> response = JsonUtils.parse(token.getRawResponse());
      final Object idTokenObj = response.get("id_token");
      idTokenRaw = idTokenObj instanceof String ? (String) idTokenObj : null;
    } catch (Exception e) {
      // ignore, not a JSON response
    }
    if (idTokenRaw == null) {
      idTokenRaw = token.getRawResponse();
    }

    idTokenRaw = decryptIfJwe(idTokenRaw);

    if (idTokenValidator != null) {
      return idTokenValidator.validate(idTokenRaw, nonce, 0);
    }
    return idTokenRaw != null ? new IdToken(idTokenRaw) : null;
  }

  /**
   * Retourne la clé privée par défaut utilisée pour déchiffrer les jetons JWE.
   *
   * @return La clé privée de déchiffrement, ou {@code null}.
   */
  public PrivateKey getDecryptionKey() {
    return decryptionKey;
  }

  /**
   * Définit la clé privée par défaut utilisée pour déchiffrer les jetons JWE (ex. FranceConnect+).
   *
   * @param decryptionKey La clé privée RSA de déchiffrement.
   */
  public void setDecryptionKey(PrivateKey decryptionKey) {
    this.decryptionKey = decryptionKey;
  }

  /**
   * Retourne les clés privées indexées par {@code kid} pour déchiffrer les jetons JWE.
   *
   * @return Le dictionnaire {@code kid -> PrivateKey}, ou {@code null}.
   */
  public Map<String, PrivateKey> getDecryptionKeys() {
    return decryptionKeys;
  }

  /**
   * Définit les clés privées indexées par {@code kid} pour déchiffrer les jetons JWE.
   *
   * @param decryptionKeys Le dictionnaire {@code kid -> PrivateKey}.
   */
  public void setDecryptionKeys(Map<String, PrivateKey> decryptionKeys) {
    this.decryptionKeys = decryptionKeys;
  }

  private String decryptIfJwe(String rawToken) {
    if (rawToken != null && rawToken.split("\\.", -1).length == 5) {
      if (decryptionKey == null && (decryptionKeys == null || decryptionKeys.isEmpty())) {
        throw new OAuthException(
            "Encrypted JWT (JWE) received but no decryption key is configured.");
      }
      return jweDecryptor.decrypt(rawToken, decryptionKeys, decryptionKey);
    }
    return rawToken;
  }

  /**
   * @return idTokenValidator
   */
  public IdTokenValidator getIdTokenValidator() {
    return idTokenValidator;
  }

  /**
   * @param idTokenValidator idTokenValidator
   */
  public void setIdTokenValidator(IdTokenValidator idTokenValidator) {
    this.idTokenValidator = idTokenValidator;
  }

  /**
   * Récupère de manière synchrone les revendications UserInfo depuis le point de terminaison {@code
   * userinfo_endpoint}.
   *
   * @param token Le jeton d'accès OAuth 2.0 / OIDC.
   * @return Les revendications standards {@link StandardClaims}.
   * @throws IOException en cas d'erreur réseau ou de lecture de la réponse.
   * @throws InterruptedException si le thread courant est interrompu.
   * @throws ExecutionException si une erreur survient durant l'exécution.
   */
  public StandardClaims getUserInfo(OAuth2AccessToken token)
      throws IOException, InterruptedException, ExecutionException {
    try {
      return getUserInfoAsync(token).get();
    } catch (ExecutionException e) {
      Throwable cause = e.getCause();
      if (cause instanceof CompletionException) {
        cause = cause.getCause();
      }
      if (cause instanceof IOException) {
        throw (IOException) cause;
      }
      if (cause instanceof RuntimeException) {
        throw (RuntimeException) cause;
      }
      throw e;
    }
  }

  /**
   * Récupère de manière asynchrone les revendications UserInfo depuis le point de terminaison
   * {@code userinfo_endpoint} (OIDC Core §5.3).
   *
   * <p>Supporte les réponses JSON ({@code application/json} ou objet JSON débutant par une accolade
   * ouvrante) ainsi que les réponses JWT signées ({@code application/jwt} ou JWS à 3 segments) avec
   * vérification de signature ({@code RS256} / {@code ES256}) et contrôle des revendications
   * ({@code iss}, {@code aud}, {@code sub}, et {@code exp} s'il est présent).
   *
   * @param token Le jeton d'accès OAuth 2.0 / OIDC.
   * @return Un {@link CompletableFuture} fournissant les {@link StandardClaims}.
   */
  public CompletableFuture<StandardClaims> getUserInfoAsync(OAuth2AccessToken token) {
    if (token == null || token.getAccessToken() == null || token.getAccessToken().isEmpty()) {
      final CompletableFuture<StandardClaims> failed = new CompletableFuture<>();
      failed.completeExceptionally(new OAuthException("Access token must not be null or empty."));
      return failed;
    }

    final DefaultApi20 api = getApi();
    final String userinfoEndpoint =
        api instanceof DefaultOidcApi20 ? ((DefaultOidcApi20) api).getUserinfoEndpoint() : null;
    if (userinfoEndpoint == null || userinfoEndpoint.isEmpty()) {
      final CompletableFuture<StandardClaims> failed = new CompletableFuture<>();
      failed.completeExceptionally(new OAuthException("UserInfo endpoint is not configured."));
      return failed;
    }

    final OAuthRequest request = new OAuthRequest(Verb.GET, userinfoEndpoint);
    signRequest(token, request);

    return execute(
            request,
            null,
            response -> {
              try (Response resp = response) {
                final int code = resp.getCode();
                if (code < 200 || code >= 300) {
                  throw new OAuthException("UserInfo request failed with HTTP status " + code);
                }
                final String rawBody = resp.getBody();
                final String trimmed = rawBody != null ? rawBody.trim() : "";
                if (trimmed.isEmpty()) {
                  throw new OAuthException("UserInfo response body is empty.");
                }
                final String contentType = getContentTypeLowerCase(resp);
                return new RawUserInfoPayload(contentType, trimmed);
              } catch (IOException e) {
                throw new OAuthException("Failed to read UserInfo response", e);
              }
            })
        .thenCompose(payload -> parseAndVerifyUserInfo(payload, token));
  }

  private CompletableFuture<StandardClaims> parseAndVerifyUserInfo(
      RawUserInfoPayload payload, OAuth2AccessToken token) {
    final String contentType = payload.contentType;
    final String body = payload.body;
    final boolean hasJwtContentType =
        contentType != null && contentType.contains("application/jwt");
    final boolean hasJsonContentType =
        contentType != null && contentType.contains("application/json");
    final int dotSegments = body.split("\\.", -1).length;

    if (dotSegments == 5 && (hasJwtContentType || contentType == null || !hasJsonContentType)) {
      final String decrypted = decryptIfJwe(body);
      if (decrypted.trim().startsWith("{")) {
        return parseAndVerifyUserInfoJson(decrypted.trim(), token);
      }
      return processUserInfoJwt(decrypted.trim(), token);
    }
    if (hasJwtContentType) {
      return processUserInfoJwt(body, token);
    }
    if (hasJsonContentType) {
      if (!body.startsWith("{")) {
        final CompletableFuture<StandardClaims> failed = new CompletableFuture<>();
        failed.completeExceptionally(
            new OAuthException("Invalid JSON UserInfo response: expected '{'"));
        return failed;
      }
      return parseAndVerifyUserInfoJson(body, token);
    }

    if (body.startsWith("{")) {
      return parseAndVerifyUserInfoJson(body, token);
    }
    if (dotSegments == 3) {
      return processUserInfoJwt(body, token);
    }

    final CompletableFuture<StandardClaims> failed = new CompletableFuture<>();
    failed.completeExceptionally(
        new OAuthException("Unsupported UserInfo response format: " + contentType));
    return failed;
  }

  private CompletableFuture<StandardClaims> parseAndVerifyUserInfoJson(
      String body, OAuth2AccessToken token) {
    new StrictJsonValidator(body).validateRootObject();
    final Map<String, Object> claimsMap = JsonUtils.parse(body);
    if (claimsMap.isEmpty()) {
      throw new OAuthException("Invalid JSON UserInfo response");
    }
    final Object subObj = claimsMap.get("sub");
    if (!(subObj instanceof String) || ((String) subObj).trim().isEmpty()) {
      throw new OAuthException("UserInfo response missing or empty 'sub' claim.");
    }
    final StandardClaims claims = new StandardClaims(claimsMap);
    verifySubConsistency(claims, token);
    return CompletableFuture.completedFuture(claims);
  }

  private CompletableFuture<StandardClaims> processUserInfoJwt(
      String rawJwt, OAuth2AccessToken token) {
    final Jwt jwt = Jwt.parse(rawJwt);
    if (idTokenValidator != null) {
      final StandardClaims claims = idTokenValidator.validateUserInfoJwt(jwt);
      verifySubConsistency(claims, token);
      return CompletableFuture.completedFuture(claims);
    }

    final DefaultApi20 api = getApi();
    final DefaultOidcApi20 oidcApi =
        api instanceof DefaultOidcApi20 ? (DefaultOidcApi20) api : null;
    final String jwksUri = oidcApi != null ? oidcApi.getJwksUri() : null;
    if (jwksUri == null || jwksUri.isEmpty()) {
      final CompletableFuture<StandardClaims> failed = new CompletableFuture<>();
      failed.completeExceptionally(
          new OAuthException(
              "IdTokenValidator or JWKS URI is required to verify signed UserInfo JWT."));
      return failed;
    }

    final OAuthRequest jwksRequest = new OAuthRequest(Verb.GET, jwksUri);
    return execute(
            jwksRequest,
            null,
            response -> {
              try (Response resp = response) {
                final int code = resp.getCode();
                if (code < 200 || code >= 300) {
                  throw new OAuthException(
                      "Failed to fetch JWKS for UserInfo verification. Status: " + code);
                }
                return new JwksParser().parse(resp.getBody());
              } catch (IOException e) {
                throw new OAuthException("Failed to parse JWKS for UserInfo verification", e);
              }
            })
        .thenApply(
            keys -> {
              final StandardClaims claims = validateUserInfoJwtWithKeys(jwt, keys, oidcApi);
              verifySubConsistency(claims, token);
              return claims;
            });
  }

  private StandardClaims validateUserInfoJwtWithKeys(
      Jwt jwt, Map<String, OidcKey> keys, DefaultOidcApi20 oidcApi) {
    final String expectedIssuer = resolveExpectedIssuer(oidcApi);
    if (expectedIssuer == null || expectedIssuer.trim().isEmpty()) {
      throw new OAuthException("Expected issuer is required to verify signed UserInfo JWT.");
    }
    final Object algObj = jwt.getHeader().get("alg");
    if (!(algObj instanceof String) || !"RS256".equals(algObj) && !"ES256".equals(algObj)) {
      throw new OAuthException("Unsupported UserInfo JWT algorithm: " + algObj);
    }
    if (getApiKey() == null || getApiKey().trim().isEmpty()) {
      throw new OAuthException("Client ID is required to verify signed UserInfo JWT.");
    }
    final IdTokenValidator fallbackValidator =
        new IdTokenValidator(expectedIssuer, getApiKey(), (String) algObj, keys);
    return fallbackValidator.validateUserInfoJwt(jwt);
  }

  private String resolveExpectedIssuer(DefaultOidcApi20 oidcApi) {
    if (oidcApi == null) {
      return null;
    }
    if (oidcApi.getMetadata() != null && oidcApi.getMetadata().getIssuer() != null) {
      return oidcApi.getMetadata().getIssuer();
    }
    if (oidcApi instanceof OidcFranceConnectApi20) {
      return ((OidcFranceConnectApi20) oidcApi).getIssuer();
    }
    if (oidcApi instanceof OidcProConnectApi20) {
      return ((OidcProConnectApi20) oidcApi).getIssuer();
    }
    if (oidcApi instanceof OidcGoogleApi20) {
      return ((OidcGoogleApi20) oidcApi).getIssuer();
    }
    if (oidcApi instanceof OidcGitHubApi20) {
      return ((OidcGitHubApi20) oidcApi).getIssuer();
    }
    if (oidcApi instanceof OidcMicrosoftAzureActiveDirectory20Api) {
      return ((OidcMicrosoftAzureActiveDirectory20Api) oidcApi).getIssuer();
    }
    return null;
  }

  private void verifySubConsistency(StandardClaims userInfoClaims, OAuth2AccessToken token) {
    final String rawResponse;
    try {
      rawResponse = token.getRawResponse();
    } catch (IllegalStateException e) {
      // Jeton construit manuellement sans réponse brute associée : aucun id_token à comparer
      return;
    }
    if (rawResponse == null || !rawResponse.trim().startsWith("{")) {
      return;
    }
    final Map<String, Object> tokenMap = JsonUtils.parse(rawResponse);
    final Object idTokenObj = tokenMap.get("id_token");
    if (!(idTokenObj instanceof String)) {
      return;
    }
    String idTokenRaw = (String) idTokenObj;
    if (idTokenRaw.split("\\.", -1).length == 5) {
      if (decryptionKey == null && (decryptionKeys == null || decryptionKeys.isEmpty())) {
        return;
      }
      idTokenRaw = decryptIfJwe(idTokenRaw);
    }
    if (idTokenRaw.split("\\.", -1).length != 3) {
      return;
    }
    final Jwt idJwt = Jwt.parse(idTokenRaw);
    final Object idSubObj = idJwt.getPayload().get("sub");
    if (!(idSubObj instanceof String) || ((String) idSubObj).trim().isEmpty()) {
      return;
    }
    final Object userInfoSubObj = userInfoClaims.getAllClaims().get("sub");
    if (!(userInfoSubObj instanceof String) || ((String) userInfoSubObj).trim().isEmpty()) {
      throw new OAuthException("UserInfo response missing or empty 'sub' claim.");
    }
    if (!idSubObj.equals(userInfoSubObj)) {
      throw new OAuthException("UserInfo 'sub' claim does not match ID Token 'sub'.");
    }
  }

  private static String getContentTypeLowerCase(Response response) {
    String header = response.getHeader("Content-Type");
    if (header == null) {
      header = response.getHeader("content-type");
    }
    if (header == null) {
      header = response.getHeader("CONTENT-TYPE");
    }
    if (header == null) {
      header = response.getHeader("Content-type");
    }
    return header != null ? header.toLowerCase(Locale.ROOT) : null;
  }

  private static final class RawUserInfoPayload {
    private final String contentType;
    private final String body;

    private RawUserInfoPayload(String contentType, String body) {
      this.contentType = contentType;
      this.body = body;
    }
  }

  /** Validateur syntaxique JSON strict pour les réponses UserInfo. */
  private static final class StrictJsonValidator {
    private static final int MAX_DEPTH = 32;
    private final String src;
    private int cursor;

    private StrictJsonValidator(String src) {
      this.src = src;
    }

    private void validateRootObject() {
      parseObject(0);
      skipWhitespace();
      if (cursor != src.length()) {
        throw new OAuthException("Invalid JSON UserInfo response");
      }
    }

    private void skipWhitespace() {
      while (cursor < src.length() && Character.isWhitespace(src.charAt(cursor))) {
        cursor++;
      }
    }

    private void parseObject(int depth) {
      if (depth > MAX_DEPTH) {
        throw new OAuthException("Invalid JSON UserInfo response: nesting limit exceeded");
      }
      skipWhitespace();
      if (cursor >= src.length() || src.charAt(cursor) != '{') {
        throw new OAuthException("Invalid JSON UserInfo response");
      }
      cursor++;
      skipWhitespace();
      if (cursor < src.length() && src.charAt(cursor) == '}') {
        cursor++;
        return;
      }
      while (true) {
        parseString();
        skipWhitespace();
        if (cursor >= src.length() || src.charAt(cursor) != ':') {
          throw new OAuthException("Invalid JSON UserInfo response");
        }
        cursor++;
        parseValue(depth + 1);
        skipWhitespace();
        if (cursor < src.length() && src.charAt(cursor) == ',') {
          cursor++;
        } else if (cursor < src.length() && src.charAt(cursor) == '}') {
          cursor++;
          return;
        } else {
          throw new OAuthException("Invalid JSON UserInfo response");
        }
      }
    }

    private void parseArray(int depth) {
      if (depth > MAX_DEPTH) {
        throw new OAuthException("Invalid JSON UserInfo response: nesting limit exceeded");
      }
      cursor++;
      skipWhitespace();
      if (cursor < src.length() && src.charAt(cursor) == ']') {
        cursor++;
        return;
      }
      while (true) {
        parseValue(depth + 1);
        skipWhitespace();
        if (cursor < src.length() && src.charAt(cursor) == ',') {
          cursor++;
        } else if (cursor < src.length() && src.charAt(cursor) == ']') {
          cursor++;
          return;
        } else {
          throw new OAuthException("Invalid JSON UserInfo response");
        }
      }
    }

    private void parseValue(int depth) {
      skipWhitespace();
      if (cursor >= src.length()) {
        throw new OAuthException("Invalid JSON UserInfo response");
      }
      final char c = src.charAt(cursor);
      if (c == '{') {
        parseObject(depth);
      } else if (c == '[') {
        parseArray(depth);
      } else if (c == '"') {
        parseString();
      } else if (c == 't' || c == 'f' || c == 'n') {
        parseLiteral();
      } else if (c == '-' || Character.isDigit(c)) {
        parseNumber();
      } else {
        throw new OAuthException("Invalid JSON UserInfo response");
      }
    }

    private void parseString() {
      skipWhitespace();
      if (cursor >= src.length() || src.charAt(cursor) != '"') {
        throw new OAuthException("Invalid JSON UserInfo response");
      }
      cursor++;
      while (cursor < src.length()) {
        final char c = src.charAt(cursor++);
        if (c == '"') {
          return;
        }
        if (c == '\\') {
          if (cursor >= src.length()) {
            throw new OAuthException("Invalid JSON UserInfo response");
          }
          final char esc = src.charAt(cursor++);
          if (esc == 'u') {
            for (int i = 0; i < 4; i++) {
              if (cursor >= src.length() || Character.digit(src.charAt(cursor++), 16) < 0) {
                throw new OAuthException("Invalid JSON UserInfo response");
              }
            }
          } else if (esc != '"'
              && esc != '\\'
              && esc != '/'
              && esc != 'b'
              && esc != 'f'
              && esc != 'n'
              && esc != 'r'
              && esc != 't') {
            throw new OAuthException("Invalid JSON UserInfo response");
          }
        } else if (c < 0x20) {
          throw new OAuthException("Invalid JSON UserInfo response");
        }
      }
      throw new OAuthException("Invalid JSON UserInfo response");
    }

    private void parseLiteral() {
      if (src.startsWith("true", cursor) || src.startsWith("null", cursor)) {
        cursor += 4;
      } else if (src.startsWith("false", cursor)) {
        cursor += 5;
      } else {
        throw new OAuthException("Invalid JSON UserInfo response");
      }
    }

    private void parseNumber() {
      if (src.charAt(cursor) == '-') {
        cursor++;
      }
      if (cursor >= src.length() || !Character.isDigit(src.charAt(cursor))) {
        throw new OAuthException("Invalid JSON UserInfo response");
      }
      if (src.charAt(cursor) == '0') {
        cursor++;
      } else {
        while (cursor < src.length() && Character.isDigit(src.charAt(cursor))) {
          cursor++;
        }
      }
      if (cursor < src.length() && src.charAt(cursor) == '.') {
        cursor++;
        if (cursor >= src.length() || !Character.isDigit(src.charAt(cursor))) {
          throw new OAuthException("Invalid JSON UserInfo response");
        }
        while (cursor < src.length() && Character.isDigit(src.charAt(cursor))) {
          cursor++;
        }
      }
      if (cursor < src.length() && (src.charAt(cursor) == 'e' || src.charAt(cursor) == 'E')) {
        cursor++;
        if (cursor < src.length() && (src.charAt(cursor) == '+' || src.charAt(cursor) == '-')) {
          cursor++;
        }
        if (cursor >= src.length() || !Character.isDigit(src.charAt(cursor))) {
          throw new OAuthException("Invalid JSON UserInfo response");
        }
        while (cursor < src.length() && Character.isDigit(src.charAt(cursor))) {
          cursor++;
        }
      }
    }
  }

  /**
   * Generates the RP-Initiated Logout URL for this service.
   *
   * @param idTokenHint the ID token hint (ID Token issued during authentication)
   * @param postLogoutRedirectUri the URI to redirect the user to after logout
   * @param state the optional state value to include in the redirect URI
   * @param clientId the client identifier
   * @return the logout URL, or {@code null} if the API or metadata is not configured/available
   */
  public String getLogoutUrl(
      String idTokenHint, String postLogoutRedirectUri, String state, String clientId) {
    final DefaultApi20 api = getApi();
    if (api instanceof DefaultOidcApi20) {
      final OidcProviderMetadata metadata = ((DefaultOidcApi20) api).getMetadata();
      if (metadata != null) {
        return OidcSessionHelper.getLogoutUrl(
            metadata.getEndSessionEndpoint(), idTokenHint, postLogoutRedirectUri, state, clientId);
      }
    }
    return null;
  }

  // Helper methods to automatically initialize state, generate auth URLs and request tokens

  /**
   * Initializes and saves a new OIDC session state (including state, nonce, and PKCE code
   * verifier).
   *
   * @return the newly created {@link OidcSessionState}
   */
  public OidcSessionState initSessionState() {
    final String state = java.util.UUID.randomUUID().toString();
    final OidcNonce nonce = OidcNonce.generate();
    final String codeVerifier =
        com.github.scribejava.core.pkce.PKCEService.defaultInstance()
            .generatePKCE()
            .getCodeVerifier();
    final OidcSessionState sessionState = new OidcSessionState(state, nonce, codeVerifier);
    if (sessionStateStore != null) {
      sessionStateStore.save(sessionState);
    }
    return sessionState;
  }

  /**
   * Génère l'URL d'autorisation à partir de l'état de session OIDC fourni.
   *
   * @param sessionState L'état de session OIDC utilisé pour générer l'URL d'autorisation.
   * @return L'URL d'autorisation complète.
   */
  public String getAuthorizationUrl(OidcSessionState sessionState) {
    return getAuthorizationUrl(sessionState, null);
  }

  /**
   * Génère l'URL d'autorisation à partir de l'état de session OIDC et de paramètres supplémentaires
   * (tels que {@code acr_values}, {@code login_hint}, {@code prompt}, {@code max_age}).
   *
   * <p>Les entrées dont la clé ou la valeur est {@code null}, vide ou uniquement composée d'espaces
   * sont automatiquement ignorées.
   *
   * @param sessionState L'état de session OIDC utilisé pour générer l'URL d'autorisation.
   * @param additionalParams Paramètres OIDC supplémentaires à inclure dans la requête, ou {@code
   *     null}.
   * @return L'URL d'autorisation complète.
   */
  public String getAuthorizationUrl(
      OidcSessionState sessionState, Map<String, String> additionalParams) {
    final com.github.scribejava.core.pkce.PKCE pkce = new com.github.scribejava.core.pkce.PKCE();
    pkce.setCodeVerifier(sessionState.getCodeVerifier());
    try {
      pkce.setCodeChallenge(
          pkce.getCodeChallengeMethod().transform2CodeChallenge(sessionState.getCodeVerifier()));
    } catch (Exception e) {
      pkce.setCodeChallengeMethod(com.github.scribejava.core.pkce.PKCECodeChallengeMethod.PLAIN);
      pkce.setCodeChallenge(sessionState.getCodeVerifier());
    }
    final Map<String, String> filteredParams = new java.util.HashMap<>();
    if (additionalParams != null) {
      for (final Map.Entry<String, String> entry : additionalParams.entrySet()) {
        final String key = entry.getKey();
        final String value = entry.getValue();
        if (key != null && !key.trim().isEmpty() && value != null && !value.trim().isEmpty()) {
          filteredParams.put(key, value);
        }
      }
    }
    final com.github.scribejava.core.oauth.AuthorizationUrlBuilder builder =
        createAuthorizationUrlBuilder()
            .additionalParams(filteredParams)
            .state(sessionState.getState());
    if (sessionState.getNonce() != null) {
      builder.nonce(sessionState.getNonce().getValue());
    }
    return builder.pkce(pkce).build();
  }

  /**
   * {@inheritDoc}
   *
   * @param grant the OAuth 2.0 grant
   * @return the retrieved {@link OAuth2AccessToken}
   * @throws IOException if network or serialization error occurs
   * @throws InterruptedException if current thread is interrupted
   * @throws java.util.concurrent.ExecutionException if execution exception occurs
   */
  @Override
  public OAuth2AccessToken getAccessToken(OAuth20Grant grant)
      throws IOException, InterruptedException, ExecutionException {
    return super.getAccessToken(grant);
  }

  /**
   * Retrieves the access token using authorization code and loads session state for verification.
   *
   * @param code the authorization code
   * @param stateVal the state parameter value
   * @return the retrieved {@link OAuth2AccessToken}
   * @throws IOException if network or serialization error occurs
   * @throws InterruptedException if current thread is interrupted
   * @throws ExecutionException if execution exception occurs
   */
  public OAuth2AccessToken getAccessToken(String code, String stateVal)
      throws IOException, InterruptedException, ExecutionException {
    final OidcSessionState sessionState =
        sessionStateStore != null ? sessionStateStore.load(stateVal) : null;
    return getAccessToken(code, sessionState);
  }

  /**
   * Retrieves the access token using authorization code and performs ID Token validation using
   * session state.
   *
   * @param code the authorization code
   * @param sessionState the {@link OidcSessionState} containing nonce and code verifier, or {@code
   *     null}
   * @return the retrieved {@link OAuth2AccessToken}
   * @throws IOException if network or serialization error occurs
   * @throws InterruptedException if current thread is interrupted
   * @throws ExecutionException if execution exception occurs
   */
  public OAuth2AccessToken getAccessToken(String code, OidcSessionState sessionState)
      throws IOException, InterruptedException, ExecutionException {
    final com.github.scribejava.core.oauth2.grant.AuthorizationCodeGrant grant =
        new com.github.scribejava.core.oauth2.grant.AuthorizationCodeGrant(code);
    if (sessionState != null && sessionState.getCodeVerifier() != null) {
      grant.setPkceCodeVerifier(sessionState.getCodeVerifier());
    }
    final OAuth2AccessToken token = getAccessToken(grant);
    if (sessionState != null) {
      validateIdToken(token, sessionState.getNonce());
      if (sessionStateStore != null && sessionState.getState() != null) {
        sessionStateStore.remove(sessionState.getState());
      }
    } else {
      validateIdToken(token);
    }
    return token;
  }

  /**
   * Asynchronously retrieves the access token using authorization code and loads session state for
   * verification.
   *
   * @param code the authorization code
   * @param stateVal the state parameter value
   * @return a {@link CompletableFuture} for the {@link OAuth2AccessToken}
   */
  public CompletableFuture<OAuth2AccessToken> getAccessTokenAsync(String code, String stateVal) {
    final OidcSessionState sessionState =
        sessionStateStore != null ? sessionStateStore.load(stateVal) : null;
    return getAccessTokenAsync(code, sessionState);
  }

  /**
   * Asynchronously retrieves the access token using authorization code and performs ID Token
   * validation.
   *
   * @param code the authorization code
   * @param sessionState the {@link OidcSessionState} containing nonce and code verifier, or {@code
   *     null}
   * @return a {@link CompletableFuture} for the {@link OAuth2AccessToken}
   */
  public CompletableFuture<OAuth2AccessToken> getAccessTokenAsync(
      String code, OidcSessionState sessionState) {
    final com.github.scribejava.core.oauth2.grant.AuthorizationCodeGrant grant =
        new com.github.scribejava.core.oauth2.grant.AuthorizationCodeGrant(code);
    if (sessionState != null && sessionState.getCodeVerifier() != null) {
      grant.setPkceCodeVerifier(sessionState.getCodeVerifier());
    }
    return getAccessTokenAsync(grant)
        .thenApply(
            token -> {
              if (sessionState != null) {
                validateIdToken(token, sessionState.getNonce());
                if (sessionStateStore != null && sessionState.getState() != null) {
                  sessionStateStore.remove(sessionState.getState());
                }
              } else {
                validateIdToken(token);
              }
              return token;
            });
  }

  /**
   * Gets the session state store.
   *
   * @return the {@link OidcSessionStateStore}
   */
  public OidcSessionStateStore getSessionStateStore() {
    return sessionStateStore;
  }

  /**
   * Sets the session state store.
   *
   * @param sessionStateStore the {@link OidcSessionStateStore} to use
   */
  public void setSessionStateStore(OidcSessionStateStore sessionStateStore) {
    this.sessionStateStore = sessionStateStore;
  }
}
