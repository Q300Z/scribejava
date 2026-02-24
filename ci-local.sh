#!/bin/bash

# Détection de l'utilisateur courant pour les permissions Docker
export USER_ID=$(id -u)
export GID=$(id -g)

# Couleurs
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Dossier pour les logs
LOG_DIR="ci-logs"

echo -e "${BLUE}🧹 Nettoyage des dossiers de sortie et des dossiers target...${NC}"
rm -rf docs-output/* ci-logs/*
# On essaie de supprimer mais on ne bloque pas si Docker a encore des verrous
find . -name "target" -type d -exec rm -rf {} + 2>/dev/null || true
# On s'assure que les dossiers racines existent pour Maven
mkdir -p "$LOG_DIR"
mkdir -p "docs-output"
mkdir -p target scribejava-core/target scribejava-apis/target \
    scribejava-httpclient-armeria/target scribejava-httpclient-okhttp/target \
    scribejava-oauth1/target scribejava-oidc/target \
    scribejava-integration-helpers/target

echo -e "${BLUE}🚀 Préparation : Build des images et Installation locale (JDK 17)...${NC}"
# On force le build avec les bons arguments de permission
docker compose -f docker-compose.ci.yml build --build-arg USER_ID=$USER_ID --build-arg GROUP_ID=$GID > /dev/null 2>&1

if ! docker compose -f docker-compose.ci.yml run --rm setup > "$LOG_DIR/setup.log" 2>&1; then
    echo -e "  ${RED}❌ setup : FAILED (voir $LOG_DIR/setup.log)${NC}"
    exit 1
fi
# On force la synchronisation disque pour éviter les corruptions de cache en lecture parallèle
sync

echo -e "${BLUE}🧐 Étape 1 : Lintage et Analyse statique (JDK 17)...${NC}"
chmod -R 777 target/ 2>/dev/null || true
if docker compose -f docker-compose.ci.yml run --rm lint > "$LOG_DIR/lint.log" 2>&1; then
    echo -e "  ${GREEN}✅ lint : SUCCESS${NC}"
else
    echo -e "  ${RED}❌ lint : FAILED (voir $LOG_DIR/lint.log)${NC}"
    exit 1
fi

echo -e "${BLUE}📚 Étape 2 : Génération de la documentation (Local JDK 11)...${NC}"
export JAVA_HOME=/usr/lib/jvm/java-1.11.0-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
# Pré-création des dossiers pour éviter les erreurs de permission
mkdir -p scribejava-core/target/classes
if mvn javadoc:aggregate -Dmaven.javadoc.skip=false -Dcheckstyle.skip -Dpmd.skip -Dspotless.check.skip > "$LOG_DIR/docs.log" 2>&1; then
    echo -e "  ${GREEN}✅ docs : SUCCESS${NC}"
    echo -e "  📂 Documentation disponible dans target/site/apidocs"
    cp -r target/site/apidocs/. docs-output/
else
    echo -e "  ${RED}❌ docs : FAILED (voir $LOG_DIR/docs.log)${NC}"
fi

echo -e "${BLUE}🧪 Étape 3 : Tests multi-JDK en parallèle...${NC}"
TEST_SERVICES=("test-jdk8" "test-jdk11" "test-jdk17" "test-jdk21" "test-jdk25")

declare -A PIDS
for SERVICE in "${TEST_SERVICES[@]}"; do
    echo "  ➡️  Démarrage de $SERVICE..."
    docker compose -f docker-compose.ci.yml run --rm "$SERVICE" > "$LOG_DIR/$SERVICE.log" 2>&1 &
    PIDS[$SERVICE]=$!
done

echo -e "${BLUE}⏳ Attente de la fin des tests...${NC}"
FAILED=0
for SERVICE in "${TEST_SERVICES[@]}"; do
    wait "${PIDS[$SERVICE]}"
    if [ $? -eq 0 ]; then
        echo -e "  ${GREEN}✅ $SERVICE : SUCCESS${NC}"
    else
        echo -e "  ${RED}❌ $SERVICE : FAILED (voir $LOG_DIR/$SERVICE.log)${NC}"
        FAILED=1
    fi
done

if [ $FAILED -eq 0 ]; then
    echo -e "\n${GREEN}🎉 Félicitations ! Toute la matrice CI est passée avec succès.${NC}"
    exit 0
else
    echo -e "\n${RED}⚠️  Certains tests ont échoué. Vérifiez les logs dans $LOG_DIR/${NC}"
    exit 1
fi
