set -e

modules=(cart-service catalog-service customer-service order-service);
module=$1
echo "Clean one module: $module"
echo "Existing modules: ${modules[*]}"

function remove_image_by_name() {
  image_id=$(docker images -f=reference="$1" -q)
  echo "Removing image:$1 id: $image_id"
  if [ -n "$image_id" ]
  then
    docker rmi "$image_id"
  fi
}

if [[ ${modules[*]} =~ $module ]]
then
  echo "Removing images of $module"
  docker-compose rm --stop --force "$module"
  remove_image_by_name "$module"
else
  echo "Module name is unknown."
  echo "Please put one of following: cart-service, catalog-service, customer-service, order-service"
fi