set -e

echo "Cleaning..."
echo "Removing catalog artefacts..."
mvn -f ./catalog-service clean

echo "Removing cart artefacts..."
mvn -f ./cart-service clean

echo "Removing customer artefacts..."
mvn -f ./customer-service clean

echo "Removing order artefacts..."
mvn -f ./order-service clean

echo "Removing int-test artefacts..."
mvn -f ./int-test clean

function remove_image_by_name() {
  image_id=$(docker images -f=reference="$1" -q)
  echo "Removing image:$1 id: $image_id"
  if [ -n "$image_id" ]
  then
    docker rmi "$image_id"
  fi
}

echo "Removing images..."
if [ "$1" == "deep" ]
then
  echo "Remove all images in docker-copmpose.yaml"
  docker-compose down --rmi all
else
  echo "Remove images of application only"
  remove_image_by_name "cart-service"
  remove_image_by_name "catalog-service"
  remove_image_by_name "customer-service"
  remove_image_by_name "order-service"
fi
echo "Cleaning finished"