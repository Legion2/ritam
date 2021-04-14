FROM ubuntu:20.04

RUN apt update && \
    apt install --no-install-recommends -y ca-certificates curl unzip && \
    curl -L https://github.com/Legion2/ritam/releases/download/v1.0.0/ritam-linux-x64.zip -o /tmp/ritam.zip && \
    unzip -o /tmp/ritam.zip -d /tmp && \
    mv /tmp/ritam-cli-linux-x64 /opt/ritam

ENV PATH="/opt/ritam:${PATH}" RITAM_CLI_ENDPOINT=http://manager:8080/

COPY manager-setup.sh /manager-setup.sh

CMD [ "/manager-setup.sh" ]
