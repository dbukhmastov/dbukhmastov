#!/bin/bash
export JAVA_HOME=/usr/lib/jvm/java-8-oracle
export CURRENT_DIR=/home/mmwoperator/tmp

echo ip 192.168.71.128 > $CURRENT_DIR/classes/apstek/testinfo
echo cmd_port 28929 >> $CURRENT_DIR/classes/apstek/testinfo
echo data_port 28930 >> $CURRENT_DIR/classes/apstek/testinfo
echo timeout 120000 >> $CURRENT_DIR/classes/apstek/testinfo
echo socket_timeout 60000 >> $CURRENT_DIR/classes/apstek/testinfo
echo send_buffer 2048 >> $CURRENT_DIR/classes/apstek/testinfo
echo receive_buffer 65536 >> $CURRENT_DIR/classes/apstek/testinfo

$JAVA_HOME/bin/java -Djavatest.processCommand.inheritEnv=true -jar $CURRENT_DIR/lib/javatest.jar


