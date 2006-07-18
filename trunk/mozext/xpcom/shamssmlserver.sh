#! /bin/sh

output=/tmp/ssmlFromServer.fifo
input=/tmp/ssmlToServer.fifo
mkfifo $output
mkfifo $input

counter=0


mark () {
    let counter++
    echo mark:$counter
}

fakemarks () {
    mark;
    sleep 3
    mark
    sleep 3
    mark
}
synth () {
    while read line; do
        echo 1>&2 $line
        fakemarks;
    done
}

synth < $input > $output
