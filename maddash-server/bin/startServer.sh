#!/bin/sh

#include variables
currdir="$(dirname "$0")"
if [ -f "${currdir}/maddash-server.env" ]; then
    source ${currdir}/maddash-server.env
fi

#parse command-line opts
pidfile=$1
jarfile=$2
shift 2
vers="2.0.4"
shortname=maddash-server

if [ -z "$pidfile" ]; then
    DEFAULT_PID_DIR="./run"
    if [ ! -d "$DEFAULT_PID_DIR" ]; then
        mkdir "$DEFAULT_PID_DIR"
    fi
    pidfile=$DEFAULT_PID_DIR/${shortname}.pid
fi

if [ -z "$jarfile" ]; then
    jarfile=./target/${shortname}-$vers.one-jar.jar
    echo "Starting ${shortname} with version:$vers"
fi

#set java opts
full_java_opts=$JAVA_OPTS
if [ -n "$JAVA_MAX_HEAP" ]; then
    full_java_opts="-Xmx${JAVA_MAX_HEAP} $full_java_opts"
fi

java $full_java_opts -jar $jarfile $* &
echo $! > $pidfile

