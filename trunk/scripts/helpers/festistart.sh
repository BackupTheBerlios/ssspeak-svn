#! /bin/sh

. $(dirname $0)/env.sh

mkdir -p /tmp/festilog

if test -z "$(pidof -x festival_server)"; then
    logger -t festistart.sh "starting ${FESTIDIR}/bin/festival_server using festival binary ${FESTIDIR}/bin/festival"
    ${FESTIDIR}/bin/festival_server  -l /tmp/festilog ${FESTIDIR}/bin/festival >& /tmp/festilog/run.log &
    sleep 2
else
    logger -t festistart.sh "festival_server already running"
fi
