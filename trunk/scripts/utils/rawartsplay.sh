#! /bin/sh
sox -t raw -r 16000 -s -w - -r 88200 -t raw - | artscat
