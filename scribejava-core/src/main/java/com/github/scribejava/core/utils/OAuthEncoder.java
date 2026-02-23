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
package com.github.scribejava.core.utils;

// import java.io.UnsupportedEncodingException; // REMOVED IMPORT

import com.github.scribejava.core.exceptions.OAuthException;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Utilitaires pour l'encodage et le décodage d'URL conforme aux spécifications OAuth.
 */
public abstract class OAuthEncoder {

    // private static final String CHARSET = "UTF-8"; // REMOVED CONSTANT
    private static final Map<String, String> ENCODING_RULES;

    static {
        final Map<String, String> rules = new HashMap<>();
        rules.put("*", "%2A");
        rules.put("+", "%20");
        rules.put("%7E", "~");
        ENCODING_RULES = Collections.unmodifiableMap(rules);
    }

    /**
     * Encode une chaîne de caractères pour une utilisation dans une URL.
     *
     * @param plain La chaîne à encoder.
     * @return La chaîne encodée.
     */
    public static String encode(String plain) {
        Preconditions.checkNotNull(plain, "Cannot encode null object");
        String encoded;
        try {
            encoded = URLEncoder.encode(plain, StandardCharsets.UTF_8.name());
        } catch (java.io.UnsupportedEncodingException uee) {
            throw new OAuthException(
                    "Charset not found while encoding string: " + StandardCharsets.UTF_8.name(), uee);
        }
        for (Map.Entry<String, String> rule : ENCODING_RULES.entrySet()) {
            encoded = applyRule(encoded, rule.getKey(), rule.getValue());
        }
        return encoded;
    }

    private static String applyRule(String encoded, String toReplace, String replacement) {
        return encoded.replaceAll(Pattern.quote(toReplace), replacement);
    }

    /**
     * Décode une chaîne de caractères provenant d'une URL.
     *
     * @param encoded La chaîne à décoder.
     * @return La chaîne décodée.
     */
    public static String decode(String encoded) {
        Preconditions.checkNotNull(encoded, "Cannot decode null object");
        try {
            return URLDecoder.decode(encoded, StandardCharsets.UTF_8.name());
        } catch (java.io.UnsupportedEncodingException uee) {
            throw new OAuthException(
                    "Charset not found while decoding string: " + StandardCharsets.UTF_8.name(), uee);
        }
    }
}
