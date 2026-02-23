# 🛰️ Client HTTP Armeria pour ScribeJava

Ce module intègre **Armeria**, le client HTTP réactif et haute performance, comme moteur de transport. C'est le choix
idéal pour les microservices modernes et les architectures non-bloquantes.

## 🛠️ Configuration

```java
ArmeriaHttpClientConfig config = ArmeriaHttpClientConfig.defaultConfig();

OAuth20Service service = new ServiceBuilder(clientId)
    .apiSecret(secret)
    .httpClientConfig(config) // Injection de la config Armeria
    .build(GoogleApi20.instance());
```

## ⚡ Performance Réactive

Armeria est optimisé pour le débit massif et utilise Netty en coulisses. Toutes les méthodes `*Async` de ScribeJava
exploitent pleinement cette puissance sans blocage de thread.

---
[🏠 Accueil](../README.md) | [🔌 APIs](../scribejava-apis/README.md) | [🔐 OIDC](../scribejava-oidc/README.md) | [🛡️ Sécurité](../ADVANCED_SECURITY.md)
