#!/bin/bash
set -x

ARTEMIS_HOME=/artemis
ARTEMIS_INSTANCE=/usr/artemis/instance

# only create an instance if one doesn't exist already
if [ ! -d $ARTEMIS_INSTANCE ]
then
  echo "creating artemis instance..."
  $ARTEMIS_HOME/bin/artemis create  \
                              --silent \
                              --no-web \
                              --require-login \
                              --relax-jolokia \
                              --name artemis \
                              --no-amqp-acceptor \
                              --no-mqtt-acceptor \
                              --no-stomp-acceptor \
                              --no-hornetq-acceptor \
                              --host "$(hostname -f)" \
                              --data /var/artemis \
                              --user "${ARTEMIS_USER}" \
                              --password "${ARTEMIS_PASSWORD}" \
                            "${ARTEMIS_INSTANCE}"
  rm -rf /usr/artemis/tmp
  ln -s /tmp/artemis /usr/artemis/tmp
  cp -f /broker.xml $ARTEMIS_INSTANCE/etc
  cp -f /jgroups-file_ping.xml $ARTEMIS_INSTANCE/etc

  # fill the host info in the broker.xml
  BROKER=$ARTEMIS_INSTANCE/etc/broker.xml
  BROKER_SED_EXPR="s/__host__/$(hostname -f)/g"
  sed -i -e "${BROKER_SED_EXPR}" "${BROKER}"
fi

cd $ARTEMIS_INSTANCE || exit 1
exec ./bin/artemis "$@"