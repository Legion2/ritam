FROM ubuntu:20.04

RUN apt update && apt install --no-install-recommends -y ca-certificates curl unzip

COPY manager-setup.sh /manager-setup.sh

CMD [ "/manager-setup.sh" ]
