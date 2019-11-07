set -e

echo "re-Build customer-service..."

mvn clean install
./run.sh
