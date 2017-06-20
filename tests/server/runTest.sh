#!/bin/bash
export JAVA_HOME=/home/mmwoperator/jenkins/tools/jre
export TESTINFO_FILE=./classes/apstec/testinfo

echo integrator_port 45678 > $TESTINFO_FILE
echo timeout 120000 >> $TESTINFO_FILE
echo socket_timeout 60000 >> $TESTINFO_FILE
echo stress_test_time 120000 >> $TESTINFO_FILE

$JAVA_HOME/bin/java -Djavatest.processCommand.inheritEnv=true -jar ./lib/javatest.jar\
 -batch -config ./linuxConfig.jti -workdir -create -overwrite ./workdir\
 -testsuite . -excludeList ./exclude.jtx -runtests -writeReport -type html\
 -filter allTests ./reportDir
