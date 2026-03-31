# 📓 Journal des modifications (Changelog)

# [9.3.0](https://github.com/Q300Z/scribejava/compare/v9.2.7...v9.3.0) (2026-03-31)


### Bug Fixes

* **ci:** align pnpm versions with package.json ([67f1f3b](https://github.com/Q300Z/scribejava/commit/67f1f3b4555f72a32d763158401c9944b85301eb))
* **ci:** simplify workflow and stabilize OidcAutoConfigTest ([15f8344](https://github.com/Q300Z/scribejava/commit/15f83441354da501f7c6ec20c3c9bd674eb7de2a))
* **ci:** use download-artifact instead of upload-artifact in test job ([02938a8](https://github.com/Q300Z/scribejava/commit/02938a8effaf91eca44190c0d21d9aa53d3f40ae))
* **oidc:** stabilize OidcAutoConfigTest and cleanup unstable tests ([48c9136](https://github.com/Q300Z/scribejava/commit/48c9136a0386591df7b45c6d223915e7d6d8a113))


### Features

* **ci:** automate release assets and doc publication on tags ([1d3b33a](https://github.com/Q300Z/scribejava/commit/1d3b33a6312b6c0b59c5a19e6c25883fd71ba190))
* **ci:** implement release candidate artifacts and enhanced multi-JDK matrix ([406a118](https://github.com/Q300Z/scribejava/commit/406a118e0896d28b53265fbd367a439785aa370f))
* **oauth:** add usePushedAuthorizationRequests setter to AuthorizationUrlBuilder ([26f08f7](https://github.com/Q300Z/scribejava/commit/26f08f7244c2fe77638dc45dd173f9c23aa2d5e8))
* **oidc:** add support for end_session_endpoint in OidcProviderMetadata ([f305646](https://github.com/Q300Z/scribejava/commit/f305646792adecfba0071af6bb4483762d61e9c9))

## [9.2.6](https://github.com/Q300Z/scribejava/compare/v1.1.1...v9.2.6) (2026-03-02)

# [1.1.0](https://github.com/Q300Z/scribejava/compare/v9.2.4...v1.1.0) (2026-03-02)


### Bug Fixes

* **core:** improve OIDC support and JSON performance ([aef74ff](https://github.com/Q300Z/scribejava/commit/aef74ffc391bb4a58c0c2a728aa9128a541fa45e))
* **core:** prevent resource leaks and improve request traceability ([ab02e87](https://github.com/Q300Z/scribejava/commit/ab02e87dcca4e73887e0d6ed630a4ca5f4bf16c9))
* **helpers:** stabilize disk cache and cleanup dependencies ([616b838](https://github.com/Q300Z/scribejava/commit/616b8382c4808d6105a642f7c1ee2a5488013edc))


### Features

* **helpers:** add disk cache and improved serialization ([85af355](https://github.com/Q300Z/scribejava/commit/85af355684abeb9323a3681ebd6cddf96fd3cef3))
* **oidc:** add typed OidcServiceBuilder to eliminate manual casts ([7ce3d33](https://github.com/Q300Z/scribejava/commit/7ce3d330ce79d10d83a84790224b1aa736ea0064))
* **oidc:** apply enterprise formatting to metadata ([620dfd6](https://github.com/Q300Z/scribejava/commit/620dfd6ffe04a2e6e43d53249ca902ff262ff3f6))
* **oidc:** enhance DX and audit capabilities ([c3022f6](https://github.com/Q300Z/scribejava/commit/c3022f68b09f1af71d1db67ce4b5459652bab070))

# [](https://github.com/Q300Z/scribejava/compare/v9.2.3...v) (2026-02-27)


### Bug Fixes

* resolve compilation and linting issues in quickstart examples ([f0d3088](https://github.com/Q300Z/scribejava/commit/f0d3088a27ff9e2e2769f3b09392493860317cde))



## [9.2.3](https://github.com/Q300Z/scribejava/compare/v9.2.1...v9.2.3) (2026-02-26)


### Bug Fixes

* cleanup release process (unified JARs only and incremental changelog) ([d1cd4f2](https://github.com/Q300Z/scribejava/commit/d1cd4f2b6a96dbb286b7e67455d86a7af4584a26))
* include JAR artifacts in the direct release workflow ([8eb80e4](https://github.com/Q300Z/scribejava/commit/8eb80e4fc69d90ee40a7d40741b3fdda9252e969))



## [9.2.1](https://github.com/Q300Z/scribejava/compare/v9.2.0...v9.2.1) (2026-02-26)


### Bug Fixes

* install missing angular preset in release workflow ([8f3a702](https://github.com/Q300Z/scribejava/commit/8f3a7020e8c6ea6e35aa4eafcdb8d17b8b903096))



# [9.2.0](https://github.com/Q300Z/scribejava/compare/v9.1.0...v9.2.0) (2026-02-26)


### Features

* trigger automated release cycle ([b65dd55](https://github.com/Q300Z/scribejava/commit/b65dd55fe29fddd03b1130a46d4351c59dec449f))



# [9.1.0](https://github.com/Q300Z/scribejava/compare/v9.0.0...v9.1.0) (2026-02-26)


### Bug Fixes

* **ci:** ensure internal dependencies are resolved before generating Javadoc ([bf341e6](https://github.com/Q300Z/scribejava/commit/bf341e6b4a15b9dbd12222e80e70c4bb481c3115))
* **ci:** resolve Spotless/JDK 21 compatibility and refactor CI jobs ([a628a4e](https://github.com/Q300Z/scribejava/commit/a628a4e63549b1043c090e21eeb64daf63e78a9a))
* corrections finales de compilation, lintage et formatage sur tous les modules ([fe041ca](https://github.com/Q300Z/scribejava/commit/fe041ca01ece6531190d1d8985ca0ce9431d5d4e))
* finaliser la compatibilité des exemples et nettoyage final ([c01fba9](https://github.com/Q300Z/scribejava/commit/c01fba9fd46fcdb4929f443ed2739aeffd51fec2))
* finaliser le lintage PMD, le formatage Spotless et nettoyage final ([3a9be92](https://github.com/Q300Z/scribejava/commit/3a9be92daa67882a4e9af19b2f7a487d82bf61b1))
* finaliser le lintage PMD, le formatage Spotless et nettoyage final ([2ecd2ee](https://github.com/Q300Z/scribejava/commit/2ecd2eee412c2875d4c385ea0e64df23e99c3904))
* **lint:** resolve PMD and Checkstyle violations in core and examples ([37a4cf0](https://github.com/Q300Z/scribejava/commit/37a4cf0b495f5b28eadf4b02a47e031f2ebeda32))
* **oidc:** resolve double slash in discovery URL and relax issuer check for Docker environments ([017fb9c](https://github.com/Q300Z/scribejava/commit/017fb9c813e8abf13524a0bde01352cc82088e09))
* réappliquer le formatage Spotless et lintage final ([d80f094](https://github.com/Q300Z/scribejava/commit/d80f0943ce6a07050413b72a67a40ba775a0134a))
* **test:** add missing JUnit Test import and resolve PMD unused import ([3d0a5a4](https://github.com/Q300Z/scribejava/commit/3d0a5a4e8dde996fcbba219d70fb5365c7d5901d))


### Features

* ajouter AuthorizedClientService et AuthFlowCoordinator pour une intégration simplifiée ([cb835f9](https://github.com/Q300Z/scribejava/commit/cb835f9ed0aa0f298b432213f9cc172967913887))
* ajouter le module scribejava-integration-helpers (Auto-refresh, Storage, CSRF) ([8842f37](https://github.com/Q300Z/scribejava/commit/8842f37a3df52ae83457591aa984afcdbe0c51a9))
* **api:** add OIDC support for Google, Microsoft, and GitHub ([6a9fe6b](https://github.com/Q300Z/scribejava/commit/6a9fe6b6621e1f08bcdee7e6388b1741018d06db))
* certification OIDC v9.1.0 avec éradication totale du JSON manuel ([f74c61e](https://github.com/Q300Z/scribejava/commit/f74c61e88829922502a061f58abca6002534a40d))
* finaliser l'abstraction (AuthorizedClientService) et l'observabilité (AuthEventListener) ([f3459f7](https://github.com/Q300Z/scribejava/commit/f3459f776fd0f90bf1165ec5447e9ea87d845b51))
* finaliser l'architecture d'intégration haut niveau (SOLID + TDD) ([66e99bd](https://github.com/Q300Z/scribejava/commit/66e99bd92f445df10ba6d6f389abf1de832c1730))
* finaliser la couche d'audit et le registre multi-tenant (100% test coverage) ([ab8e75c](https://github.com/Q300Z/scribejava/commit/ab8e75cf5ce291620cd895c4d2cea005d88336b0))
* finaliser la couche d'intégration entreprise (AuthSessionContext, OidcDiscoveryCache, 100% coverage) ([74394f8](https://github.com/Q300Z/scribejava/commit/74394f81f8013cdf6f8fc20a54f37ac6ba065803))
* **http:** implement multipart support in OkHttpHttpClient and fix maven build instability ([b96aa66](https://github.com/Q300Z/scribejava/commit/b96aa66b64080652832fe8654ddb73926e01e406))
* implémentation complète OIDC Enterprise (Nonce, JWT Validation, Fallback UserInfo) ([3fed897](https://github.com/Q300Z/scribejava/commit/3fed8975ea81bba94db051aea92b1b55b2ea5bd6))
* moteur JSON natif avancé (Builder, Wrapper, Unicode, Sécurité) ([4fba720](https://github.com/Q300Z/scribejava/commit/4fba720253cb7e629fe61f5ee314c366da03edba))
* moteur OIDC autonome (Phase 2 - Coeur et Compatibilité) ([31741bf](https://github.com/Q300Z/scribejava/commit/31741bf31e3ca5814e52c84789b5f32d8bbc496a))
* observabilité et robustesse Enterprise (Retry, Logger, RateLimit) ([1ec2a2d](https://github.com/Q300Z/scribejava/commit/1ec2a2de822c1e55f437b5861964146cf2f8dd87))
* **oidc:** add toggle for strict issuer validation in OidcDiscoveryService ([3d0b42d](https://github.com/Q300Z/scribejava/commit/3d0b42dcf7f5f5f223e3698abfcf187dab0a194b))
* **oidc:** améliorer la couverture de tests et adapter les services d'API ([f3a07b8](https://github.com/Q300Z/scribejava/commit/f3a07b82a7c5fd8d2d74ee575a7e3efb58c650b8))
* optimisations OIDC industrielles (Cache, Rotation, EC Keys) ([9138f2c](https://github.com/Q300Z/scribejava/commit/9138f2c40c0abdb9532dbd7ccfedf635e34c5ca1))
* premium Developer Experience (Auto-Config, Fluent Claims, cURL) ([37f6bd8](https://github.com/Q300Z/scribejava/commit/37f6bd8bca882c8e15af4159e036c3c350ca4a92))
* rendre le module OIDC autonome (Phase 1 : OidcNonce interne) ([1e98987](https://github.com/Q300Z/scribejava/commit/1e98987dbf07e3bbee63a616044982d7b31a74fd))
* rendre le module OIDC autonome (Phase 2 : Moteur JWT natif) ([cf6a236](https://github.com/Q300Z/scribejava/commit/cf6a2361ec9b8821dae0c2339011b17dcfc05fdb))
* **security:** implement JAR (RFC 9101) with JWE encryption, Key Rollover and PAR caching ([75b00cb](https://github.com/Q300Z/scribejava/commit/75b00cbd529c357eff099716bc5157b116e2fae2))



# [9.0.0](https://github.com/Q300Z/scribejava/compare/4bb6e49b61ee27bc8c9fde2c62823cbd4fc3249b...v9.0.0) (2026-02-21)


### Bug Fixes

* allow spaces in scope param in OAuth2Accesstoken response ([255a8e6](https://github.com/Q300Z/scribejava/commit/255a8e622a343b3b7401916af6cf92baa538bf16))
* **apis:** remove empty statement in FlickrApi ([38bb07f](https://github.com/Q300Z/scribejava/commit/38bb07f85cfd763d2100882348f0e96a259b78dc))
* **armeria:** remove unnecessary fully qualified name in tests ([ddf04ad](https://github.com/Q300Z/scribejava/commit/ddf04adba2bc155b3e9941d9a2984e0a8a1816e5))
* **core:** remove unused dpopProofCreator field to satisfy PMD ([1ac8388](https://github.com/Q300Z/scribejava/commit/1ac83888eb73f19b09194c27a4bab71fa4fca257))
* **http:** support request cancellation in OkHttp and Armeria clients ([1db12c7](https://github.com/Q300Z/scribejava/commit/1db12c7e9174f3d784f40910ffae5bf000525d26))
* OAuth20Service::refreshAccessToken should use RefreshTokenEndpoint, not AccessTokenEndpoint (thanks to https://github.com/vivin) ([2ec0701](https://github.com/Q300Z/scribejava/commit/2ec0701e6fff84c8ea9f0438aca956df12e2945d))


### Features

* added polar oauth2 api and example ([3a095e2](https://github.com/Q300Z/scribejava/commit/3a095e2471b6a24bb2e74a6b7bf95a63d98ce387))
* Cleanup scribejava-apis and remove non-compliant implementations ([19b5021](https://github.com/Q300Z/scribejava/commit/19b5021bb19a865928d5f3db757857d989392f58))
* **core:** add automatic endpoint discovery support to ServiceBuilder ([e99a2b1](https://github.com/Q300Z/scribejava/commit/e99a2b1f677c42f368c63cd902b89896b93145e0))
* **core:** add rich exception hierarchy for better error handling ([7197bce](https://github.com/Q300Z/scribejava/commit/7197bcef7a622c3088e82be0ddb37d41f6dd4995))
* **core:** decouple grant types using Strategy pattern ([309ff21](https://github.com/Q300Z/scribejava/commit/309ff21d28d0239c2d3a05666b6f1168755eff76))
* Final cleanup of obsolete and legacy APIs ([157fc55](https://github.com/Q300Z/scribejava/commit/157fc55d48f5a51591623d908f5eeb8636dff733))
* Finalize modernization architecture (OIDC, Interceptors, DPoP) ([e5ab2a9](https://github.com/Q300Z/scribejava/commit/e5ab2a97bb84dd04783726a8c13dd962b2ce85ee))
* Implement OpenID Connect (OIDC) support ([32693a7](https://github.com/Q300Z/scribejava/commit/32693a72873875f15ecb3df66bc21573464894a1))
* Introduce DPoP support ([7d448c9](https://github.com/Q300Z/scribejava/commit/7d448c948afb6b0dd6969c90ba9674cfdd0e2c05))
* Introduce Pushed Authorization Requests (PAR) ([9044dfe](https://github.com/Q300Z/scribejava/commit/9044dfe0c641613b613ce5970beddd72a2219fb5))
* **oidc:** enforce azp claim validation and add security edge cases ([c743e99](https://github.com/Q300Z/scribejava/commit/c743e9924673460ed9298e57545327895f7b8d09))
* **oidc:** enhance OIDC support with Discovery and Registration services ([288ad9d](https://github.com/Q300Z/scribejava/commit/288ad9d05e16432d207cd426fbf5c3333f0a7cc4))
* Refactor core to Java 8 style (Asynchronous and Encoding) ([7305d89](https://github.com/Q300Z/scribejava/commit/7305d897b6cb529acaa1fe6c67994fa89b582270))
* Refactor core to Java 8 style (Streams, Lambdas and Time) ([8fb46a5](https://github.com/Q300Z/scribejava/commit/8fb46a5c614b37f13139c16dae8e743071d4123e))
* Remove deprecated HTTP client modules and complete dependency cleanup ([9e708e3](https://github.com/Q300Z/scribejava/commit/9e708e33a653f18d1040edff9d6599ec4d5635b7))
* Setup Java 8 compatibility and initial cleanup ([0251fce](https://github.com/Q300Z/scribejava/commit/0251fce0deae62044752244a9f8fb7376e2217ee))
* Standardize PKCE by default ([3117b26](https://github.com/Q300Z/scribejava/commit/3117b260b373c859fb67eb6ca176a229faff7f23))
* Strict OAuth 1.0a and OAuth 2.0 separation ([3882102](https://github.com/Q300Z/scribejava/commit/3882102de9c199e95017b0741cf0815a41fc7f52))


### Reverts

* Revert "prepare v7.1.0" ([c55dd67](https://github.com/Q300Z/scribejava/commit/c55dd67d16205582e7a74e7394df3b821cc38375))
* Revert "[maven-release-plugin] prepare release scribejava-7.1.0" ([d908a3a](https://github.com/Q300Z/scribejava/commit/d908a3a8bf4fc3c60fd6dd7b03849ec30163d5ab))
* Revert "[maven-release-plugin] prepare for next development iteration" ([fc54cd7](https://github.com/Q300Z/scribejava/commit/fc54cd7d81e89a15bdee3cc822978300d3ddc60f))
* Revert "prepare v6.4.0" ([0bb4efd](https://github.com/Q300Z/scribejava/commit/0bb4efdd57580d5ae955ab32b97d3f7c8908b4d5))
* Revert "[maven-release-plugin] prepare release scribejava-6.4.0" ([be5199d](https://github.com/Q300Z/scribejava/commit/be5199d70585a570dd43319a8622c146c4d97a6a))
* Revert "[maven-release-plugin] prepare for next development iteration" ([c7715d8](https://github.com/Q300Z/scribejava/commit/c7715d8511f2fadb29b7f8174c0f0a635f79e008))
* Revert "[maven-release-plugin] prepare release scribejava-3.3.0" ([195e1eb](https://github.com/Q300Z/scribejava/commit/195e1eb1e2b38d933d8367a458a9473836f95093))
* Revert "[maven-release-plugin] prepare for next development iteration" ([feca5d4](https://github.com/Q300Z/scribejava/commit/feca5d4ef938b076dc23d0dabbc15ef4a454171f))
* Revert "[maven-release-plugin] prepare release scribejava-2.5.0" ([852bc7e](https://github.com/Q300Z/scribejava/commit/852bc7efed6907cb04de559b863fd5ac7b5d240e))
* Revert "[maven-release-plugin] prepare for next development iteration" ([826be08](https://github.com/Q300Z/scribejava/commit/826be08321647c3feced154871db38dd00d0c055))
* Revert "Removed the @Override annotation as it resulted in a compilation error." ([4bb6e49](https://github.com/Q300Z/scribejava/commit/4bb6e49b61ee27bc8c9fde2c62823cbd4fc3249b))
