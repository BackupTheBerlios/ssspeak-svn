#! /bin/sh

cd $(dirname $0)

bash -x ./sable2wav --tts $@ 2> sable2wav.log