set -e

echo "Cleaning..."
echo "Removing catalog artefacts..."
mvn -f ./catalog-service clean

echo "Removing cart artefacts..."
mvn -f ./cart-service clean

echo "Removing int-test artefacts..."
mvn -f ./int-test clean

echo "Removing images..."
docker-compose down --rmi all
