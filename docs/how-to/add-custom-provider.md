# 🔌 Comment ajouter un nouveau fournisseur (API Custom Provider)

ScribeJava est conçue pour être extrêmement modulaire et facile à étendre. Si vous devez intégrer un serveur d'autorisation ou un fournisseur d'identité OAuth 2.0 ou 1.0a qui n'est pas encore présent dans le module `scribejava-apis`, vous pouvez facilement créer votre propre configuration d'API.

---

## 🏗️ 1. L'architecture de base (`DefaultApi20`)

Pour ajouter un fournisseur OAuth 2.0, il vous suffit de créer une classe qui hérite de **`com.github.scribejava.core.builder.api.DefaultApi20`**.

Cette classe abstraite nécessite l'implémentation de deux méthodes fondamentales :
- **`getAccessTokenEndpoint()`** : L'URL du point de terminaison pour échanger le code d'autorisation (ou les autres concessions) contre un jeton d'accès (Access Token).
- **`getAuthorizationBaseUrl()`** : L'URL de base vers laquelle rediriger l'utilisateur pour qu'il s'authentifie et accorde les autorisations.

---

## 🛠️ 2. Exemple pas à pas d'une API Personnalisée

Voici l'implémentation type recommandée, utilisant le pattern **Singleton** (filtre de concurrence thread-safe) :

```java
package com.github.scribejava.apis;

import com.github.scribejava.core.builder.api.DefaultApi20;

public class MonServiceApi extends DefaultApi20 {

    // 1. Constructeur protégé pour empêcher l'instanciation directe
    protected MonServiceApi() {}

    // 2. Holder interne statique pour un chargement paresseux et thread-safe
    private static class InstanceHolder {
        private static final MonServiceApi INSTANCE = new MonServiceApi();
    }

    // 3. Point d'accès unique à l'instance
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

Si votre fournisseur d'identité ne suit pas scrupuleusement la spécification standard ou requiert des en-têtes/formats particuliers, vous pouvez surcharger plusieurs méthodes de la classe `DefaultApi20` :

### A. Format de réponse du jeton non standard
Par défaut, ScribeJava utilise le parseur JSON standard (`OAuth2AccessTokenJsonExtractor`). Si votre fournisseur retourne le jeton dans un autre format (comme du texte brut ou des paires clé-valeur de type formulaire) :
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

### B. Méthode d'authentification du client spécifique
Par défaut, le `client_id` et le `client_secret` sont envoyés via l'en-tête HTTP `Authorization` (Basic Auth). Si votre fournisseur exige qu'ils soient envoyés dans le corps du POST (RequestBody) :
```java
import com.github.scribejava.core.oauth2.clientauthentication.ClientAuthentication;
import com.github.scribejava.core.oauth2.clientauthentication.RequestBodyAuthenticationScheme;

@Override
public ClientAuthentication getClientAuthentication() {
    return RequestBodyAuthenticationScheme.instance();
}
```

### C. Signature des requêtes de ressources (Bearer Signature)
Par défaut, le jeton d'accès est transmis dans les en-têtes de requêtes via `Authorization: Bearer <token>`. Si le serveur exige que le jeton soit transmis comme paramètre d'URL (Query string) :
```java
import com.github.scribejava.core.oauth2.bearersignature.BearerSignature;
import com.github.scribejava.core.oauth2.bearersignature.BearerSignatureURIQueryParameter;

@Override
public BearerSignature getBearerSignature() {
    return BearerSignatureURIQueryParameter.instance();
}
```

---

## 🚀 4. Utilisation de votre API personnalisée

Une fois votre classe d'API définie, vous pouvez l'utiliser directement dans votre `ServiceBuilder` habituel :

```java
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;

OAuth20Service service = new ServiceBuilder("votre-client-id")
    .apiSecret("votre-client-secret")
    .callback("https://votre-app.com/callback")
    .defaultScope("read_user")
    .build(MonServiceApi.instance()); // Passage de votre instance personnalisée
```

---

## 📝 5. Cas d'OAuth 1.0a

Si vous devez connecter un service utilisant le protocole **OAuth 1.0a** (plus ancien), la démarche est identique mais vous devez hériter de **`com.github.scribejava.core.builder.api.DefaultApi10a`** et implémenter trois points d'accès :
- `getRequestTokenEndpoint()`
- `getAccessTokenEndpoint()`
- `getAuthorizationUrl(OAuth1RequestToken requestToken)`

> [!TIP]
> Pour plus d'exemples d'implémentations réelles, vous pouvez explorer le répertoire source [scribejava-apis](https://github.com/Q300Z/scribejava/tree/master/scribejava-apis/src/main/java/com/github/scribejava/apis) qui regorge de configurations prêtes à l'emploi pour plus de 50 fournisseurs d'identité mondiaux (Google, LinkedIn, Twitter/X, GitHub, etc.).
