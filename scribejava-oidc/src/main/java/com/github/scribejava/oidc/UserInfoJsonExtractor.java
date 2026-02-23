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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.core.model.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Extracteur pour la réponse JSON du point de terminaison UserInfo d'OpenID Connect.
 *
 * <p>Analyse le corps de la réponse HTTP pour construire une instance de {@link StandardClaims}.
 *
 * @see <a href="http://openid.net/specs/openid-connect-core-1_0.html#UserInfo">OpenID Connect Core
 * 1.0, Section 5.3 (UserInfo Endpoint)</a>
 */
public class UserInfoJsonExtractor {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Constructeur protégé.
     */
    protected UserInfoJsonExtractor() {
    }

    /**
     * Retourne l'instance unique (singleton) de l'extracteur.
     *
     * @return L'instance de {@link UserInfoJsonExtractor}.
     */
    public static UserInfoJsonExtractor instance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * Extrait les revendications (Claims) à partir de la réponse HTTP.
     *
     * @param response La réponse HTTP reçue du point de terminaison UserInfo.
     * @return Une instance de {@link StandardClaims} contenant les données extraites.
     * @throws IOException si le corps de la réponse ne peut pas être analysé comme du JSON.
     * @see <a href="http://openid.net/specs/openid-connect-core-1_0.html#UserInfoResponse">Section
     * 5.3.2 (Successful UserInfo Response)</a>
     */
    public StandardClaims extract(final Response response) throws IOException {
        final JsonNode node = OBJECT_MAPPER.readTree(response.getBody());
        final Map<String, Object> claimsMap = new HashMap<>();

        final Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            final Map.Entry<String, JsonNode> entry = fields.next();
            final JsonNode value = entry.getValue();
            if (value.isBoolean()) {
                claimsMap.put(entry.getKey(), value.asBoolean());
            } else if (value.isNumber()) {
                claimsMap.put(entry.getKey(), value.numberValue());
            } else {
                claimsMap.put(entry.getKey(), value.asText());
            }
        }
        return new StandardClaims(claimsMap);
    }

    private static class InstanceHolder {
        private static final UserInfoJsonExtractor INSTANCE = new UserInfoJsonExtractor();
    }
}
