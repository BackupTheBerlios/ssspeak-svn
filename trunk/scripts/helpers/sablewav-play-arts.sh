#! /bin/sh
while read file; do
    ./raw2arts.sh < $file | artscat 
    rm $file
done