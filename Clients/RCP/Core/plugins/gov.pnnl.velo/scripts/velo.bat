@echo off
set VELOCMD_HOME=%~dp0..
echo %VELOCMD_HOME%
set MAINCLASS=gov.pnnl.velo.core.cmdline.OASCISCommand

REM Replace with dynamic list of jars in VELOCMD_HOME/lib
java -cp %VELOCMD_HOME%/lib/VeloJavaClient.jar;%VELOCMD_HOME%/lib/commons-codec-1.3.jar;%VELOCMD_HOME%/lib/commons-httpclient-3.1.jar;%VELOCMD_HOME%/lib/commons-logging-1.1.jar  %MAINCLASS% %*
