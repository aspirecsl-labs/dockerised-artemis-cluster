#!/bin/bash
set -x

ARTEMIS_HOME=/artemis
ARTEMIS_INSTANCE=/usr/artemis/instance

ARTEMIS_USER=artemis
ARTEMIS_PASSWORD=artemis

# only create an instance if one doesn't exist already
if [ ! -d $ARTEMIS_INSTANCE ]
then
  echo "creating artemis instance..."
  $ARTEMIS_HOME/bin/artemis create  \
                              --silent \
                              --name artemis \
                              --no-autocreate \
                              --relax-jolokia \
                              --allow-anonymous \
                              --no-amqp-acceptor \
                              --no-mqtt-acceptor \
                              --no-stomp-acceptor \
                              --no-hornetq-acceptor \
                              --host "$(hostname -f)" \
                              --data /var/artemis \
                              --http-host 0.0.0.0 \
                              --user "${ARTEMIS_USER}" \
                              --password "${ARTEMIS_PASSWORD}" \
                            "${ARTEMIS_INSTANCE}"
  rm -rf /usr/artemis/tmp
  ln -s /tmp/artemis /usr/artemis/tmp
  cp -f /broker.xml $ARTEMIS_INSTANCE/etc
  cp -f /jolokia-access.xml $ARTEMIS_INSTANCE/etc
  cp -f /jgroups-file_ping.xml $ARTEMIS_INSTANCE/etc
  cp -f /artemis-roles.properties $ARTEMIS_INSTANCE/etc
  cp -f /artemis-users.properties $ARTEMIS_INSTANCE/etc

  # fill the host info in the broker.xml
  BROKER=$ARTEMIS_INSTANCE/etc/broker.xml
  BROKER_SED_EXPR="s/{HOST}/$(hostname -f)/g"
  sed -i -e "${BROKER_SED_EXPR}" "${BROKER}"
fi

cd $ARTEMIS_INSTANCE || exit 1
exec ./bin/artemis "$@"