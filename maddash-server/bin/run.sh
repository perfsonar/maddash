#!/bin/bash
java -Xmx768m -Djava.net.preferIPv4Stack=true -jar target/maddash-server-2.0.one-jar.jar $*
##
#Use below to run JMX agent thread and memory monitoring
#java -Xmx256m -Dmaddash.jmx.port=8080 -javaagent:target/maddash-server-1.2-jmx.jar -Djava.net.preferIPv4Stack=true -jar target/maddash-server-1.2.one-jar.jar  $*

