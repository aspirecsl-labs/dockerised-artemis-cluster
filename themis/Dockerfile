FROM haproxy:2.1
LABEL maintainer="labs@aspirecsl.com"
# Make sure pipes are considered to determine success, see: https://github.com/hadolint/hadolint/wiki/DL4006
SHELL ["/bin/bash", "-o", "pipefail", "-c"]

######################################
###     Set proxy if necessary     ###
#------------------------------------#
ENV http_proxy http://85.115.54.201:80/
ENV https_proxy http://85.115.54.201:80/
ENV no_proxy="127.0.0.1, localhost"
######################################

COPY haproxy.cfg /usr/local/etc/haproxy/haproxy.cfg


RUN groupadd -g 1000 -r haproxy && \
    useradd -r -u 1000 -g haproxy haproxy && \
    mkdir -p /run/haproxy && \
    touch /run/haproxy/admin.sock && \
    chown haproxy:haproxy /run/haproxy/admin.sock

EXPOSE \
# HaProxy Stats
    8080 \
# Artemis MQ UI
    8161 \
# Artemis MQ
    61616