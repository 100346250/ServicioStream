#!/bin/bash

javac -cp .:vlcj-3.10.1/vlcj-3.10.1.jar:vlcj-3.10.1/slf4j-simple-1.7.21.jar:jar/sip-sdp.jar:jar/nist-sip-1.2-ims.jar:jar/nist-sdp-1.0.jar:jar/log4j-1.2.8.jar:jar/JainSipRi1.2.jar:jar/JainSipApi1.2.jar:jar/concurrent.jar:jar/commons-codec.20041127.091804.jar *.java

java -cp .:vlcj-3.10.1/vlcj-3.10.1.jar:vlcj-3.10.1/slf4j-simple-1.7.21.jar:jar/sip-sdp.jar:jar/nist-sip-1.2-ims.jar:jar/nist-sdp-1.0.jar:jar/log4j-1.2.8.jar:jar/JainSipRi1.2.jar:jar/JainSipApi1.2.jar:jar/concurrent.jar:jar/commons-codec.20041127.091804.jar Server 5854 163.117.144.157 5860