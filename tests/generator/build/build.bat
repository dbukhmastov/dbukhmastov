@echo off

call setEnv.bat

REM ===== Build test pack =====
call %ANT% -buildfile build.xml
if not "%ERRORLEVEL%" == "0" (
  echo Can't compile testsuite
)
rem pause
