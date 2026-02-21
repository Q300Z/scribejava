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

    public static String encode(String plain) {
        Preconditions.checkNotNull(plain, "Cannot encode null object");
        String encoded;
        try {
            encoded = URLEncoder.encode(plain, StandardCharsets.UTF_8.name());
        } catch (java.io.UnsupportedEncodingException uee) {
            throw new OAuthException("Charset not found while encoding string: " + StandardCharsets.UTF_8.name(), uee);
        }
        for (Map.Entry<String, String> rule : ENCODING_RULES.entrySet()) {
            encoded = applyRule(encoded, rule.getKey(), rule.getValue());
        }
        return encoded;
    }

    private static String applyRule(String encoded, String toReplace, String replacement) {
        return encoded.replaceAll(Pattern.quote(toReplace), replacement);
    }

    public static String decode(String encoded) {
        Preconditions.checkNotNull(encoded, "Cannot decode null object");
        try {
            return URLDecoder.decode(encoded, StandardCharsets.UTF_8.name());
        } catch (java.io.UnsupportedEncodingException uee) {
            throw new OAuthException("Charset not found while decoding string: " + StandardCharsets.UTF_8.name(), uee);
        }
    }
}
