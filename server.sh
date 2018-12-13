#!/bin/bash

javac -cp .:vlcj-3.10.1/vlcj-3.10.1.jar:vlcj-3.10.1/slf4j-simple-1.7.21.jar *.java

java -cp .:vlcj-3.10.1/vlcj-3.10.1.jar:vlcj-3.10.1/slf4j-simple-1.7.21.jar Server 5854
