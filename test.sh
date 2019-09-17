Color_Off='\033[0m'       # Text Reset
BGreen='\033[1;32m'       # Green
BBlue='\033[1;34m'        # Blue
BINFO=${BBlue}INFO${Color_Off}

echo -e "[${BINFO}] ------------------------------------------------------------------------"
echo -e "[${BINFO}] ${BGreen}TEST STARTED.. ${Color_Off}"
echo -e "[${BINFO}] ------------------------------------------------------------------------"
echo
echo "Uploading products.."
curl -F 'file=@./catalog-service/src/main/resources/products.csv' http://localhost:8091/api/catalog/upload

echo
echo "Getting product.."
PRODUCT_1=$(curl http://localhost:8091/api/catalog/1)
echo
echo "Product ${PRODUCT_1}"

echo
echo "Init cart..."
CART_ID=$(curl http://localhost:8090/api/cart/ | cut -c 8-43)
echo "Cart id = ${CART_ID}"

echo
echo "Add product to cart..."
curl http://localhost:8090/api/cart/"${CART_ID}"/add?productId=1
echo
curl http://localhost:8090/api/cart/"${CART_ID}"/add?productId=1

echo
echo -e "[${BINFO}] ------------------------------------------------------------------------"
echo -e "[${BINFO}] ${BGreen}TEST FINISHED! ${Color_Off}"
echo -e "[${BINFO}] ------------------------------------------------------------------------"

