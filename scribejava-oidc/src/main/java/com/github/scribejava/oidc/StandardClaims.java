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

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Représente les revendications standards (Standard Claims) OpenID Connect.
 *
 * <p>Ces revendications fournissent des informations de profil sur l'utilisateur final.
 *
 * @see <a href="http://openid.net/specs/openid-connect-core-1_0.html#StandardClaims">OpenID Connect
 * Core 1.0, Section 5.1 (Standard Claims)</a>
 */
public class StandardClaims {

    private final Map<String, Object> claims;

    /**
     * Construit une instance à partir d'un dictionnaire de revendications.
     *
     * @param claims Le dictionnaire contenant les noms et valeurs des revendications.
     */
    public StandardClaims(final Map<String, Object> claims) {
        this.claims = claims != null ? Collections.unmodifiableMap(claims) : Collections.emptyMap();
    }

    /**
     * Retourne l'identifiant du sujet.
     *
     * @return L'identifiant unique de l'utilisateur.
     */
    public Optional<String> getSub() {
        return getClaim("sub");
    }

    /**
     * Retourne le nom complet de l'utilisateur.
     *
     * @return Le nom complet.
     */
    public Optional<String> getName() {
        return getClaim("name");
    }

    /**
     * Retourne le prénom de l'utilisateur.
     *
     * @return Le prénom.
     */
    public Optional<String> getGivenName() {
        return getClaim("given_name");
    }

    /**
     * Retourne le nom de famille de l'utilisateur.
     *
     * @return Le nom de famille.
     */
    public Optional<String> getFamilyName() {
        return getClaim("family_name");
    }

    /**
     * Retourne le deuxième prénom de l'utilisateur.
     *
     * @return Le deuxième prénom.
     */
    public Optional<String> getMiddleName() {
        return getClaim("middle_name");
    }

    /**
     * Retourne le pseudonyme de l'utilisateur.
     *
     * @return Le pseudonyme.
     */
    public Optional<String> getNickname() {
        return getClaim("nickname");
    }

    /**
     * Retourne le nom d'utilisateur préféré.
     *
     * @return Le nom d'utilisateur.
     */
    public Optional<String> getPreferredUsername() {
        return getClaim("preferred_username");
    }

    /**
     * Retourne l'URL de la page de profil.
     *
     * @return L'URL du profil.
     */
    public Optional<String> getProfile() {
        return getClaim("profile");
    }

    /**
     * Retourne l'URL de la photo de profil.
     *
     * @return L'URL de l'image.
     */
    public Optional<String> getPicture() {
        return getClaim("picture");
    }

    /**
     * Retourne l'URL du site web de l'utilisateur.
     *
     * @return L'URL du site web.
     */
    public Optional<String> getWebsite() {
        return getClaim("website");
    }

    /**
     * Retourne l'adresse e-mail de l'utilisateur.
     *
     * @return L'adresse e-mail.
     */
    public Optional<String> getEmail() {
        return getClaim("email");
    }

    /**
     * Indique si l'adresse e-mail a été vérifiée.
     *
     * @return true si l'e-mail est vérifié.
     */
    public Optional<Boolean> isEmailVerified() {
        return getClaim("email_verified");
    }

    /**
     * Retourne le sexe de l'utilisateur.
     *
     * @return Le genre.
     */
    public Optional<String> getGender() {
        return getClaim("gender");
    }

    /**
     * Retourne la date de naissance.
     *
     * @return La date de naissance au format ISO 8601:2004 YYYY-MM-DD.
     */
    public Optional<String> getBirthdate() {
        return getClaim("birthdate");
    }

    /**
     * Retourne l'identifiant du fuseau horaire.
     *
     * @return La zone info (ex: Europe/Paris).
     */
    public Optional<String> getZoneinfo() {
        return getClaim("zoneinfo");
    }

    /**
     * Retourne la locale de l'utilisateur.
     *
     * @return La locale (ex: fr-FR).
     */
    public Optional<String> getLocale() {
        return getClaim("locale");
    }

    /**
     * Retourne le numéro de téléphone.
     *
     * @return Le numéro de téléphone au format E.164.
     */
    public Optional<String> getPhoneNumber() {
        return getClaim("phone_number");
    }

    /**
     * Indique si le numéro de téléphone a été vérifié.
     *
     * @return true si le numéro est vérifié.
     */
    public Optional<Boolean> isPhoneNumberVerified() {
        return getClaim("phone_number_verified");
    }

    /**
     * Retourne l'adresse postale de l'utilisateur.
     *
     * @return L'objet adresse.
     * @see <a href="http://openid.net/specs/openid-connect-core-1_0.html#AddressClaim">Section 5.1.1
     * (Address Claim)</a>
     */
    public Optional<Object> getAddress() {
        return getClaim("address");
    }

    /**
     * Retourne la date de dernière mise à jour des informations.
     *
     * @return Le timestamp de mise à jour.
     */
    public Optional<Long> getUpdatedAt() {
        return getClaim("updated_at");
    }

    @SuppressWarnings("unchecked")
    private <T> Optional<T> getClaim(final String key) {
        return Optional.ofNullable((T) claims.get(key));
    }

    /**
     * Retourne toutes les revendications brutes.
     *
     * @return Le dictionnaire de toutes les revendications.
     */
    public Map<String, Object> getAllClaims() {
        return claims;
    }
}
