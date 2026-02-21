package com.github.scribejava.oidc;

import com.github.scribejava.core.utils.OAuthEncoder;

/**
 * Helper to generate HTML for OIDC Session Management and Logout.
 */
public class OidcSessionHelper {

    private static final int SESSION_CHECK_INTERVAL_MS = 5000;

    private OidcSessionHelper() {
    }

    /**
     * Generates the RP iframe HTML for Session Management.
     *
     * @param opCheckSessionIframe The check_session_iframe URL from OP metadata.
     * @param clientId             The client ID.
     * @param sessionState         The session_state from the authentication response.
     * @return HTML string for the RP iframe.
     */
    public static String getSessionManagementIframeHtml(final String opCheckSessionIframe, final String clientId,
                                                        final String sessionState) {
        return "<html><body>"
                + "<script>"
                + "  var targetOrigin = '" + opCheckSessionIframe + "';"
                + "  var clientId = '" + clientId + "';"
                + "  var sessionState = '" + sessionState + "';"
                + "  var mes = clientId + ' ' + sessionState;"
                + "  function checkSession() {"
                + "    var win = window.parent.frames['op_iframe'].contentWindow;"
                + "    win.postMessage(mes, targetOrigin);"
                + "  }"
                + "  setInterval(checkSession, " + SESSION_CHECK_INTERVAL_MS + ");"
                + "  window.addEventListener('message', function(e) {"
                + "    if (e.origin !== targetOrigin) return;"
                + "    if (e.data === 'changed') window.parent.location.reload();"
                + "  }, false);"
                + "</script>"
                + "</body></html>";
    }

    /**
     * Generates the Front-Channel Logout iframe HTML.
     *
     * @param logoutUri The frontchannel_logout_uri.
     * @param issuer    The issuer (iss).
     * @param sid       The session ID (sid).
     * @return HTML string for the logout iframe.
     */
    public static String getFrontChannelLogoutIframeHtml(final String logoutUri, final String issuer,
                                                         final String sid) {
        String url = logoutUri;
        if (issuer != null && sid != null) {
            url += (url.contains("?") ? "&" : "?") + "iss=" + OAuthEncoder.encode(issuer) + "&sid="
                    + OAuthEncoder.encode(sid);
        }
        return "<iframe src=\"" + url + "\" style=\"display:none\"></iframe>";
    }
}
