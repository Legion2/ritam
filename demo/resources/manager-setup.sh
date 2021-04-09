#!/bin/bash

BASEDIR=$(dirname $(readlink -f "$0"))
(
    cd "$BASEDIR"
    if ! command -v ritam &> /dev/null
    then
        echo "ritam could not be found"
    fi

    ritam apply devices device1.yaml
    ritam apply devices device2.yaml
    ritam apply devices device3.yaml
    ritam apply devices device4.yaml
    ritam apply devices device5.yaml
)

