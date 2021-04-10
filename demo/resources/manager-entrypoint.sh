#!/bin/bash

set -e -o pipefail

/resources/manager-setup.sh
exec /deployments/run-java.sh "$@"
