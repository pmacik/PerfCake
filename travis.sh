#!/bin/bash
# Abort on Error
#set -e

export PING_SLEEP=30s
export MAVEN_OPTS="-Xmx326m"; 

# Set up a repeating loop to send some output to Travis.
bash -c "while true; do echo \$(date) - building ...; sleep $PING_SLEEP; done" &
export PING_LOOP_PID=$!

# My build is using maven, but you could build anything with this, E.g.
mvn -l maven.log install site assembly:single javadoc:javadoc -PallTests

if [[ $? -ne 0 ]];
then
	tail -c 2M maven.log;
fi

zip -9r site.zip perfcake/target/site
zip -9r distro.zip perfcake/target/*tar* perfcake/target/*zip
zip -9r distro-agent.zip perfcake-agent/target/*tar* perfcake-agent/target/*zip
zip -9r javadoc.zip perfcake/target/site/apidocs
zip -9r javadoc-agent.zip perfcake-agent/target/site/apidocs

# nicely terminate the ping output loop
kill $PING_LOOP_PID