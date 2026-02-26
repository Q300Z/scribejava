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
package com.github.scribejava.core.model;

import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.httpclient.multipart.MultipartPayload;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/** Représentation d'une requête HTTP OAuth. */
public class OAuthRequest {

  private static final String OAUTH_PREFIX = "oauth_";

  private final String url;
  private final Verb verb;
  private final ParameterList querystringParams = new ParameterList();
  private final ParameterList bodyParams = new ParameterList();
  private final Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
  private final Map<String, String> oauthParameters = new HashMap<>();
  private String charset;
  private String stringPayload;
  private byte[] byteArrayPayload;
  private File filePayload;
  private MultipartPayload multipartPayload;
  private String realm;

  /**
   * Constructeur par défaut.
   *
   * @param verb Le verbe HTTP (GET, POST, etc.).
   * @param url L'URL de la ressource.
   */
  public OAuthRequest(Verb verb, String url) {
    this.verb = verb;
    this.url = url;
  }

  /**
   * Ajoute un paramètre OAuth.
   *
   * @param key Le nom du paramètre (doit commencer par 'oauth_', ou être 'scope' ou 'realm').
   * @param value La valeur du paramètre.
   * @throws IllegalArgumentException si le paramètre n'est pas un paramètre OAuth valide.
   */
  public void addOAuthParameter(String key, String value) {
    oauthParameters.put(checkKey(key), value);
  }

  private String checkKey(String key) {
    if (key.startsWith(OAUTH_PREFIX)
        || key.equals(OAuthConstants.SCOPE)
        || key.equals(OAuthConstants.REALM)) {
      return key;
    } else {
      throw new IllegalArgumentException(
          String.format(
              "OAuth parameters must either be '%s', '%s' or start with '%s'",
              OAuthConstants.SCOPE, OAuthConstants.REALM, OAUTH_PREFIX));
    }
  }

  /**
   * @return Le dictionnaire des paramètres OAuth.
   */
  public Map<String, String> getOauthParameters() {
    return oauthParameters;
  }

  /**
   * @return Le royaume (realm) d'authentification.
   */
  public String getRealm() {
    return realm;
  }

  /**
   * @param realm Le royaume d'authentification à définir.
   */
  public void setRealm(String realm) {
    this.realm = realm;
  }

  /**
   * Retourne l'URL complète (hôte + ressource + paramètres de requête encodés).
   *
   * @return L'URL complète.
   */
  public String getCompleteUrl() {
    return querystringParams.appendTo(url);
  }

  /**
   * Ajoute un en-tête HTTP à la requête.
   *
   * @param key Le nom de l'en-tête.
   * @param value La valeur de l'en-tête.
   */
  public void addHeader(String key, String value) {
    headers.put(key, value);
  }

  /**
   * Ajoute un paramètre au corps de la requête (pour POST/PUT).
   *
   * @param key Le nom du paramètre.
   * @param value La valeur du paramètre.
   */
  public void addBodyParameter(String key, String value) {
    bodyParams.add(key, value);
  }

  /**
   * Ajoute un paramètre à la chaîne de requête (QueryString).
   *
   * @param key Le nom du paramètre.
   * @param value La valeur du paramètre.
   */
  public void addQuerystringParameter(String key, String value) {
    querystringParams.add(key, value);
  }

  /**
   * Ajoute un paramètre de manière intelligente selon le verbe HTTP.
   *
   * @param key Le nom du paramètre.
   * @param value La valeur du paramètre.
   */
  public void addParameter(String key, String value) {
    if (verb.isPermitBody()) {
      bodyParams.add(key, value);
    } else {
      querystringParams.add(key, value);
    }
  }

  /**
   * @return La charge utile multipart, ou null.
   */
  public MultipartPayload getMultipartPayload() {
    return multipartPayload;
  }

