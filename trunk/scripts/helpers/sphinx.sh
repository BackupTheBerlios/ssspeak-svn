#! /bin/sh

startperlbox () {
    perlbox-voice 2>&1 | tee /tmp/sphinx.log
}


generateStart () {
    echo "start:ehm!";
}

generateCmd () {
    echo "cmd:$1"
}

getCmd () {
    echo "$@" | sed 's,^.*ESTPATH: \([A-Z]*\).*,\1,'
}

scancommands () {
    while read line; do
        case "$line" in
            INFO:*Livemode*) generateStart;;
            INFO:*BESTPATH:*) generateCmd $(getCmd ${line});;
        esac
    done
}

startperlbox | scancommands
