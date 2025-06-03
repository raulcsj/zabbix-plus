@echo off
echo Starting application...

REM Placeholder for JAVA_OPTS. User should configure this as needed.
REM SET JAVA_OPTS=-Xms256m -Xmx512m

REM Placeholder for the application JAR path. User should configure this.
SET APP_JAR=core-0.0.1-SNAPSHOT.jar

REM Navigate to the application's root directory.
REM This script assumes it is located in the application's root directory.
cd /d "%~dp0"

REM Execute the Java application.
java %JAVA_OPTS% -jar %APP_JAR%

REM Basic error handling.
if errorlevel 1 (
    echo Application failed to start.
    pause
)
