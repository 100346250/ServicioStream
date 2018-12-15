#!/bin/bash

# java -cp .:vlcj-3.10.1/vlcj-3.10.1.jar:vlcj-3.10.1/slf4j-simple-1.7.21.jar:jar/sip-sdp.jar:jar/nist-sip-1.2-ims.jar:jar/nist-sdp-1.0.jar:jar/log4j-1.2.8.jar:jar/JainSipRi1.2.jar:jar/JainSipApi1.2.jar:jar/concurrent.jar:jar/commons-codec.20041127.091804.jar Client 163.117.144.156 5860 movie.mp4@163.117.144.157:5860

javac -cp .:vlcj-3.10.1/vlcj-3.10.1.jar:vlcj-3.10.1/slf4j-simple-1.7.21.jar:jar/JainSipApi1.2.jar:jar/sip-sdp.jar:jar/JainSipRi1.2.jar:jar/log4j-1.2.8.jar:jar/nist-sdp-1.0.jar:jar/nist-sip-1.2-ims.jar:jar/commons-codec-20041127.091804.jar:jar/concurrent.jar Client 163.117.144.156 5860 movie.mp4@163.117.144.157:5860
