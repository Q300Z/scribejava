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

echo -e "${BLUE}🧹 Nettoyage des dossiers de sortie...${NC}"
rm -rf docs-output/* ci-logs/*
mkdir -p "$LOG_DIR"
mkdir -p "docs-output"

echo -e "${BLUE}🚀 Préparation : Build des images et Installation locale (JDK 17)...${NC}"
# On force le build et on ignore le code de retour car Docker Compose peut être capricieux sur les sorties
docker compose -f docker-compose.ci.yml build > /dev/null 2>&1

if ! docker compose -f docker-compose.ci.yml run --rm setup > "$LOG_DIR/setup.log" 2>&1; then
    echo -e "  ${RED}❌ setup : FAILED (voir $LOG_DIR/setup.log)${NC}"
    exit 1
fi

echo -e "${BLUE}🧐 Étape 1 : Lintage et Analyse statique (JDK 17)...${NC}"
if docker compose -f docker-compose.ci.yml run --rm lint > "$LOG_DIR/lint.log" 2>&1; then
    echo -e "  ${GREEN}✅ lint : SUCCESS${NC}"
else
    echo -e "  ${RED}❌ lint : FAILED (voir $LOG_DIR/lint.log)${NC}"
    exit 1
fi

echo -e "${BLUE}📚 Étape 2 : Génération de la documentation (JDK 17)...${NC}"
if docker compose -f docker-compose.ci.yml run --rm docs > "$LOG_DIR/docs.log" 2>&1; then
    echo -e "  ${GREEN}✅ docs : SUCCESS${NC}"
    echo -e "  📂 Extraction de la documentation..."
    docker run --rm -v scribejava_docs-data:/from -v "$(pwd)/docs-output:/to" eclipse-temurin:17-jdk-jammy cp -r /from/. /to/
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
