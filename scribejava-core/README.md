# ⚙️ ScribeJava Core

Le cœur du moteur OAuth. Ce module contient toute la logique de construction de requêtes, de signature et de gestion des flux (Grants).

---

## 🏗️ Architecture Extensible

ScribeJava v9 est conçu pour être étendu sans modifier le code source original.

### Ajouter un nouveau type de Grant (Pattern Strategy)
Si vous avez un flux OAuth propriétaire, étendez `OAuth20Grant` :

```java
public class MyCustomGrant extends OAuth20Grant {
    @Override
    public void addParameters(OAuthRequest request) {
        request.addParameter(OAuthConstants.GRANT_TYPE, "my_custom_type");
        request.addParameter("extra_param", "value");
    }
}
```

### Injecter un Handler personnalisé
Vous pouvez intercepter les comportements du service en fournissant des `Handlers` spécifiques lors de la construction :

```java
OAuth20Service service = new ServiceBuilder(clientId)
    .apiSecret(secret)
    .build(GitHubApi.instance());

// Utilisation d'un Handler pour la révocation
service.revokeToken(myToken, TokenTypeHint.ACCESS_TOKEN); 
```

---

## 🛰️ Clients HTTP supportés
Par défaut, le module `core` utilise le **JDK natif** (`java.net.HttpURLConnection`). 
Pour des besoins de haute performance ou asynchrones, utilisez l'un des adaptateurs officiels :
*   [OkHttp](../scribejava-httpclient-okhttp)
*   [Armeria](../scribejava-httpclient-armeria)

---
[🏠 Accueil](../README.md) | [🔌 APIs](../scribejava-apis/README.md) | [🔐 OIDC](../scribejava-oidc/README.md) | [🛡️ Sécurité](../ADVANCED_SECURITY.md)
