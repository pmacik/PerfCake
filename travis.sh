#!/bin/bash
# Abort on Error
#set -e

#export PING_SLEEP=30s
export MAVEN_OPTS="-Xmx326m";

# Set up a repeating loop to send some output to Travis.
#bash -c "while true; do echo \$(date) - building ...; sleep $PING_SLEEP; done" &
#export PING_LOOP_PID=$!

# My build is using maven, but you could build anything with this, E.g.

#mvn install -DskipTests
#mvn -pl perfcake package assembly:single -DskipTests
#mvn -pl perfcake-agent package assembly:single -DskipTests

cp -vf ./perfcake/target/perfcake-*-bin.zip .
cp -vf ./perfcake/target/perfcake-*-bin.tar.gz .
cp -vf ./perfcake/target/perfcake-*-bin.tar.bz2 .
cp -vf ./perfcake-agent/target/perfcake-agent-*-bin-agent.zip .
cp -vf ./perfcake-agent/target/perfcake-agent-*-bin-agent.tar.gz .
cp -vf ./perfcake-agent/target/perfcake-agent-*-bin-agent.tar.bz2 .

#maven_exit=$?
#if [[ $maven_exit -ne 0 ]];
#then
#	tail -c 2M maven.log;
#fi

# nicely terminate the ping output loop
#kill $PING_LOOP_PID
#exit $maven_exit