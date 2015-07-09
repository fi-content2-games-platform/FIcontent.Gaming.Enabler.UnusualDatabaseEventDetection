#! /bin/bash -e

set -x

echo "starting '$0' inside '$(pwd)'"

exec /usr/local/bin/forego start -e /root/.env -f ../Procfile
