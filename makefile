# makefile for the Java version of the standalone SmartDoor SUT

JAVAC = /usr/bin/javac
JAVA  = /usr/bin/java
JAR   = /usr/bin/jar

WEBSOCKET_JARS = ./jar/Java-WebSocket-1.5.2.jar
SLF4J_JARS = ./jar/slf4j-api-1.7.25.jar:./jar/slf4j-simple-1.7.25.jar

CLASS_PATH = ".:$(WEBSOCKET_JARS):$(SLF4J_JARS)"

%.class: %.java
	${JAVAC} -cp $(CLASS_PATH) $<

default:
	@echo "makefile has no default target"

CLASS_FILES = SmartDoorSut.class SmartDoor.class SmartDoorServer.class
TARGETS = $(CLASS_FILES)

smartdoor_sut: $(TARGETS)
all: smartdoor_sut

# ----- cleaning up

clean:
	rm -f *.class
	rm -f VERSION.txt

very_clean: clean

# ----- make ZIP for distribution

ZIP = /usr/bin/zip
ZIP_NAME = standalone-smartdoor-java.zip
ZIP_OPTIONS = -o -rp -q --exclude=*.git*

THIS_DATE=`date`
THIS_COMMIT=`git rev-parse --short HEAD`

VERSION.txt:
	@echo "Version of this standalone-smartdoor-java SUT: " > $@
	@echo "- created on: ${THIS_DATE}" >> $@
	@echo "- latest git revision: ${THIS_COMMIT}" >> $@

zip: very_clean smartdoor_sut VERSION.txt
	pushd ..; $(ZIP) $(ZIP_OPTIONS) $(ZIP_NAME) standalone-smartdoor-java; popd
