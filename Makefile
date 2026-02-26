# Makefile pour ScribeJava v9.1+ "Enterprise Edition"
# Automatisation, Observabilité et Certification multi-JDK

.PHONY: help build test test-parallel lint pitest install doc release release-local clean format certify ci sync

# --- Variables ---
VERSION_CURRENT := $(shell grep -m 1 "<version>" pom.xml | sed 's/[^0-9.]//g' | sed 's/-SNAPSHOT//')

help: ## Affiche cette aide
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

# --- Développement ---

build: ## Compile tous les modules (Zero-Dependency)
	mvn clean compile -Dmaven.javadoc.skip=true

test: ## Lance les tests unitaires (JUnit 5 + AssertJ)
	mvn test -Dmaven.javadoc.skip=true

test-parallel: ## Lance les tests en parallèle (1 thread par coeur)
	mvn test -T 1C -Dmaven.javadoc.skip=true

lint: ## Analyse statique (Checkstyle, PMD, Spotless, License)
	mvn checkstyle:check pmd:check spotless:check license:check

format: ## Formate le code (Google Style) et répare les headers de licence
	mvn spotless:apply license:format

certify: format ## Formate et lance la certification multi-JDK locale (Nécessite Docker)
	@echo "\033[32m🧪 Lancement de la certification ScribeJava...\033[0m"
	./ci-local.sh

ci: ## Alias pour certify
	@$(MAKE) certify

pitest: ## Mutation Testing sur les modules critiques (Core & OIDC)
	mvn pitest:mutationCoverage -pl scribejava-core,scribejava-oidc

install: ## Installe les JARs dans le repo local .m2
	mvn clean install -DskipTests -Dmaven.javadoc.skip=true

doc: ## Génère la Javadoc agrégée (Premium DX)
	mvn javadoc:aggregate -Dmaven.test.skip=true

# --- Release (Automatisation du cycle de vie) ---

release: ## Déclenche la release automatisée sur GitHub (Nécessite GitHub CLI)
	@echo "\033[32m🚀 Déclenchement du cycle de release sur GitHub Actions...\033[0m"
	@gh workflow run direct-release.yml
	@echo "\033[33m⏳ Workflow lancé ! Suivez la progression avec : gh run watch\033[0m"

release-local: ## Effectue la release localement (Sans 'gh', nécessite npm: conventional-changelog, conventional-recommended-bump)
	@echo "\033[32m🔍 Calcul de la prochaine version...\033[0m"
	$(eval BUMP_TYPE=$(shell npx conventional-recommended-bump -p angular))
	$(eval NEW_VER=$(shell npx semver $(VERSION_CURRENT) -i $(BUMP_TYPE)))
	@echo "Version calculée : $(NEW_VER) (Type: $(BUMP_TYPE))"
	@mvn versions:set -DnewVersion=$(NEW_VER) -DgenerateBackupPoms=false
	@mvn versions:commit
	@echo "\033[32m📝 Mise à jour du CHANGELOG.md...\033[0m"
	@npx conventional-changelog -p angular -i CHANGELOG.md -s -r 0
	@git add .
	@git commit -m "chore(release): $(NEW_VER)"
	@git tag -a v$(NEW_VER) -m "Release v$(NEW_VER)"
	@echo "\033[32m🚀 Publication des tags et de master...\033[0m"
	@git push origin master --tags
	@echo "\033[33m✅ Release v$(NEW_VER) terminée localement.\033[0m"

sync: ## Récupère les derniers changements de release depuis GitHub
	git pull --rebase origin master
	git fetch --tags

clean: ## Nettoie les dossiers target et les logs de CI
	mvn clean
	rm -rf target/ ci-logs/
