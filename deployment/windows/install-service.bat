@echo off
REM Script to install the Zabbix Plus Framework as a Windows Service.

REM --- Configuration ---
REM !!! IMPORTANT: Edit these variables before running the script !!!
SET SERVICE_NAME=ZabbixPlusFramework
SET DISPLAY_NAME="Zabbix Plus Framework"
REM Set this to the ABSOLUTE path where zabbix-plus-framework.bat and the JAR are located.
SET APP_ROOT_DIR=C:\path\to\your\application
REM Set this to the ABSOLUTE path for service logs.
SET LOG_DIR=C:\path\to\your\application\logs
REM --- End Configuration ---

echo Installing service %SERVICE_NAME%...

REM Check if running as Administrator
net session >nul 2>&1
if %errorLevel% == 0 (
    echo Administrative permissions confirmed.
) else (
    echo ERROR: This script must be run as Administrator.
    pause
    exit /b 1
)

REM Validate APP_ROOT_DIR - check if zabbix-plus-framework.bat exists
if not exist "%APP_ROOT_DIR%\zabbix-plus-framework.bat" (
    echo ERROR: zabbix-plus-framework.bat not found in %APP_ROOT_DIR%.
    echo Please set APP_ROOT_DIR correctly in this script.
    pause
    exit /b 1
)

REM Create log directory if it doesn't exist
if not exist "%LOG_DIR%" (
    echo Creating log directory: %LOG_DIR%
    mkdir "%LOG_DIR%"
    if errorlevel 1 (
        echo ERROR: Could not create log directory: %LOG_DIR%.
        pause
        exit /b 1
    )
)

REM Construct the full path to the batch file
SET BIN_PATH="%APP_ROOT_DIR%\zabbix-plus-framework.bat"

echo Creating service with binPath: %BIN_PATH%
sc create %SERVICE_NAME% binPath= "%BIN_PATH%" DisplayName= %DISPLAY_NAME% start= auto obj= LocalSystem
if errorlevel 1 (
    echo ERROR: Failed to create service.
    pause
    exit /b 1
)

echo Setting service description...
sc description %SERVICE_NAME% "Runs the Zabbix Plus Framework application."

echo Configuring service recovery options...
sc failure %SERVICE_NAME% reset= 86400 actions= restart/60000/restart/60000/restart/60000

echo.
echo --- Important Notes ---
echo 1. Ensure Java is installed and 'java.exe' is in your system PATH or JAVA_HOME is set.
echo 2. Customize 'zabbix-plus-framework.bat' for JAVA_OPTS and the correct JAR file name if needed.
echo 3. Log redirection for batch file services with 'sc.exe' is limited.
echo    For robust logging, consider using a service wrapper like NSSM (Non-Sucking Service Manager).
echo    NSSM can redirect stdout/stderr of your script to files in %LOG_DIR%.
echo.

REM Attempt to configure basic log redirection (may not work reliably for .bat files)
REM sc create %SERVICE_NAME% ... other options ... AppExit "cmd.exe /c \"set LOG_FILE=%LOG_DIR%\%SERVICE_NAME%-stdout.log && set ERR_FILE=%LOG_DIR%\%SERVICE_NAME%-stderr.log && %BIN_PATH% >> %LOG_FILE% 2>> %ERR_FILE%\""
REM The above is complex and often fails. We will rely on users using NSSM or similar for proper logging for now.

echo To start the service, run:
echo sc start %SERVICE_NAME%
echo or use the Services management console (services.msc).

echo.
echo Service %SERVICE_NAME% installed successfully.
pause
exit /b 0
