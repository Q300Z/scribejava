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

# --- Release (Automatisation du cycle de vie via release-it) ---

release: ## Lance le cycle de release (Bump, Changelog, Tag, Push) - Déclenche ensuite le CI de publication
	@echo "\033[32m🚀 Lancement du processus de release avec release-it...\033[0m"
	@release-it

sync: ## Récupère les derniers changements de release depuis GitHub
	git pull --rebase origin master
	git fetch --tags

clean: ## Nettoie les dossiers target et les logs de CI
	mvn clean
	rm -rf target/ ci-logs/
