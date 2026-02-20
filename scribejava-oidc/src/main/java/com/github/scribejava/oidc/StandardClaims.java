package com.github.scribejava.oidc;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * OpenID Connect Standard Claims.
 */
public class StandardClaims {

    private final Map<String, Object> claims;

    public StandardClaims(final Map<String, Object> claims) {
        this.claims = claims != null ? Collections.unmodifiableMap(claims) : Collections.emptyMap();
    }

    public Optional<String> getSub() {
        return getClaim("sub");
    }

    public Optional<String> getName() {
        return getClaim("name");
    }

    public Optional<String> getGivenName() {
        return getClaim("given_name");
    }

    public Optional<String> getFamilyName() {
        return getClaim("family_name");
    }

    public Optional<String> getMiddleName() {
        return getClaim("middle_name");
    }

    public Optional<String> getNickname() {
        return getClaim("nickname");
    }

    public Optional<String> getPreferredUsername() {
        return getClaim("preferred_username");
    }

    public Optional<String> getProfile() {
        return getClaim("profile");
    }

    public Optional<String> getPicture() {
        return getClaim("picture");
    }

    public Optional<String> getWebsite() {
        return getClaim("website");
    }

    public Optional<String> getEmail() {
        return getClaim("email");
    }

    public Optional<Boolean> isEmailVerified() {
        return getClaim("email_verified");
    }

    public Optional<String> getGender() {
        return getClaim("gender");
    }

    public Optional<String> getBirthdate() {
        return getClaim("birthdate");
    }

    public Optional<String> getZoneinfo() {
        return getClaim("zoneinfo");
    }

    public Optional<String> getLocale() {
        return getClaim("locale");
    }

    public Optional<String> getPhoneNumber() {
        return getClaim("phone_number");
    }

    public Optional<Boolean> isPhoneNumberVerified() {
        return getClaim("phone_number_verified");
    }

    public Optional<Object> getAddress() {
        return getClaim("address");
    }

    public Optional<Long> getUpdatedAt() {
        return getClaim("updated_at");
    }

    @SuppressWarnings("unchecked")
    private <T> Optional<T> getClaim(final String key) {
        return Optional.ofNullable((T) claims.get(key));
    }

    public Map<String, Object> getAllClaims() {
        return claims;
    }
}
