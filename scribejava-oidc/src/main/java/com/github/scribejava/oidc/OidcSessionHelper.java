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

import com.github.scribejava.core.utils.OAuthEncoder;

/**
 * Aide à la génération de code HTML pour la gestion de session (Session Management) et la
 * déconnexion OIDC.
 *
 * <p>Fournit des méthodes pour créer les iframes nécessaires à la surveillance de l'état de session
 * et à la notification de déconnexion via le navigateur (Front-Channel).
 *
 * @see <a href="http://openid.net/specs/openid-connect-session-1_0.html">OpenID Connect Session
 *     Management 1.0</a>
 * @see <a href="https://openid.net/specs/openid-connect-frontchannel-1_0.html">OpenID Connect
 *     Front-Channel Logout 1.0</a>
 */
public class OidcSessionHelper {

  private static final int SESSION_CHECK_INTERVAL_MS = 5000;

  private OidcSessionHelper() {}

  /**
   * Génère l'URL de déconnexion RP-Initiated Logout.
   *
   * @param endSessionEndpoint L'URL du point de terminaison de fin de session.
   * @param idTokenHint Le jeton d'identité (ID Token) précédemment émis.
   * @param postLogoutRedirectUri L'URL de redirection après déconnexion.
   * @param state L'état à transmettre à la redirection.
   * @param clientId L'identifiant du client.
   * @return L'URL de déconnexion formatée avec les paramètres encodés.
   */
  public static String getLogoutUrl(
      String endSessionEndpoint,
      String idTokenHint,
      String postLogoutRedirectUri,
      String state,
      String clientId) {
    if (endSessionEndpoint == null) {
      return null;
    }
    final StringBuilder url = new StringBuilder(endSessionEndpoint);
    boolean first = !endSessionEndpoint.contains("?");

    if (idTokenHint != null && !idTokenHint.isEmpty()) {
      url.append(first ? "?" : "&")
          .append("id_token_hint=")
          .append(OAuthEncoder.encode(idTokenHint));
      first = false;
    }
    if (postLogoutRedirectUri != null && !postLogoutRedirectUri.isEmpty()) {
      url.append(first ? "?" : "&")
          .append("post_logout_redirect_uri=")
          .append(OAuthEncoder.encode(postLogoutRedirectUri));
      first = false;
    }
    if (state != null && !state.isEmpty()) {
      url.append(first ? "?" : "&").append("state=").append(OAuthEncoder.encode(state));
      first = false;
    }
    if (clientId != null && !clientId.isEmpty()) {
      url.append(first ? "?" : "&").append("client_id=").append(OAuthEncoder.encode(clientId));
      first = false;
    }
    return url.toString();
  }

  /**
   * Génère le code HTML de l'iframe RP pour la gestion de session.
   *
   * <p>Cette iframe communique avec l'iframe du fournisseur (OP) via {@code postMessage} pour
   * vérifier si l'état de la session utilisateur a changé chez le fournisseur.
   *
   * @param opCheckSessionIframe L'URL {@code check_session_iframe} provenant des métadonnées de
   *     l'OP.
   * @param clientId L'identifiant du client (Client ID).
   * @param sessionState La valeur {@code session_state} reçue dans la réponse d'autorisation.
   * @return Le code HTML de l'iframe sous forme de chaîne de caractères.
   * @see <a href="http://openid.net/specs/openid-connect-session-1_0.html#RPiframe">OpenID Connect
   *     Session Management 1.0, Section 3.1 (RP iframe)</a>
   */
  public static String getSessionManagementIframeHtml(
      final String opCheckSessionIframe, final String clientId, final String sessionState) {
    return "<html><body>"
        + "<script>"
        + "  var targetOrigin = '"
        + escapeJs(opCheckSessionIframe)
        + "';"
        + "  var clientId = '"
        + escapeJs(clientId)
        + "';"
        + "  var sessionState = '"
        + escapeJs(sessionState)
        + "';"
        + "  var mes = clientId + ' ' + sessionState;"
        + "  function checkSession() {"
        + "    var win = window.parent.frames['op_iframe'].contentWindow;"
        + "    win.postMessage(mes, targetOrigin);"
        + "  }"
        + "  setInterval(checkSession, "
        + SESSION_CHECK_INTERVAL_MS
        + ");"
        + "  window.addEventListener('message', function(e) {"
        + "    if (e.origin !== targetOrigin) return;"
        + "    if (e.data === 'changed') window.parent.location.reload();"
        + "  }, false);"
        + "</script>"
        + "</body></html>";
  }

  /**
   * Génère le code HTML pour l'iframe de déconnexion via le navigateur (Front-Channel Logout).
   *
   * <p>Cette iframe est utilisée par le fournisseur pour notifier le client de la fin de session
   * utilisateur.
   *
   * @param logoutUri L'URL {@code frontchannel_logout_uri} du client.
   * @param issuer L'identifiant de l'émetteur (iss).
   * @param sid L'identifiant de session (sid).
   * @return Le tag HTML {@code <iframe>}.
   * @see <a href="https://openid.net/specs/openid-connect-frontchannel-1_0.html#RPLogout">OpenID
   *     Connect Front-Channel Logout 1.0, Section 2 (RP Logout Functionality)</a>
   */
  public static String getFrontChannelLogoutIframeHtml(
      final String logoutUri, final String issuer, final String sid) {
    String url = logoutUri;
    if (issuer != null && sid != null) {
      url +=
          (url.contains("?") ? "&" : "?")
              + "iss="
              + OAuthEncoder.encode(issuer)
              + "&sid="
              + OAuthEncoder.encode(sid);
    }
    return "<iframe src=\"" + escapeHtmlAttr(url) + "\" style=\"display:none\"></iframe>";
  }

  private static String escapeJs(String value) {
    if (value == null) {
      return "";
    }
    return value
        .replace("\\", "\\\\")
        .replace("'", "\\'")
        .replace("\"", "\\\"")
        .replace("\r", "\\r")
        .replace("\n", "\\n");
  }

  private static String escapeHtmlAttr(String value) {
    if (value == null) {
      return "";
    }
    return value.replace("\"", "&quot;");
  }
}
