set -e

modules=(cart-service catalog-service customer-service order-service);
module=$1
echo "Build one module: $module"
echo "Existing modules: ${modules[*]}"

if [[ ${modules[*]} =~ $module ]]
then
  echo "Building..."
  mvn -f ./"$module"/pom.xml clean install
  docker-compose build "$module"
else
  echo "Module name is unknown."
  echo "Please put one of following: cart-service, catalog-service, customer-service, order-service"
fi