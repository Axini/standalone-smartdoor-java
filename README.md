// Copyright 2023 Axini B.V. https://www.axini.com, see: LICENSE.txt.

# Description

This project provides a pure *Java* implementation of a *standalone* SmartDoor SUT.
The implementation is heavily based on Axini's in-house Ruby implementation of the (standalone) SmartDoor SUT.

This *pure* Java implementation has been developed to make it easier to distribute the SUT. It is not longer required to encapsulate the Ruby implementation of the SmartDoor SUT into a >50MB jar file containing a complete JRuby distribution.

This is an initial version of the implementation; it is still work in progress.


# External libraries

The `./jar` directory contains several `.jar` files from external parties, which are used by the SmartDoor SUT.

## Java-Websocket
https://github.com/TooTallNate/Java-WebSocket

./jar/Java-WebSocket-1.5.2.jar

## Simple Logging Facade For Java (SLF4J)
https://www.slf4j.org/

./jar/slf4j-api-1.7.25.jar
./jar/slf4j-simple-1.7.25.jar

The file `simplelogger.properties` contains the (formatting) settings of the logger.


# Compilation

The distribution contains a makefile with the target `smartdoor_sut` which compiles the three Java files. After building the Java classes, the shell script `run_smartdoor_sut.sh` can be used to start the SmartDoor SUT.

By default, the SmartDoor SUT listens on `ws://localhost:3001`. This can be changed in the script `run_smartdoor_sut.sh`, though.


# Current limitations

- Documentation is lacking. No javadoc comments for methods.
- Error handling should be improved upon.
- Command line options to the adapter script are not implemented yet.
- (Unit) tests are missing. The SUT has been tested with AMP, though. ;-)
