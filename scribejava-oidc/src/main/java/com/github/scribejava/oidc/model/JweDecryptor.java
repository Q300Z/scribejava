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
package com.github.scribejava.oidc.model;

import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.utils.JsonUtils;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.spec.MGF1ParameterSpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;

/**
 * Déchiffreur natif Java 8 pour les jetons JWE (JSON Web Encryption, RFC 7516) en sérialisation
 * compacte à 5 segments (notamment requis pour FranceConnect+).
 */
public class JweDecryptor {

  private static final int GCM_TAG_BITS = 128;
  private static final int GCM_TAG_BYTES = 16;

  /**
   * Déchiffre un jeton JWE compact avec une clé privée unique.
   *
   * @param compactJwe Le jeton JWE à 5 segments séparés par des points.
   * @param privateKey La clé privée de déchiffrement.
   * @return La charge utile déchiffrée en UTF-8 (ex. un JWS à 3 segments ou du JSON).
   */
  public String decrypt(String compactJwe, PrivateKey privateKey) {
    return decrypt(compactJwe, null, privateKey);
  }

  /**
   * Déchiffre un jeton JWE compact en sélectionnant la clé privée via le {@code kid} de l'en-tête
   * protégé, avec repli sur une clé par défaut.
   *
   * @param compactJwe Le jeton JWE à 5 segments séparés par des points.
   * @param decryptionKeys Dictionnaire associant les identifiants {@code kid} aux clés privées.
   * @param defaultKey Clé privée par défaut si le {@code kid} est absent ou non trouvé.
   * @return La charge utile déchiffrée en UTF-8.
   */
  public String decrypt(
      String compactJwe, Map<String, PrivateKey> decryptionKeys, PrivateKey defaultKey) {
    if (compactJwe == null) {
      throw new OAuthException("JWE token cannot be null");
    }
    final String[] parts = compactJwe.split("\\.", -1);
    if (parts.length != 5) {
      throw new OAuthException("Invalid JWE format. Expected 5 parts but found " + parts.length);
    }

    final Base64.Decoder urlDecoder = Base64.getUrlDecoder();
    final Map<String, Object> header;
    final byte[] encryptedKey;
    final byte[] iv;
    final byte[] ciphertext;
    final byte[] authTag;
    try {
      final String headerJson = new String(urlDecoder.decode(parts[0]), StandardCharsets.UTF_8);
      header = JsonUtils.parse(headerJson);
      encryptedKey = urlDecoder.decode(parts[1]);
      iv = urlDecoder.decode(parts[2]);
      ciphertext = urlDecoder.decode(parts[3]);
      authTag = urlDecoder.decode(parts[4]);
    } catch (Exception ex) {
      throw new OAuthException("Failed to decode JWE segments: " + ex.getMessage(), ex);
    }

    final String alg = getRequiredHeaderString(header, "alg");
    final String enc = getRequiredHeaderString(header, "enc");
    final Object kidObj = header.get("kid");
    final String kid = kidObj instanceof String ? (String) kidObj : null;

    final PrivateKey key = resolvePrivateKey(kid, decryptionKeys, defaultKey);
    final byte[] cek = decryptKey(alg, encryptedKey, key);
    final byte[] aad = parts[0].getBytes(StandardCharsets.US_ASCII);
    final byte[] plaintextBytes = decryptContent(enc, cek, iv, ciphertext, authTag, aad);

    final Object zipObj = header.get("zip");
    if ("DEF".equals(zipObj)) {
      return new String(decompressDeflate(plaintextBytes), StandardCharsets.UTF_8);
    }
    if (zipObj != null) {
      throw new OAuthException("Unsupported JWE compression algorithm: " + zipObj);
    }
    return new String(plaintextBytes, StandardCharsets.UTF_8);
  }

  private String getRequiredHeaderString(Map<String, Object> header, String name) {
    final Object val = header.get(name);
    if (!(val instanceof String) || ((String) val).trim().isEmpty()) {
      throw new OAuthException("Missing '" + name + "' in JWE protected header.");
    }
    return (String) val;
  }

  private PrivateKey resolvePrivateKey(
      String kid, Map<String, PrivateKey> decryptionKeys, PrivateKey defaultKey) {
    if (kid != null && decryptionKeys != null && decryptionKeys.containsKey(kid)) {
      final PrivateKey matched = decryptionKeys.get(kid);
      if (matched != null) {
        return matched;
      }
    }
    if (defaultKey != null) {
      return defaultKey;
    }
    throw new OAuthException(
        kid != null
            ? "No decryption key found for JWE kid: " + kid
            : "No decryption key configured for JWE.");
  }

