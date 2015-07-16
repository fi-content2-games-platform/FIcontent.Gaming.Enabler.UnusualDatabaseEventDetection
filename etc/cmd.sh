#! /bin/bash -e

#set -x

if [ "$#" -ne 1 ]; then
    echo "Incorrect number of parameters. Usage:"
    echo "$0 config.properties"
    exit 1
fi

if [ -n "${MYSQL_PORT_3306_TCP}" ]; then
    if [ -z "${UDED_DB_HOST}" ]; then
	UDED_DB_HOST='mysql'
    else
	echo >&2 'warning: both UDED_DB_HOST and MYSQL_PORT_3306_TCP found'
	echo >&2 "  Connecting to UDED_DB_HOST (${UDED_DB_HOST})"
	echo >&2 '  instead of the linked mysql container'
    fi
fi

if [ -z "${UDED_DB_HOST}" ]; then
    echo >&2 'error: missing UDED_DB_HOST and/or MYSQL_PORT_3306_TCP environment variables'
    echo >&2 '  Did you forget to --link some_mysql_container:mysql or set an external db'
    echo >&2 '  with -e UDED_DB_HOST=hostname:port?'
    exit 1
fi

: ${UDED_DB_USER:=root}
if [ "$UDED_DB_USER" = 'root' ]; then
    : ${UDED_DB_PASSWORD:=$MYSQL_ENV_MYSQL_ROOT_PASSWORD}
fi
: ${UDED_DB_NAME:=mygame}

if [ -z "$UDED_DB_PASSWORD" ]; then
    echo >&2 'error: missing required UDED_DB_PASSWORD environment variable'
    echo >&2 '  Did you forget to -e UDED_DB_PASSWORD=... ?'
    echo >&2
    echo >&2 '  (Also of interest might be UDED_DB_USER and UDED_DB_NAME.)'
    exit 1
fi

cat <<EOF
Using the parameters:
  UDED_DB_HOST='${UDED_DB_HOST}'
  UDED_DB_USER='${UDED_DB_USER}'
  UDED_DB_NAME='${UDED_DB_NAME}'
  UDED_DB_PASSWORD='$(printf "%*s" "${#UDED_DB_PASSWORD}" | tr " " "*")'
EOF

_TMP_HOST=$(echo "${UDED_DB_HOST}" | sed 's/:/\\:/g')
_TMP_PASSWORD=$(echo "${UDED_DB_PASSWORD}" | sed 's/:/\\:/g')

sed -i "s/^url =.*/url = jdbc\\:mysql\\:\/\/${_TMP_HOST}\/${UDED_DB_NAME}/" $1
sed -i "s/^user =.*/user = ${UDED_DB_USER}/" $1
sed -i "s/^password =.*/password = ${_TMP_PASSWORD}/" $1

exec /usr/bin/java -Dlog4j.debug -Dlog4j.configuration=file:/root/etc/log4j.properties -Djava.util.logging.config.file=/root/etc/logging.properties -cp jars/*:src:target/* -jar target/uded-0.3.jar
