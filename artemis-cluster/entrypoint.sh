#!/bin/bash
set -x

export ARTEMIS_HOME=/artemis
export PATH=$PATH:$ARTEMIS_HOME/bin:/var/artemis/"${CLUSTER_NAME}"/bin

BROKER_SED_EXPR="s/__HOST__/$(hostname -f)/g"
BROKER=/var/artemis/${CLUSTER_NAME}/etc/broker.xml

JGROUPS_SED_EXPR="s/__CLUSTER_NAME__/${CLUSTER_NAME}/g"
JGROUPS=/var/artemis/${CLUSTER_NAME}/etc/jgroups-file_ping.xml

sed -i -e "${BROKER_SED_EXPR}" "${BROKER}"
sed -i -e "${JGROUPS_SED_EXPR}" "${JGROUPS}"

exec ./bin/artemis "$@"