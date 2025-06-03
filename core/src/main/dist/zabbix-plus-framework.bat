@echo off

REM Resolve APP_HOME to the directory where the script is located (go up one level from bin)
set "APP_HOME=%~dp0.."
set "APP_HOME=%APP_HOME:in\=%"
REM Normalize APP_HOME path by resolving any '..'
for %%i in ("%APP_HOME%") do set "APP_HOME=%%~fi"


REM Add default JVM options if needed
set "DEFAULT_JVM_OPTS=-Dfile.encoding=UTF-8"
if "%JAVA_OPTS%"=="" (
    set "JAVA_OPTS=%DEFAULT_JVM_OPTS%"
) else (
    set "JAVA_OPTS=%DEFAULT_JVM_OPTS% %JAVA_OPTS%"
)

REM Construct the classpath
REM Add all JARs from the lib directory
set "CLASSPATH=%APP_HOME%\lib\*"

REM Main class to execute
set "MAIN_CLASS=io.zabbixplus.framework.core.CoreApplication"

REM Change to APP_HOME to resolve relative paths for config/ui if needed by the app
cd /D "%APP_HOME%"

echo Starting Zabbix Plus Framework...
echo APP_HOME: %APP_HOME%
echo CLASSPATH: %CLASSPATH%
echo JAVA_OPTS: %JAVA_OPTS%
echo MAIN_CLASS: %MAIN_CLASS%
echo Arguments: %*

REM Execute the Java application
java %JAVA_OPTS% -cp "%CLASSPATH%" "%MAIN_CLASS%" %*
