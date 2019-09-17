Color_Off='\033[0m'       # Text Reset
BGreen='\033[1;32m'       # Green
BBlue='\033[1;34m'        # Blue
BINFO=${BBlue}INFO${Color_Off}

echo -e "[${BINFO}] ------------------------------------------------------------------------"
echo -e "[${BINFO}] ${BGreen}INTEGRATION TEST STARTING... ${Color_Off}"
echo -e "[${BINFO}] ------------------------------------------------------------------------"
echo

mvn -f ./int-test clean install
