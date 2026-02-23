# 🚀 Client HTTP OkHttp pour ScribeJava

Ce module permet d'utiliser **OkHttp 4** comme moteur de transport pour ScribeJava. Il est recommandé pour les
applications Android ou les environnements nécessitant une gestion fine des pools de connexions.

## 🛠️ Configuration

Pour utiliser OkHttp, passez une instance de `OkHttpHttpClientConfig` au `ServiceBuilder` :

```java
OkHttpHttpClientConfig config = OkHttpHttpClientConfig.defaultConfig();
config.setConnectTimeout(5000);

OAuth20Service service = new ServiceBuilder(clientId)
    .apiSecret(secret)
    .httpClientConfig(config) // Injection de la config OkHttp
    .build(GitHubApi.instance());
```

## ⚡ Mode Asynchrone

OkHttp supporte nativement les appels non-bloquants :

```java
CompletableFuture<OAuth2AccessToken> future = service.getAccessTokenAsync(grant);
future.thenAccept(token -> {
    // Traitement du jeton
});
```

## 📎 Support Multipart

Le client OkHttp supporte désormais l'envoi de fichiers et de charges utiles complexes via le format
`multipart/form-data`.

```java
MultipartPayload payload = new MultipartPayload();
payload.addBodyPart(new ByteArrayBodyPartPayload(fileBytes, "image/jpeg"));
payload.addBodyPart(new ByteArrayBodyPartPayload("metadata".getBytes(), "application/json"));

OAuthRequest request = new OAuthRequest(Verb.POST, url);
request.setPayload(payload);
service.execute(request);
```

---
[🏠 Accueil](../README.md) | [🔌 APIs](../scribejava-apis/README.md) | [🔐 OIDC](../scribejava-oidc/README.md) | [🛡️ Sécurité](../ADVANCED_SECURITY.md)
