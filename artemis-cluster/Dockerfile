FROM openjdk:8-jre-slim-buster
LABEL maintainer="labs@aspirecsl.com"
# Make sure pipes are considered to determine success, see: https://github.com/hadolint/hadolint/wiki/DL4006
SHELL ["/bin/bash", "-o", "pipefail", "-c"]

# set proxy if necessary
ENV PROXY_ADDRESS ipv4.31.10.40.245.webdefence.global.blackspider.com:80
ENV http_proxy http://$PROXY_ADDRESS/
ENV https_proxy http://$PROXY_ADDRESS/

ENV CLUSTER_NAME ${CLUSTER_NAME:-hello-artemis}
ENV SHA512_VAL=9e7ad63fad2e2bcfc727d68a49df82fb71820dc785c04f737836c9f831a9a2aa85d4f7b4469e2c27373da0c0b6b1a58b3adee033813e5e9787badac1b0ceade7
ENV ARTEMIS_DOWNLOAD_LINK 'https://www.apache.org/dyn/closer.cgi?filename=activemq/activemq-artemis/2.10.1/apache-artemis-2.10.1-bin.tar.gz&action=download'

ENV ARTEMIS_USER artemisuser
ENV ARTEMIS_PASSWORD artemispwd
ENV ANONYMOUS_LOGIN false

ENV CREATE_ARGUMENTS --silent \
                     --name ${CLUSTER_NAME} \
                     --host $(hostname -f) \
                     --user ${ARTEMIS_USER} \
                     --password ${ARTEMIS_PASSWORD} \
                     --require-login \
                     --aio \
                     --no-web \
                     --no-amqp-acceptor \
                     --no-hornetq-acceptor \
                     --no-mqtt-acceptor \
                     --no-stomp-acceptor

RUN groupadd -g 1000 -r artemis && useradd -r -u 1000 -g artemis artemis && \
    apt-get update && \
    apt-get install -y --no-install-recommends libaio1 wget && \
    wget --no-verbose --output-document=/tmp/artemis.tgz $ARTEMIS_DOWNLOAD_LINK && \
    if [ "$SHA512_VAL" != "$(sha512sum /tmp/artemis.tgz | awk '{print($1)}')" ];\
    then \
        echo "unexpected sha512 checksum on the downloaded tarball! exiting."  && \
        exit 1; \
    else \
        echo "configuring artemis instance..." && \
        mkdir -p /var/jgroups/${CLUSTER_NAME} /var/artemis/${CLUSTER_NAME} && \
        tar --exclude=*/web/* --exclude=*/examples/* -x -v -f /tmp/artemis.tgz && \
        mv apache-artemis-* artemis && \
        /artemis/bin/artemis create ${CREATE_ARGUMENTS} /var/artemis/${CLUSTER_NAME}; \
    fi; \
    rm -rf /var/lib/apt/lists/* /tmp/artemis.tgz

USER artemis

EXPOSE \
# Web Server
    8161 \
# JMX Exporter
    9404 \
# Port for CORE,MQTT,AMQP,HORNETQ,STOMP,OPENWIRE
    61616

USER root

COPY ./entrypoint.sh /
COPY ./broker.xml /var/artemis/${CLUSTER_NAME}/etc
COPY jgroups-file_ping.xml /var/artemis/${CLUSTER_NAME}/etc

RUN chmod ugo+x /entrypoint.sh && \
    chown -R artemis.artemis \
             /artemis \
             /entrypoint.sh \
             /var/jgroups/${CLUSTER_NAME} \
             /var/artemis/${CLUSTER_NAME} \
             /var/artemis/${CLUSTER_NAME}/etc/broker.xml \
             /var/artemis/${CLUSTER_NAME}/etc/jgroups-file_ping.xml

USER artemis

VOLUME ["/var/jgroups/${CLUSTER_NAME}", "/var/artemis/${CLUSTER_NAME}"]
WORKDIR /var/artemis/${CLUSTER_NAME}

ENTRYPOINT ["/entrypoint.sh"]
CMD ["run"]