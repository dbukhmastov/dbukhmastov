#!/bin/bash
export JAVA_HOME=/usr/lib/jvm/java-8-oracle
export CURRENT_DIR=/home/mmwoperator/tmp
export TESTINFO_FILE=$CURRENT_DIR/classes/apstec/testinfo

echo ip 192.168.71.128 > $TESTINFO_FILE
echo cmd_port 28929 >> $TESTINFO_FILE
echo data_port 28930 >> $TESTINFO_FILE
echo timeout 120000 >> $TESTINFO_FILE
echo socket_timeout 60000 >> $TESTINFO_FILE
echo send_buffer 2048 >> $TESTINFO_FILE
echo receive_buffer 65536 >> $TESTINFO_FILE

$JAVA_HOME/bin/java -Djavatest.processCommand.inheritEnv=true -jar $CURRENT_DIR/lib/javatest.jar\
 -batch -config $CURRENT_DIR/myConfig.jti -workdir -create -overwrite $CURRENT_DIR/workdir\
 -set simple.jvm $JAVA_HOME/bin/java\
 -testsuite $CURRENT_DIR -excludeList $CURRENT_DIR/exclude.jtx -runtests\
 -writeReport -type html -filter allTests $CURRENT_DIR/ReportDir
