#!/bin/bash
# curl localhost:8888/prime/9223372036854775783
clear
javac -d bin ./src/*.java 
java -cp ./bin/ LoadBalancer