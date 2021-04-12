#!/bin/bash

set -e -o pipefail

RESOURCES=/resources/

ritam apply devices ${RESOURCES}device1.yaml
ritam apply devices ${RESOURCES}device2.yaml
ritam apply devices ${RESOURCES}device3.yaml
ritam apply devices ${RESOURCES}device4.yaml
ritam apply devices ${RESOURCES}device5.yaml