  /**
   * Définit la charge utile sous forme de chaîne (ex: JSON, XML).
   *
   * @param payload Le contenu du corps.
   */
  public void setPayload(String payload) {
    resetPayload();
    stringPayload = payload;
  }

  /**
   * Définit la charge utile sous forme de tableau d'octets.
   *
   * @param payload Le contenu binaire.
   */
  public void setPayload(byte[] payload) {
    resetPayload();
    byteArrayPayload = payload.clone();
  }

  /**
   * Définit la charge utile sous forme de fichier.
   *
   * @param payload Le fichier à envoyer.
   */
  public void setPayload(File payload) {
    resetPayload();
    filePayload = payload;
  }

  private void resetPayload() {
    stringPayload = null;
    byteArrayPayload = null;
    filePayload = null;
    multipartPayload = null;
  }

  /**
   * Récupère la liste des paramètres de la chaîne de requête.
   *
   * @return Un {@link ParameterList} contenant les paramètres.
   * @throws OAuthException si l'URL est malformée.
   */
  public ParameterList getQueryStringParams() {
    try {
      final ParameterList result = new ParameterList();
      final String queryString = new URL(url).getQuery();
      result.addQuerystring(queryString);
      result.addAll(querystringParams);
      return result;
    } catch (MalformedURLException mue) {
      throw new OAuthException("Malformed URL", mue);
    }
  }

  /**
   * Récupère la liste des paramètres du corps de la requête.
   *
   * @return Un {@link ParameterList} contenant les paramètres du corps.
   */
  public ParameterList getBodyParams() {
    return bodyParams;
  }

  /**
   * @return L'URL d'origine de la requête.
   */
  public String getUrl() {
    return url;
  }

  /**
   * Retourne l'URL nettoyée (sans port standard ni chaîne de requête).
   *
   * @return L'URL pour la signature OAuth.
   */
  public String getSanitizedUrl() {
    if (url.startsWith("http://") && (url.endsWith(":80") || url.contains(":80/"))) {
      return url.replaceAll("\\?.*", "").replaceAll(":80", "");
    } else if (url.startsWith("https://") && (url.endsWith(":443") || url.contains(":443/"))) {
      return url.replaceAll("\\?.*", "").replaceAll(":443", "");
    } else {
      return url.replaceAll("\\?.*", "");
    }
  }

  /**
   * @return La charge utile sous forme de chaîne.
   */
  public String getStringPayload() {
    return stringPayload;
  }

  /**
   * @return La charge utile sous forme de tableau d'octets.
   */
  public byte[] getByteArrayPayload() {
    if (byteArrayPayload != null) {
      return byteArrayPayload;
    }
    final String body = bodyParams.asFormUrlEncodedString();
    try {
      return body.getBytes(getCharset());
    } catch (UnsupportedEncodingException uee) {
      throw new OAuthException("Unsupported Charset: " + getCharset(), uee);
    }
  }

  /**
   * @return La charge utile sous forme de fichier.
   */
  public File getFilePayload() {
    return filePayload;
  }

  @Override
  public String toString() {
    return String.format("@Request(%s %s)", getVerb(), getUrl());
  }

  /**
   * Retourne une commande cURL prête à l'emploi (secrets masqués par défaut).
   *
   * @return String
   */
  public String toCurlCommand() {
    return toCurlCommand(true);
  }

