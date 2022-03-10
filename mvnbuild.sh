#!/bin/bash

clear && mvn clean && \
 sleep 5  &&  mvn install && \
 sleep 5 && \
 cp -v `pwd`/target/*.jar /tmp/volatile_hd/ && \
 echo "" > /tmp/volatile_hd/logs.log && \
 echo " .... Clear ... "
