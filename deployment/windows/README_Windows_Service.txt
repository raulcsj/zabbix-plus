Zabbix Plus Framework - Windows Service Installation Guide

This guide explains how to install, manage, and uninstall the Zabbix Plus Framework as a Windows Service.

Prerequisites:
1.  Java Installation:
    *   Ensure that Java Runtime Environment (JRE) or Java Development Kit (JDK) is installed on your system.
    *   The `java.exe` executable must be available in the system's PATH environment variable, OR
    *   The `JAVA_HOME` environment variable must be set to point to your Java installation directory.
        To check, open Command Prompt and type `java -version`. If it runs, Java is likely in your PATH.
        To set JAVA_HOME (example): `setx JAVA_HOME "C:\Program Files\Java\jdk-11.0.x"` (path may vary).

How to Use `install-service.bat`:
1.  **Prepare your Application Directory (`APP_ROOT_DIR`):**
    *   Choose an installation directory for your application (e.g., `C:\ZabbixPlusFramework`). This will be your `APP_ROOT_DIR`.
    *   Copy your application JAR file (e.g., `core-0.0.1-SNAPSHOT.jar`) into this `APP_ROOT_DIR`.
    *   Create a `plugins` subdirectory in `APP_ROOT_DIR` and place your plugin JARs into it.
    *   **Copy Service Scripts:** Copy `zabbix-plus-framework.bat`, `install-service.bat`, and `uninstall-service.bat` from the `deployment/windows` (or wherever you got them) into your `APP_ROOT_DIR`.

2.  Edit `install-service.bat` (now located in your `APP_ROOT_DIR`):
    *   Open `install-service.bat` in a text editor.
    *   Set the `SERVICE_NAME` variable if you want a different name for the service (default: "ZabbixPlusFramework").
    *   Set the `DISPLAY_NAME` variable for a custom display name in the Services console (default: "Zabbix Plus Framework").
    *   **Crucially, set `APP_ROOT_DIR`** to the absolute path of your application directory (e.g., `C:\ZabbixPlusFramework`).
    *   Set `LOG_DIR` to the absolute path where you want service logs to be stored (e.g., `C:\ZabbixPlusFramework\logs`). The script will attempt to create this directory if it doesn't exist.

3.  Customize `zabbix-plus-framework.bat` (now located in your `APP_ROOT_DIR`):
    *   Open `zabbix-plus-framework.bat` in a text editor.
    *   Update `SET APP_JAR=core-0.0.1-SNAPSHOT.jar` to match the actual filename of your Zabbix Plus Framework JAR file.
    *   If needed, configure `JAVA_OPTS` by uncommenting the line (`REM SET JAVA_OPTS=...`) and setting appropriate Java Virtual Machine options (e.g., memory settings like `-Xms512m -Xmx1024m`).

4.  Run as Administrator:
    *   Right-click on `install-service.bat` (the one in your `APP_ROOT_DIR`) and select "Run as administrator".
    *   Follow any prompts on screen.

How to Use `uninstall-service.bat`:
1.  Edit `uninstall-service.bat` (if necessary):
    *   If you changed `SERVICE_NAME` in `install-service.bat`, ensure the `SERVICE_NAME` variable in `uninstall-service.bat` matches.
2.  Run as Administrator:
    *   Right-click on `uninstall-service.bat` and select "Run as administrator".

Checking Service Status:
You can check the status of the service using the Command Prompt:
`sc query %SERVICE_NAME%`
(Replace `%SERVICE_NAME%` with the actual service name if you changed it, e.g., `sc query ZabbixPlusFramework`)

Or use the Windows Services management console:
1.  Press Win + R, type `services.msc`, and press Enter.
2.  Look for the service by its display name (e.g., "Zabbix Plus Framework").

Service Logs:
*   Direct log redirection for batch scripts using `sc.exe` is unreliable. The `zabbix-plus-framework.bat` script itself does not currently redirect its output to a file.
*   The `install-service.bat` script creates a `LOG_DIR` that you specify, but standard output/error from the Java application launched by `zabbix-plus-framework.bat` won't automatically go there.
*   **Recommended for Robust Logging:** For better log management (capturing stdout/stderr from the Java application to files, log rotation, etc.), it is highly recommended to use a service wrapper utility like NSSM (Non-Sucking Service Manager).
    *   NSSM allows you to wrap `zabbix-plus-framework.bat` (or directly the `java` command) and provides robust options for I/O redirection.
    *   NSSM website: https://nssm.cc/
    *   With NSSM, you would configure it to run `zabbix-plus-framework.bat` and specify output and error log files within the `LOG_DIR`.

Troubleshooting:
*   "Access Denied" errors: Ensure you are running the `.bat` scripts as an Administrator.
*   Service fails to start:
    *   Check `JAVA_HOME` / PATH settings.
    *   Verify the `APP_JAR` variable in `zabbix-plus-framework.bat` points to the correct JAR file.
    *   Verify `APP_ROOT_DIR` in `install-service.bat` is correct.
    *   Check the Windows Event Viewer (Application and System logs) for more detailed error messages.
    *   Try running `zabbix-plus-framework.bat` directly from a command prompt (as admin) in the `APP_ROOT_DIR` to see if the Java application starts and if there are any console errors.
NSSM (Non-Sucking Service Manager) - A More Robust Alternative:
For production environments or where more control over the service is needed (especially for logging, process management, and recovery), using NSSM is strongly recommended.

Key advantages of NSSM:
*   Reliable stdout and stderr redirection to log files.
*   Log rotation.
*   Graceful shutdown of applications.
*   More advanced recovery options.

You would typically:
1. Download NSSM.
2. Run `nssm install <YourServiceName>`
3. Configure the path to `zabbix-plus-framework.bat` (or directly `java -jar ...`) and set up I/O redirection to your `LOG_DIR`.

This concludes the basic setup for running the Zabbix Plus Framework as a Windows service.
