#!/bin/bash
set -x

ARTEMIS_HOME=/artemis
ARTEMIS_INSTANCE=/usr/artemis/instance
export PATH=$PATH:$ARTEMIS_HOME/bin:$ARTEMIS_INSTANCE/bin

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
fi

cd $ARTEMIS_INSTANCE || exit 1
exec ./bin/artemis "$@"