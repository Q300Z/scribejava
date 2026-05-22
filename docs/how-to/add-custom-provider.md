# 🔌 Comment ajouter un nouveau fournisseur (API Custom Provider)

ScribeJava est conçue pour être extrêmement modulaire et facile à étendre. Si vous devez intégrer un serveur d'autorisation ou un fournisseur d'identité OAuth 2.0 ou 1.0a qui n'est pas encore présent dans la bibliothèque, vous pouvez facilement créer votre propre configuration d'API.

---

## 🎯 1. Pourquoi implémenter un fournisseur personnalisé ?

L'implémentation d'un fournisseur d'API personnalisé (`DefaultApi20` ou `DefaultApi10a`) présente plusieurs intérêts majeurs :

1. **Intégration de Serveurs Privés ou Internes** : Si votre entreprise utilise son propre serveur d'authentification (ex. Keycloak, Authentik, Shibboleth, Ping Identity ou un serveur OAuth2 "maison"), ces configurations privées n'ont pas leur place dans la bibliothèque publique ScribeJava. Créer un fournisseur personnalisé vous permet de les intégrer proprement dans votre codebase.
2. **Gestion des Spécificités Non Standards** : De nombreux serveurs OAuth2 ne respectent pas strictement la RFC 6749 (ex. ils retournent des formats de jetons différents, exigent des paramètres de requête spécifiques ou utilisent des en-têtes d'autorisation non conventionnels). Surcharger les méthodes de `DefaultApi20` permet d'adapter ScribeJava à ces singularités sans toucher au moteur interne de la bibliothèque.
3. **Indépendance vis-à-vis des Mises à Jour de ScribeJava** : Si un nouveau service tiers apparaît sur le marché ou qu'une API existante change ses points de terminaison, vous n'avez pas besoin d'attendre une nouvelle version de ScribeJava ou de soumettre une Pull Request. Vous pouvez ajuster ou créer votre propre fournisseur d'API en quelques secondes directement dans votre projet.
4. **Zéro Réflexion & Performance Maximale** : Plutôt que de configurer dynamiquement des URL via des fichiers de configuration complexes à analyser, l'écriture d'une classe d'API fournit une configuration typée, compilée, immuable et performante.

---

## 🏗️ 2. L'architecture de base

Pour ajouter un fournisseur OAuth 2.0, il vous suffit de créer une classe qui hérite de **`com.github.scribejava.core.builder.api.DefaultApi20`**.

Cette classe abstraite nécessite l'implémentation de deux méthodes fondamentales :
- **`getAccessTokenEndpoint()`** : L'URL du point de terminaison pour échanger le code d'autorisation (ou les autres concessions) contre un jeton d'accès (Access Token).
- **`getAuthorizationBaseUrl()`** : L'URL de base vers laquelle rediriger l'utilisateur pour qu'il s'authentifie et accorde les autorisations.

### Exemple de classe d'API (avec le pattern Singleton thread-safe) :

```java
package com.github.scribejava.apis;

import com.github.scribejava.core.builder.api.DefaultApi20;

public class MonServiceApi extends DefaultApi20 {

    // Constructeur protégé pour empêcher l'instanciation directe
    protected MonServiceApi() {}

    // Holder interne statique pour un chargement immuable et thread-safe
    private static class InstanceHolder {
        private static final MonServiceApi INSTANCE = new MonServiceApi();
    }

    // Point d'accès unique à l'instance immuable
    public static MonServiceApi instance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public String getAccessTokenEndpoint() {
        return "https://api.monservice.com/oauth/token";
    }

    @Override
    public String getAuthorizationBaseUrl() {
        return "https://api.monservice.com/oauth/authorize";
    }
}
```

---

## ⚙️ 3. Personnalisations avancées (Facultatif)

Si votre fournisseur d'identité exige des comportements particuliers, vous pouvez surcharger les méthodes suivantes de `DefaultApi20` :

### A. Format de réponse du jeton non standard
Par défaut, ScribeJava s'attend à recevoir le jeton sous format JSON standard. Si le serveur retourne le jeton dans un autre format (comme du texte brut ou du format QueryString `access_token=xxx&expires_in=yyy`) :
```java
import com.github.scribejava.core.extractors.TokenExtractor;
import com.github.scribejava.core.extractors.OAuth2AccessTokenExtractor;
import com.github.scribejava.core.model.OAuth2AccessToken;

@Override
public TokenExtractor<OAuth2AccessToken> getAccessTokenExtractor() {
    // Utilise l'extracteur clé-valeur QueryString au lieu de JSON
    return OAuth2AccessTokenExtractor.instance();
}
```

### B. Authentification du client spécifique
Par défaut, ScribeJava envoie le `client_id` et le `client_secret` via l'en-tête HTTP `Authorization: Basic ...` (Basic Auth). Si votre fournisseur exige qu'ils soient envoyés dans le corps de la requête POST (RequestBody) :
```java
import com.github.scribejava.core.oauth2.clientauthentication.ClientAuthentication;
import com.github.scribejava.core.oauth2.clientauthentication.RequestBodyAuthenticationScheme;

@Override
public ClientAuthentication getClientAuthentication() {
    return RequestBodyAuthenticationScheme.instance();
}
```

### C. Signature des requêtes de ressources (Bearer Signature)
Par défaut, le jeton d'accès est transmis dans les en-têtes de requêtes de ressources via `Authorization: Bearer <token>`. Si le serveur exige que le jeton soit transmis comme paramètre d'URL (Query parameter) :
```java
import com.github.scribejava.core.oauth2.bearersignature.BearerSignature;
import com.github.scribejava.core.oauth2.bearersignature.BearerSignatureURIQueryParameter;

@Override
public BearerSignature getBearerSignature() {
    return BearerSignatureURIQueryParameter.instance();
}
```

---

## 🚀 4. Utilisation et Intégration Complète (Le Flux OAuth2)

Voici comment instancier votre fournisseur d'API personnalisé et dérouler un flux complet d'obtention de jeton et de signature de requête :

```java
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.AuthorizationCodeGrant;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        // 1. Initialisation du service avec votre API personnalisée
        final OAuth20Service service = new ServiceBuilder("mon-client-id")
                .apiSecret("mon-client-secret")
                .callback("http://localhost:8080/callback")
                .defaultScope("profile")
                .build(MonServiceApi.instance()); // <-- Utilisation ici

        // 2. Génération de l'URL d'autorisation
        final String authorizationUrl = service.getAuthorizationUrl();
        System.out.println("1. Connectez-vous via cette URL : " + authorizationUrl);

        // 3. Récupération du code d'autorisation (ex. via console)
        System.out.print("Entrez le code d'autorisation reçu : ");
        final Scanner in = new Scanner(System.in, "UTF-8");
        final String code = in.nextLine();

        // 4. Échange du code d'autorisation contre l'Access Token
        System.out.println("Échange du code d'autorisation...");
        final OAuth2AccessToken accessToken = service.getAccessToken(new AuthorizationCodeGrant(code));
        System.out.println("Jeton reçu ! Access Token : " + accessToken.getAccessToken());

        // 5. Signature et exécution d'une requête HTTP vers l'API protégée
        final OAuthRequest request = new OAuthRequest(Verb.GET, "https://api.monservice.com/v1/profile");
        service.signRequest(accessToken, request); // <-- Signature automatique

        try (Response response = service.execute(request)) {
            System.out.println("Code HTTP de réponse : " + response.getCode());
            System.out.println("Corps de la réponse : " + response.getBody());
        }
    }
}
```

---

## 📝 5. Cas d'OAuth 1.0a

Si vous devez connecter un service utilisant le protocole **OAuth 1.0a** (plus ancien), la démarche est identique mais vous devez hériter de **`com.github.scribejava.core.builder.api.DefaultApi10a`** et implémenter trois points d'accès :
- `getRequestTokenEndpoint()`
- `getAccessTokenEndpoint()`
- `getAuthorizationUrl(OAuth1RequestToken requestToken)`

> [!TIP]
> Pour plus d'exemples d'implémentations réelles, vous pouvez explorer le répertoire source [scribejava-apis](https://github.com/Q300Z/scribejava/tree/master/scribejava-apis/src/main/java/com/github/scribejava/apis) qui regorge de configurations prêtes à l'emploi pour plus de 50 fournisseurs d'identité mondiaux (Google, LinkedIn, Twitter/X, GitHub, etc.).
