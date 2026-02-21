# Guide d'Extensibilité

Comment adapter ScribeJava pour des APIs non standards.

## 📥 Extracteur de Token Personnalisé

Si une API retourne un format non standard (ex: XML ou texte brut au lieu de JSON), vous pouvez créer votre propre `TokenExtractor`.

### 1. Implémenter l'interface
```java
public class MonExtracteur implements TokenExtractor<OAuth2AccessToken> {
    @Override
    public OAuth2AccessToken extract(Response response) throws IOException {
        String body = response.getBody();
        // Parser votre format ici
        return new OAuth2AccessToken(tokenParse);
    }
}
```

### 2. Configurer votre classe API
```java
public class MonApi extends DefaultApi20 {
    @Override
    public TokenExtractor<OAuth2AccessToken> getAccessTokenExtractor() {
        return new MonExtracteur();
    }
}
```

## 🌐 Client HTTP Personnalisé

Si vous devez utiliser une librairie HTTP d'entreprise spécifique ou un client mocké pour les tests :
1. Implémentez `com.github.scribejava.core.httpclient.HttpClient`.
2. Passez-le au `ServiceBuilder` :
```java
OAuthService service = new ServiceBuilder(apiKey)
    .httpClient(new MonClientInterne())
    .build(api);
```
