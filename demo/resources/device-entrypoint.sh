#!/bin/bash

set -e -o pipefail

pm2 ping &> /dev/null
exec /deployments/run-java.sh "$@"
