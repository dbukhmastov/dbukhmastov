@echo off
set JAVA=C:\java\jdk8\bin\java.exe
set TESTINFO_FILE=%CD%\classes\apstec\testinfo

echo ip 192.168.71.128 > %TESTINFO_FILE%
echo cmd_port 28929 >> %TESTINFO_FILE%
echo data_port 28930 >> %TESTINFO_FILE%
echo timeout 120000 >> %TESTINFO_FILE%
echo socket_timeout 60000 >> %TESTINFO_FILE%
echo send_buffer 2048 >> %TESTINFO_FILE%
echo receive_buffer 65536 >> %TESTINFO_FILE%
echo ElemCnt 16 >> %TESTINFO_FILE%
echo LineCnt 16 >> %TESTINFO_FILE%
echo SidesCnt 2 >> %TESTINFO_FILE%
echo ModesCnt 2 >> %TESTINFO_FILE%
echo RxCnt 8 >> %TESTINFO_FILE%


%JAVA% -Djavatest.processCommand.inheritEnv=true -jar %CD%\lib\javatest.jar^
 -batch -config %CD%\myConfig.jti -workdir -create -overwrite %CD%\workdir^
 -set simple.jvm %JAVA%^
 -testsuite %CD% -excludeList %CD%\exclude.jtx -runtests^
 -writeReport -type html -filter allTests %CD%\ReportDir

