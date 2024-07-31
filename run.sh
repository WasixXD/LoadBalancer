#!/bin/bash 

clear
javac -d bin ./src/*.java 
java -cp ./bin/ LoadBalancer
