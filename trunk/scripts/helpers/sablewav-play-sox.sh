#! /bin/sh
. $(dirname $0)/env.sh

lockfile=/tmp/sablewav-play.lock

trap "rm -f $lockfile" 0

log locking $lockfile
lockfile $lockfile
log lock succeeded

while read file; do
    log sablewav-pay.sh: $file
    ./rawplay.sh < $file
    #rm $file
done