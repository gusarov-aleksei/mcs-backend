set -e

echo "Build from the scratch"

echo "Building catalog..."
mvn -f ./catalog-service/pom.xml clean install

echo "Building cart..."
mvn -f ./cart-service/pom.xml clean install

echo
echo "Building images..."
docker-compose build --no-cache