  /**
   * Retourne une commande cURL prête à l'emploi.
   *
   * @param redactSecrets true pour masquer les secrets.
   * @return String
   */
  public String toCurlCommand(boolean redactSecrets) {
    final StringBuilder sb = new StringBuilder("curl -X ");
    sb.append(getVerb()).append(" '").append(getCompleteUrl()).append('\'');

    for (final Map.Entry<String, String> entry : headers.entrySet()) {
      String val = entry.getValue();
      if (redactSecrets && "Authorization".equalsIgnoreCase(entry.getKey())) {
        if (val.startsWith("Bearer ") || val.startsWith("bearer ")) {
          val = val.substring(0, 7) + "[REDACTED]";
        } else {
          val = "[REDACTED]";
        }
      }
      sb.append(" -H '").append(entry.getKey()).append(": ").append(val).append('\'');
    }

    if (verb.isPermitBody()) {
      if (stringPayload != null) {
        sb.append(" --data '").append(stringPayload).append('\'');
      } else if (!bodyParams.isEmpty()) {
        sb.append(" --data '");
        boolean first = true;
        for (final Parameter p : bodyParams.getParams()) {
          if (!first) {
            sb.append('&');
          }
          String val = p.getValue();
          if (redactSecrets
              && (p.getKey().contains("secret")
                  || p.getKey().contains("token")
                  || "code".equals(p.getKey()))) {
            val = "[REDACTED]";
          }
          sb.append(p.getKey()).append('=').append(val);
          first = false;
        }
        sb.append('\'');
      }
    }

    return sb.toString();
  }

  /**
   * Retourne une représentation lisible de la requête avec masquage des secrets.
   *
   * @return String
   */
  public String toDebugString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(getVerb()).append(' ').append(getUrl()).append('\n');

    sb.append("Headers:\n");
    for (final Map.Entry<String, String> entry : headers.entrySet()) {
      String val = entry.getValue();
      if ("Authorization".equalsIgnoreCase(entry.getKey())) {
        if (val.startsWith("Bearer ") || val.startsWith("bearer ")) {
          val = val.substring(0, 7) + "[REDACTED]";
        } else {
          val = "[REDACTED]";
        }
      }
      sb.append("  ").append(entry.getKey()).append('=').append(val).append('\n');
    }

    sb.append("Parameters:\n");
    appendParams(sb, querystringParams, "Query");
    appendParams(sb, bodyParams, "Body");

    return sb.toString();
  }

  private void appendParams(StringBuilder sb, ParameterList params, String type) {
    if (params.isEmpty()) {
      return;
    }
    sb.append("  ").append(type).append(":\n");
    for (final Parameter p : params.getParams()) {
      String val = p.getValue();
      if (p.getKey().contains("secret")
          || p.getKey().contains("token")
          || "code".equals(p.getKey())) {
        val = "[REDACTED]";
      }
      sb.append("    ").append(p.getKey()).append('=').append(val).append('\n');
    }
  }

  /**
   * @return Le verbe HTTP.
   */
  public Verb getVerb() {
    return verb;
  }

  /**
   * @return Le dictionnaire des en-têtes HTTP.
   */
  public Map<String, String> getHeaders() {
    return headers;
  }

  /**
   * @return L'encodage de caractères utilisé (UTF-8 par défaut).
   */
  public String getCharset() {
    return charset == null ? Charset.defaultCharset().name() : charset;
  }

  /**
   * @param charsetName Le nom du charset à utiliser.
   */
  public void setCharset(String charsetName) {
    charset = charsetName;
  }

  /**
   * Définit l'en-tête DPoP.
   *
   * @param dpopProof Le jeton JWT de preuve DPoP.
   * @see <a href="https://tools.ietf.org/html/rfc9449">RFC 9449 (DPoP)</a>
   */
  public void setDPoPProof(String dpopProof) {
    addHeader("DPoP", dpopProof);
  }

  /**
   * Interface pour convertir une réponse brute en un objet de type T.
   *
   * @param <T> Le type cible de la conversion.
   */
  public interface ResponseConverter<T> {

    ResponseConverter<Response> IDENTITY = response -> response;

    /**
     * Convertit la réponse HTTP.
     *
     * <p>L'implémentation doit fermer la réponse si elle n'est pas incluse dans l'objet retourné.
     *
     * @param response La réponse HTTP brute.
     * @return L'objet converti de type T.
     * @throws IOException en cas d'erreur de lecture.
     */
    T convert(Response response) throws IOException;
  }
}