  private byte[] decryptKey(String alg, byte[] encryptedKey, PrivateKey key) {
    try {
      final OAEPParameterSpec oaepSpec;
      if ("RSA-OAEP-256".equals(alg)) {
        oaepSpec =
            new OAEPParameterSpec(
                "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
      } else if ("RSA-OAEP".equals(alg)) {
        oaepSpec =
            new OAEPParameterSpec(
                "SHA-1", "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT);
      } else {
        throw new OAuthException("Unsupported JWE key management algorithm: " + alg);
      }
      final Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
      cipher.init(Cipher.DECRYPT_MODE, key, oaepSpec);
      return cipher.doFinal(encryptedKey);
    } catch (OAuthException ex) {
      throw ex;
    } catch (GeneralSecurityException ex) {
      throw new OAuthException("Failed to decrypt JWE content encryption key", ex);
    }
  }

  private byte[] decryptContent(
      String enc, byte[] cek, byte[] iv, byte[] ciphertext, byte[] authTag, byte[] aad) {
    if ("A128GCM".equals(enc)) {
      return decryptAesGcm(cek, 16, iv, ciphertext, authTag, aad);
    }
    if ("A192GCM".equals(enc)) {
      return decryptAesGcm(cek, 24, iv, ciphertext, authTag, aad);
    }
    if ("A256GCM".equals(enc)) {
      return decryptAesGcm(cek, 32, iv, ciphertext, authTag, aad);
    }
    if ("A128CBC-HS256".equals(enc)) {
      return decryptAesCbcHmac(cek, 32, "HmacSHA256", iv, ciphertext, authTag, aad);
    }
    if ("A192CBC-HS384".equals(enc)) {
      return decryptAesCbcHmac(cek, 48, "HmacSHA384", iv, ciphertext, authTag, aad);
    }
    if ("A256CBC-HS512".equals(enc)) {
      return decryptAesCbcHmac(cek, 64, "HmacSHA512", iv, ciphertext, authTag, aad);
    }
    throw new OAuthException("Unsupported JWE content encryption algorithm: " + enc);
  }

  private byte[] decryptAesGcm(
      byte[] cek, int expectedCekLen, byte[] iv, byte[] ciphertext, byte[] authTag, byte[] aad) {
    if (cek.length != expectedCekLen) {
      throw new OAuthException(
          "Invalid CEK length for AES-GCM: expected " + expectedCekLen + " got " + cek.length);
    }
    if (authTag.length != GCM_TAG_BYTES) {
      throw new OAuthException("Invalid JWE authentication tag length: " + authTag.length);
    }
    try {
      final byte[] cipherWithTag = new byte[ciphertext.length + authTag.length];
      System.arraycopy(ciphertext, 0, cipherWithTag, 0, ciphertext.length);
      System.arraycopy(authTag, 0, cipherWithTag, ciphertext.length, authTag.length);

      final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(
          Cipher.DECRYPT_MODE,
          new SecretKeySpec(cek, "AES"),
          new GCMParameterSpec(GCM_TAG_BITS, iv));
      cipher.updateAAD(aad);
      return cipher.doFinal(cipherWithTag);
    } catch (GeneralSecurityException ex) {
      throw new OAuthException("JWE AES-GCM decryption failed", ex);
    }
  }

  private byte[] decryptAesCbcHmac(
      byte[] cek,
      int expectedCekLen,
      String hmacAlg,
      byte[] iv,
      byte[] ciphertext,
      byte[] authTag,
      byte[] aad) {
    if (cek.length != expectedCekLen) {
      throw new OAuthException(
          "Invalid CEK length for AES-CBC-HMAC: expected " + expectedCekLen + " got " + cek.length);
    }
    final int halfLen = expectedCekLen / 2;
    if (authTag.length != halfLen) {
      throw new OAuthException("Invalid JWE authentication tag length: " + authTag.length);
    }
    try {
      final byte[] macKey = Arrays.copyOfRange(cek, 0, halfLen);
      final byte[] encKey = Arrays.copyOfRange(cek, halfLen, expectedCekLen);

      final long aadBits = aad.length * 8L;
      final byte[] al = ByteBuffer.allocate(8).putLong(aadBits).array();

      final Mac mac = Mac.getInstance(hmacAlg);
      mac.init(new SecretKeySpec(macKey, hmacAlg));
      mac.update(aad);
      mac.update(iv);
      mac.update(ciphertext);
      mac.update(al);
      final byte[] fullTag = mac.doFinal();
      final byte[] expectedTag = Arrays.copyOfRange(fullTag, 0, halfLen);

      if (!MessageDigest.isEqual(expectedTag, authTag)) {
        throw new OAuthException("JWE authentication tag verification failed");
      }

      final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(encKey, "AES"), new IvParameterSpec(iv));
      return cipher.doFinal(ciphertext);
    } catch (OAuthException ex) {
      throw ex;
    } catch (GeneralSecurityException ex) {
      throw new OAuthException("JWE AES-CBC-HMAC decryption failed", ex);
    }
  }

  private byte[] decompressDeflate(byte[] compressed) {
    final Inflater inflater = new Inflater(true);
    try {
      inflater.setInput(compressed);
      final ByteArrayOutputStream out = new ByteArrayOutputStream(compressed.length * 2);
      final byte[] buffer = new byte[1024];
      while (!inflater.finished()) {
        final int count = inflater.inflate(buffer);
        if (count == 0 && inflater.needsInput()) {
          break;
        }
        out.write(buffer, 0, count);
      }
      return out.toByteArray();
    } catch (DataFormatException ex) {
      throw new OAuthException("Failed to decompress JWE payload", ex);
    } finally {
      inflater.end();
    }
  }
}
