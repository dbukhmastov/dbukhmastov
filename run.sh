#!/bin/bash
echo WORKSPACE=$WORKSPACE
export JAVA_HOME=/usr/lib/jvm/java-8-oracle
export TESTSUITE_DIR=$CURRENT_DIR/testsuite
if [ -d $TESTSUITE_DIR ]; then
  rm -rf $TESTSUITE_DIR
fi
mkdir $TESTSUITE_DIR
unzip -qo $CURRENT_DIR/dbukhmastov/tests.zip -d $TESTSUITE_DIR

export TESTINFO_FILE=$TESTSUITE_DIR/classes/apstec/testinfo

echo ip 192.168.71.128 > $TESTINFO_FILE
echo cmd_port 28929 >> $TESTINFO_FILE
echo data_port 28930 >> $TESTINFO_FILE
echo timeout 120000 >> $TESTINFO_FILE
echo socket_timeout 60000 >> $TESTINFO_FILE
echo send_buffer 2048 >> $TESTINFO_FILE
echo receive_buffer 65536 >> $TESTINFO_FILE

cd $TESTSUITE_DIR
$JAVA_HOME/bin/java -Djavatest.processCommand.inheritEnv=true -jar $TESTSUITE_DIR/lib/javatest.jar\
 -batch -config $TESTSUITE_DIR/linuxConfig.jti -workdir -create -overwrite $TESTSUITE_DIR/workdir\
 -set simple.jvm $JAVA_HOME/bin/java\
 -testsuite $TESTSUITE_DIR -excludeList $TESTSUITE_DIR/exclude.jtx -runtests\
 -writeReport -type html -filter allTests $TESTSUITE_DIR/ReportDir
cd $CURRENT_DIR
