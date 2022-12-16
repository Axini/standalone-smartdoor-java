#!/bin/sh
# Script to start the pure Java version of the standalone SmartDoor SUT.

JAVA=/usr/bin/java

WEBSOCKET_JARS=./jar/Java-WebSocket-1.5.2.jar
SLF4J_JARS=./jar/slf4j-api-1.7.25.jar:./jar/slf4j-simple-1.7.25.jar
CLASS_PATH=.:$WEBSOCKET_JARS:$SLF4J_JARS

HOST=localhost
PORT=3001
MANUFACTURER=Axini

echo "Starting SmartDoor SUT at ws://$HOST:$PORT"
$JAVA -cp $CLASS_PATH SmartDoorSut $HOST $PORT $MANUFACTURER
