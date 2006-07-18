#! /bin/sh

sox -t raw -r 16000 -s -w - -t ossdsp -s /dev/dsp