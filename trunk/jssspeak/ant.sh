#! /bin/sh
export CLASSPATH=$(dirname $0)/lib/aspectjrt.jar:$(dirname $0)/lib/aspectjtools.jar:${CLASSPATH}

exec ant $@