@echo off
REM Script to uninstall the Zabbix Plus Framework Windows Service.

REM --- Configuration ---
REM !!! IMPORTANT: Edit this variable if you changed it during installation !!!
SET SERVICE_NAME=ZabbixPlusFramework
REM --- End Configuration ---

echo Uninstalling service %SERVICE_NAME%...

REM Check if running as Administrator
net session >nul 2>&1
if %errorLevel% == 0 (
    echo Administrative permissions confirmed.
) else (
    echo ERROR: This script must be run as Administrator.
    pause
    exit /b 1
)

echo Stopping service %SERVICE_NAME%...
sc stop %SERVICE_NAME%
REM It's okay if stop fails (e.g., service not running), so we don't check errorlevel here rigorously.

echo Deleting service %SERVICE_NAME%...
sc delete %SERVICE_NAME%
if errorlevel 1 (
    echo ERROR: Failed to delete service. It might not exist or other issues occurred.
    pause
    exit /b 1
)

echo Service %SERVICE_NAME% uninstalled successfully.
pause
exit /b 0
