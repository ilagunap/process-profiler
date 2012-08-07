#!/bin/bash

export JAVAASSIST=./javaassist/javassist-3.16.1-GA/javassist.jar
export PROFILE_PATTERN=com.ilaguna.test

java -classpath ./bin:$JAVAASSIST:./bin/metricscollector.jar -javaagent:./bin/metricscollector.jar com.ilaguna.test.TestShapes
