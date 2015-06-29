#!/usr/bin/env bash

# How to use: ./test.sh HOST PORT
HOST=$1
PORT=$2
[ "$HOST" ] || HOST="localhost"
[ "$PORT" ] || PORT="8000"
echo "Entering FIC2Lab smoke test sequence. Vendor's validation procedure of the Unusual Database-Event Detection SE engaged. Target host: $HOST"


# wait for service
echo -n "Waiting service to launch"
while ! (netcat -vz localhost $2 &> /dev/null); do echo -n "."; sleep 5; done
echo ""
echo "service is running."


#echo "Run smoke test for Unusual Database-Event Detection"

# get return code of command to show monitoring website; 200 is ok, anything else is an error
RETCODE=$(curl --max-time 5 -o /dev/null -sw '%{http_code}' http://$1:$2/)
if [ "$RETCODE" == "200" ]; then
	echo "Curl command for UDED is OK."
else
	echo "Curl command for UDED failed. Validation procedure terminated."
	echo "Debug information: HTTP code $RETCODE instead of expected 200 from SHOST"
	exit 1
fi

echo "Smoke test completed. Vendor component validation procedure succeeded. Over."
