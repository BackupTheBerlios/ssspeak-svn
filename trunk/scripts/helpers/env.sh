prog=$(basename $0)

log () {
    logger -t "$prog" -- "$@"
}

classpath=$(echo $(dirname $0)/../java/{class,*.jar,*.zip} | tr ' ' ':')
CLASSPATH=${CLASSPATH:-$classpath:$CLASSPATH}

FESTIDIR=${FESTIDIR:-/opt/ssspeak/festival}

export CLASSPATH
