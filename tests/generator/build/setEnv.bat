@echo off

if 1%ANT_HOME%==1 (
  set ANT_HOME=C:\ant
)
set ANT=%ANT_HOME%/bin/ant
if not exist %ANT% (
  echo Can not find ANT=%ANT%
  exit -1
)

if 1%JAVA_HOME%==1 (
  set JAVA_HOME=C:\java\jdk8
)

set TEST_COMPONENTS=../

set JAVA=java
set JAVAC=javac
set JAR=jar
if not "%JAVA_HOME%" == "" (
    set JAVA="%JAVA_HOME%/bin/java.exe"
    set JAVAC="%JAVA_HOME%/bin/javac"
    set JAR="%JAVA_HOME%/bin/jar"
)
if not exist %JAVA% (
  echo Can not find JAVA=%JAVA%
  exit -1
)
