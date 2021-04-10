#!/bin/bash

set -e -o pipefail

BASEDIR=$(dirname $(readlink -f "$0"))
(
    cd "$BASEDIR"
    if ! command -v ritam2 &> /dev/null
    then
        echo "ritam could not be found, installing it..."
        curl -L https://github.com/Legion2/ritam/releases/download/v0.3.0/ritam-linux-x64.zip -o /tmp/ritam.zip
        unzip -o /tmp/ritam.zip -d /tmp &> /dev/null
        RITAM_COMMAND=/tmp/ritam-cli-linux-x64/ritam
    else
        echo "using existing ritam executable"
        RITAM_COMMAND=ritam
    fi

    $RITAM_COMMAND apply devices device1.yaml
    $RITAM_COMMAND apply devices device2.yaml
    $RITAM_COMMAND apply devices device3.yaml
    $RITAM_COMMAND apply devices device4.yaml
    $RITAM_COMMAND apply devices device5.yaml
)
