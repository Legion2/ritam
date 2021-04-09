#!/bin/bash

echo '"install requirements"' > $TOSCA_OUTPUT_status
pip install -r requirements.txt

if pgrep -f "python -u main.py" &>/dev/null; then
    echo '"it is already running"' > $TOSCA_OUTPUT_status
    exit
else
    python -u main.py
    echo '"starting"' > $TOSCA_OUTPUT_status
fi
