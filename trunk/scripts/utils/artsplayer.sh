#! /bin/bash


usage="usage: $0 --server <event_output_pipe>, usage: $0 <input_wav..>"

usage () {
    echo 1>&2 $usage
    exit 1
}

marker_audio_file=sound/link-test.wav
#declare -i mark_size="$(stat -c %s $marker_audio_file) - 44"
mark_size=1614
event_output_pipe=$2

ismark () {
    input_size=$(stat -c %s $1)
    
    if test $mark_size -eq $input_size; then
        return 0
    fi
    return 1
}

init () {
    for i in "in" "out" "ctl"; do
        test -e artsplayer_${i}.fifo || mkfifo artsplayer_${i}.fifo
    done
}

playwav () {
     sox -t raw -r 16000 -s -w $1 -r 88200 -t raw -

     if test x$(basename $1) != x$(basename $1 .hlnk); then
         rm $1
     fi
}

playloop () {
    artscat artsplayer_out.fifo
}

count=0

inputloop () {
    while true; do
        read wavfile

        if test -n "$wavfile"; then

            if test $wavfile = /dev/null; then
                exit
            fi

            ismark $wavfile && {
                echo MARK:$count >> $event_output_pipe
                let count++

            }

            playwav $wavfile
        fi
    done
}

controlloop () {
    while true; do
        read cmd < artsplayer_ctl.fifo
        
        if test -n "$cmd"; then
            case "$cmd" in
                p*|P*) psgkill artscat -STOP;;
                c*|C*) psgkill artscat -CONT;;
                q*|Q*|k*|K*) psgkill artscat; playloop &;;
            esac
        fi
    done
}

justplay () {
    for i in $@; do

        if test $i != /dev/null; then
            tmpfile=$i.hlnk
            ln $i $tmpfile
        else
            tmpfile=$i
        fi

        echo $tmpfile >> artsplayer_in.fifo
    done

    exit
}


finish () {
    psgkill 'artsplayer'
    psgkill 'artscat'
}

while test -n "$1"; do
    case "$1" in
        --server) shift; server=1; event_output_pipe=$1;;
        --ctl) shift; doctl $1; exit;;
        -*) usage;;
        *) break;;
    esac

    shift
done

if test -z "$server"; then
    test $# -gt 0 || usage
    justplay $@
fi

trap finish 0


init

playloop &

controlloop &

inputloop < artsplayer_in.fifo > artsplayer_out.fifo




