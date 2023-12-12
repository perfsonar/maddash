#!/bin/sh

#include variables
currdir="$(dirname "$0")"
if [ -f "${currdir}/maddash-server.env" ]; then
    source ${currdir}/maddash-server.env
fi

#parse command-line opts
#NOTE: pidfile not used
pidfile=$1
jarfile=$2
shift 2
vers="2.0.5"
shortname=maddash-server

if [ -z "$jarfile" ]; then
    jarfile=./target/${shortname}-$vers.one-jar.jar
    echo "Starting ${shortname} with version:$vers"
fi

#set java opts
full_java_opts=$JAVA_OPTS
if [ -n "$JAVA_MAX_HEAP" ]; then
    full_java_opts="-Xmx${JAVA_MAX_HEAP} $full_java_opts"
fi

java $full_java_opts -jar $jarfile $*
