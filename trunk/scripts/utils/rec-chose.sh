#! /bin/sh

set -b

usage="$0 <-|word..>"

usage () {
    echo 1>&2 $usage
    exit 1    
}

output=/dev/stdout

if test -z "$1"; then
    usage
fi

mfndir=$(mktemp -u)

finish () {
    #rm -rf $mfndir
    true
}

mkdir ${mfndir}

dic=$(dirname $0)/words.dic;#/usr/lib/perlbox-voice/Perlbox/lib/cmudict_sphinx

makeinput () {
    if test x"$1" = x"-"; then
        cat | sort -u > ${mfndir}/words
    else
        (for i in $@; do echo $i; done) | sort -u > ${mfndir}/words
    fi
}
    
makesent () {
    cat ${mfndir}/words | (
        while read line; do
            printf "<s> %s </s>\n" "$line"
        done
    ) > ${mfndir}/current.sent
}


makelm () {
  /usr/lib/perlbox-voice/Perlbox/bin/quick_lm.pl -s ${mfndir}/current.sent -o ${mfndir}/current.lm
}

makedict () {
    cat ${mfndir}/words | (
        while read i; do
            egrep -i -e "^${i}(\([0-9]+\))?[ ]+" ${dic}
        done
    ) > ${mfndir}/current.dic
}
makeinput $@
makesent
#makedict
makelm

sphinx_cmd="/usr/bin/sphinx2-continuous -live TRUE -ctloffset 0 -ctlcount 100000000 -cepdir ${mfndir}/ctl -datadir ${mfndir}/ctl -agcemax TRUE -langwt 6.5 -fwdflatlw 8.5 -rescorelw 9.5 -ugwt 0.5 -fillpen 1e-10 -silpen 0.005 -inspen 0.65 -top1 -topsenfrm 3 -topsenthresh -70000 -beam 2e-06 -npbeam 2e-06 -lpbeam 2e-05 -lponlybeam 0.0005 -nwbeam 0.0005 -fwdflat FALSE -fwdflatbeam 1e-08 -fwdflatnwbeam 0.0003 -bestpath TRUE -kbdumpdir${mfndir} -lmfn ${mfndir}/current.lm -dictfn ${dic} -noisedict /usr/share/sphinx2/model/hmm/6k/noisedict -phnfn /usr/share/sphinx2/model/hmm/6k/phone -mapfn /usr/share/sphinx2/model/hmm/6k/map -hmmdir /usr/share/sphinx2/model/hmm/6k -hmmdirlist /usr/share/sphinx2/model/hmm/6k -8bsen TRUE -sendumpfn /usr/share/sphinx2/model/hmm/6k/sendump -cbdir /usr/share/sphinx2/model/hmm/6k"

trap finish 0

echo 1>&2 $sphinx_cmd
exec $sphinx_cmd;# 2> ${mfndir}/sphinx.err

