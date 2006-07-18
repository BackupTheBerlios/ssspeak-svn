#! /bin/sh
export WORDS_DIC=${srcdir}/../data/words.dic

set -e

./RecognizerTest hello big world
./RecognizerTest world hello big


