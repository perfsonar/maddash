#!/bin/bash

java -Done-jar.main.class=net.es.maddash.utils.DBClientUtil -Djava.net.preferIPv4Stack=true -jar target/maddash-server-0.1.one-jar.jar $*
