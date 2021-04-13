#!/bin/bash

set -e -o pipefail

PM2_HOME=/etc/.pm2 pm2 ping &> /dev/null
exec /deployments/run-java.sh "$@"
