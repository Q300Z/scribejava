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
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.httpclient.jdk.JDKHttpClient;
import com.github.scribejava.core.model.JsonBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.oidc.model.EcOidcKey;
import com.github.scribejava.oidc.model.JweDecryptor;
import com.github.scribejava.oidc.model.OidcKey;
import com.github.scribejava.oidc.model.OidcNonce;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.security.PrivateKey;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests TDD pour le déchiffrement JWE natif Java 8 (profil FranceConnect+). */
public class JweDecryptorTest {

  private MockWebServer server;
  private RSAKey encKey1;
  private RSAKey encKey2;
  private ECKey sigEcKey;

  /**
   * Initialise le serveur mock et les paires de clés de test.
   *
   * @throws Exception en cas d'erreur d'initialisation.
   */
  @BeforeEach
  public void setUp() throws Exception {
    server = new MockWebServer();
    server.start();
    encKey1 = new RSAKeyGenerator(2048).keyID("enc-kid-1").generate();
    encKey2 = new RSAKeyGenerator(2048).keyID("enc-kid-2").generate();
    sigEcKey = new ECKeyGenerator(Curve.P_256).keyID("sig-ec-kid").generate();
  }

  /**
   * Arrête le serveur mock.
   *
   * @throws Exception en cas d'erreur.
   */
  @AfterEach
  public void tearDown() throws Exception {
    server.shutdown();
  }

  private String encryptAsJwe(
      String plaintext, RSAKey recipientKey, JWEAlgorithm alg, EncryptionMethod enc)
      throws Exception {
    final JWEHeader header =
        new JWEHeader.Builder(alg, enc).keyID(recipientKey.getKeyID()).contentType("JWT").build();
    final JWEObject jweObject = new JWEObject(header, new Payload(plaintext));
    jweObject.encrypt(new RSAEncrypter(recipientKey.toRSAPublicKey()));
    return jweObject.serialize();
  }

  private String signEcJwt(JWTClaimsSet claims) throws Exception {
    final SignedJWT signedJWT =
        new SignedJWT(
            new JWSHeader.Builder(JWSAlgorithm.ES256).keyID(sigEcKey.getKeyID()).build(), claims);
    signedJWT.sign(new ECDSASigner(sigEcKey));
    return signedJWT.serialize();
  }

  @Test
  public void shouldDecryptRsaOaep256A256GcmAndCbcHmacWithKidSelection() throws Exception {
    final JweDecryptor decryptor = new JweDecryptor();

    final String jweGcm =
        encryptAsJwe(
            "{\"sub\":\"user-fc-plus\"}",
            encKey1,
            JWEAlgorithm.RSA_OAEP_256,
            EncryptionMethod.A256GCM);
    assertThat(decryptor.decrypt(jweGcm, encKey1.toPrivateKey()))
        .isEqualTo("{\"sub\":\"user-fc-plus\"}");

    final String jweCbc =
        encryptAsJwe(
            "{\"sub\":\"user-cbc\"}",
            encKey2,
            JWEAlgorithm.RSA_OAEP,
            EncryptionMethod.A128CBC_HS256);
    final Map<String, PrivateKey> keys = new HashMap<>();
    keys.put("enc-kid-1", encKey1.toPrivateKey());
    keys.put("enc-kid-2", encKey2.toPrivateKey());
    assertThat(decryptor.decrypt(jweCbc, keys, null)).isEqualTo("{\"sub\":\"user-cbc\"}");
  }

  @Test
  public void shouldRejectTamperedOrWrongKeyJwe() throws Exception {
    final JweDecryptor decryptor = new JweDecryptor();
    final String jwe =
        encryptAsJwe(
            "secret-payload", encKey1, JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256GCM);

    assertThrows(OAuthException.class, () -> decryptor.decrypt(jwe, encKey2.toPrivateKey()));

    final String[] parts = jwe.split("\\.");
    final String tamperedTag = parts[4].substring(0, parts[4].length() - 2) + "AA";
    final String tamperedJwe =
        parts[0] + "." + parts[1] + "." + parts[2] + "." + parts[3] + "." + tamperedTag;
    assertThrows(
        OAuthException.class, () -> decryptor.decrypt(tamperedJwe, encKey1.toPrivateKey()));
  }

  @Test
  public void shouldValidateJweIdTokenAndJweUserInfoInOidcService() throws Exception {
    final String baseUrl = server.url("/api/v2").toString();
    final OidcFranceConnectApi20 api = OidcFranceConnectApi20.custom(baseUrl);
    final Map<String, OidcKey> sigKeys =
        Collections.singletonMap(
            sigEcKey.getKeyID(),
            new EcOidcKey(sigEcKey.getKeyID(), "ES256", sigEcKey.toECPublicKey()));
    final IdTokenValidator validator =
        new IdTokenValidator(baseUrl, "client-fc-plus", "ES256", sigKeys);

    final OidcService service =
        new OidcServiceBuilder("client-fc-plus")
            .apiSecret("secret")
            .httpClient(new JDKHttpClient())
            .build(api);
    service.setIdTokenValidator(validator);

    final OidcNonce nonce = OidcNonce.generate();
    final Date exp = new Date(System.currentTimeMillis() + 60_000L);
    final String idJws =
        signEcJwt(
            new JWTClaimsSet.Builder()
                .subject("fc-plus-sub-1")
                .issuer(baseUrl)
                .audience("client-fc-plus")
                .claim("nonce", nonce.getValue())
                .expirationTime(exp)
                .build());
    final String idJwe =
        encryptAsJwe(idJws, encKey1, JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256GCM);

    final String userInfoJws =
        signEcJwt(
            new JWTClaimsSet.Builder()
                .subject("fc-plus-sub-1")
                .issuer(baseUrl)
                .audience("client-fc-plus")
                .claim("given_name", "Marianne")
                .claim("family_name", "République")
                .claim("email", "marianne@example.gouv.fr")
                .expirationTime(exp)
                .build());
    final String userInfoJwe =
        encryptAsJwe(userInfoJws, encKey1, JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256GCM);

    final String rawTokenResponse =
        new JsonBuilder()
            .add("access_token", "fc-plus-access-token")
            .add("token_type", "Bearer")
            .add("id_token", idJwe)
            .build();
    final OAuth2AccessToken token = new OAuth2AccessToken("fc-plus-access-token", rawTokenResponse);

    // Sans clé de déchiffrement configurée -> rejet explicite
    assertThrows(OAuthException.class, () -> service.validateIdToken(token, nonce));

    // Avec clé de déchiffrement -> validation complète de l'id_token JWE+JWS et du /userinfo
    // JWE+JWS
    service.setDecryptionKey(encKey1.toPrivateKey());
    final IdToken validatedIdToken = service.validateIdToken(token, nonce);
    assertThat(validatedIdToken.getStandardClaims().getSub()).contains("fc-plus-sub-1");

    server.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "application/jwt")
            .setBody(userInfoJwe));

    final StandardClaims claims = service.getUserInfo(token);
    assertThat(claims.getSub()).contains("fc-plus-sub-1");
    assertThat(claims.getGivenName()).contains("Marianne");
    assertThat(claims.getEmail()).contains("marianne@example.gouv.fr");
  }
}
