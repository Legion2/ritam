#!/bin/bash

set -e -o pipefail

curl -L https://github.com/Legion2/ritam/releases/download/v0.3.0/ritam-linux-x64.zip -o /tmp/ritam.zip
unzip -o /tmp/ritam.zip -d /tmp &> /dev/null
RITAM_COMMAND="/tmp/ritam-cli-linux-x64/ritam -e http://manager:8080/"

RESOURCES=/resources/

$RITAM_COMMAND apply devices ${RESOURCES}device1.yaml
$RITAM_COMMAND apply devices ${RESOURCES}device2.yaml
$RITAM_COMMAND apply devices ${RESOURCES}device3.yaml
$RITAM_COMMAND apply devices ${RESOURCES}device4.yaml
$RITAM_COMMAND apply devices ${RESOURCES}device5.yaml
