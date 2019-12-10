FROM openjdk:8-jre-slim-buster
LABEL maintainer="labs@aspirecsl.com"
# Make sure pipes are considered to determine success, see: https://github.com/hadolint/hadolint/wiki/DL4006
SHELL ["/bin/bash", "-o", "pipefail", "-c"]

# set proxy if necessary
ENV PROXY_ADDRESS ipv4.31.10.40.245.webdefence.global.blackspider.com:80
ENV http_proxy http://$PROXY_ADDRESS/
ENV https_proxy http://$PROXY_ADDRESS/
ENV no_proxy="127.0.0.1, localhost"

ENV SHA512_VAL=9e7ad63fad2e2bcfc727d68a49df82fb71820dc785c04f737836c9f831a9a2aa85d4f7b4469e2c27373da0c0b6b1a58b3adee033813e5e9787badac1b0ceade7
ENV ARTEMIS_DOWNLOAD_LINK 'https://www.apache.org/dyn/closer.cgi?filename=activemq/activemq-artemis/2.10.1/apache-artemis-2.10.1-bin.tar.gz&action=download'

COPY broker.xml /
COPY entrypoint.sh /
COPY jgroups-file_ping.xml /
COPY artemis-roles.properties /
COPY artemis-users.properties /

RUN groupadd -g 1000 -r artemis && useradd -r -u 1000 -g artemis artemis && \
    apt-get update && \
    ############################################################
    ###          N/W debug tools for DEV phase ONLY          ###
    #----------------------------------------------------------#
    # apt-get install -y --no-install-recommends iputils-ping \#
    #                       net-tools inetutils-traceroute && \#
    ############################################################
    apt-get install -y --no-install-recommends libaio1 wget && \
    wget --output-document=/tmp/artemis.tgz $ARTEMIS_DOWNLOAD_LINK && \
    if [ "$SHA512_VAL" != "$(sha512sum /tmp/artemis.tgz | awk '{print($1)}')" ];\
    then \
        echo "unexpected sha512 checksum on the downloaded tarball! exiting."  && \
        exit 1; \
    else \
        mkdir -p /var/jgroups /usr/artemis /var/artemis /tmp/artemis && \
        tar --exclude=*/examples/* -x -v -f /tmp/artemis.tgz && \
        mv apache-artemis-* artemis && \
        chmod ugo+x /entrypoint.sh && \
        chown -R artemis:artemis \
                     /artemis \
                     /broker.xml \
                     /var/jgroups \
                     /usr/artemis \
                     /var/artemis \
                     /tmp/artemis \
                     /entrypoint.sh \
                     /jgroups-file_ping.xml \
                     /artemis-roles.properties \
                     /artemis-users.properties; \
    fi; \
    rm -rf /var/lib/apt/lists/* /tmp/artemis.tgz

VOLUME ["/var/jgroups"]
VOLUME ["/var/artemis"]
EXPOSE \
# Web Server
    8161 \
# Messages
    61616

USER artemis
ENTRYPOINT ["/entrypoint.sh"]
CMD ["run"]