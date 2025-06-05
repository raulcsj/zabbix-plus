# Zabbix Plus Framework

## 1. Introduction

The Zabbix Plus Framework is a Spring Boot-based application designed to extend the capabilities of Zabbix (or related monitoring solutions) through a robust plugin architecture. It allows developers to create and integrate custom functionalities, including backend logic and user interface components, seamlessly into a central platform.

**Key Features:**

*   **Extensible Plugin Architecture:** Dynamically load and manage plugins packaged as JAR files.
*   **Backend & Frontend Integration:** Plugins can contribute both backend services (Java/Kotlin) and frontend UI elements (Vue.js).
*   **Core Services:** Provides common services and infrastructure that plugins can leverage.
*   **Dynamic UI Composition:** The main UI can dynamically render navigation items and components provided by active plugins.
*   **Centralized Configuration:** Plugins can have their own configuration files.

## 2. Framework Architecture

The framework consists of several key modules:

*   **`core`:** The main Spring Boot application. It handles plugin loading, lifecycle management, provides core services, and exposes APIs for the frontend.
*   **`plugin-api`:** Defines the interfaces (`Plugin`, `UiPlugin`, `PluginContext`) that all plugins must implement. This ensures a consistent contract between the core framework and plugins.
*   **Plugins (e.g., `example-plugin`):** Individual modules containing custom logic and UI components. They are packaged as JARs and placed in a `plugins` directory.
*   **`main-ui`:** A Vue.js single-page application that serves as the primary user interface. It dynamically adapts to loaded plugins by fetching metadata and rendering appropriate UI elements.
*   **`database`:** (Conceptual or actual module for database schema management). The framework uses a database, interacted with via Ebean ORM in the `core` module using entities like `ExampleEntity`.

**Plugin Loading Mechanism:**
The `PluginService` in the `core` module is responsible for scanning a designated `plugins` directory (default: `./plugins`) for JAR files. It uses Java's `ServiceLoader` mechanism to discover and instantiate `Plugin` implementations within these JARs. Each plugin is loaded in its own `URLClassLoader` for some degree of isolation.

**Request Flow (Simplified):**
*   **UI Interaction:** User interacts with the `main-ui` (Vue.js application).
*   **Frontend to Backend API:** `main-ui` makes API calls to backend controllers.
    *   For general UI metadata: `GET /api/ui/plugin-metadata` (handled by `PluginUiController` in `core`).
    *   For plugin-specific actions: e.g., `GET /api/plugins/simpleexampleplugin/data` (handled by controllers within the specific plugin, like `ExamplePluginApiController`).
*   **Core/Plugin Logic:** Backend controllers in `core` or plugins process these requests, potentially interacting with core services (like `ExampleTableService`) or plugin-specific logic.

### Plugin Data Flow Example

This example outlines the typical sequence of events when a user interacts with a plugin's UI, leading to data fetching and display:

1.  **User Interaction (Frontend):** The user clicks a button or performs an action within a Vue component that is part of a loaded plugin's UI (e.g., inside `MyPluginComponent.vue` rendered by `PluginPlaceholderView`).
2.  **Plugin API Call (Frontend):** The Vue component makes an asynchronous request (e.g., using `axios` or `fetch`) to a plugin-specific backend API endpoint. This endpoint is typically namespaced for the plugin, for example: `POST /api/plugins/myplugin/data` or `GET /api/plugins/myplugin/items/{itemId}`.
3.  **Request Routing to Plugin Controller (Backend):** The Spring Boot application receives the request. Based on the URL pattern (e.g., `/api/plugins/myplugin/**`), it routes the request to a `@RestController` defined *within* the specific plugin's JAR file (e.g., `MyPluginApiController.java`).
4.  **Plugin Controller Logic (Backend):** The plugin's controller method handles the request. It may:
    *   Interact with a plugin-specific service (e.g., `MyPluginService.java` also within the plugin).
    *   Or, interact with a core service provided by the framework (e.g., `ExampleTableService` if the plugin needs to access shared data).
5.  **Service Layer (Backend):** The service (plugin-specific or core) executes the business logic. This might involve:
    *   Fetching data from a database (e.g., using Ebean ORM via `ExampleTableService` with entities like `ExampleEntity`, or the plugin's own data access layer).
    *   Calling external APIs.
    *   Performing calculations or data transformations.
6.  **Data Return Flow (Backend to Frontend):**
    *   The service returns data to the plugin controller.
    *   The plugin controller constructs an HTTP response (e.g., a JSON payload) and returns it.
    *   Spring Boot sends this response back to the `main-ui`.
7.  **UI Update (Frontend):** The plugin's Vue component (which made the initial call) receives the response. It then updates its state, causing the UI to re-render and display the new data or the results of the action.

## 3. Core Module (`core`)

The `core` module is the heart of the Zabbix Plus Framework.

*   **Responsibilities:**
    *   Application entry point (`CoreApplication.java`).
    *   Managing the lifecycle of plugins via `PluginService`.
    *   Providing shared services accessible to plugins.
    *   Serving the main UI and its associated APIs.
*   **Key Components:**
    *   **`PluginService.java`:**
        *   Loads plugin JARs from the `plugins` directory at startup. It uses a separate `URLClassLoader` for each plugin, providing a degree of classloader isolation between plugins and between plugins and the core application.
        *   Manages plugin lifecycle (`load`, `init`, `unload`).
        *   Provides access to loaded plugin instances.
        *   During the `init` phase of each plugin, a `PluginContext` is created. This context holds a reference to the main `ApplicationContext` (allowing plugins to access core Spring beans) and the plugin's specific configuration (parsed from its `config.yml` or `config.yaml` file, if present). This `PluginContext` is then passed to the plugin's `init` method.
    *   **`PluginContext.java`:** (Defined in `plugin-api`, but used by `core`)
        *   Passed to each plugin during its `init` phase.
        *   Provides access to the main `ApplicationContext` (allowing plugins to retrieve core beans) and the plugin's specific configuration (from its `config.yml`).
    *   **Component Scanning (`CoreApplication.java`):**
        *   `CoreApplication` uses `@SpringBootApplication(scanBasePackages = {"io.zabbixplus.framework"})`.
        *   It's crucial that this `scanBasePackages` is broad enough to discover Spring components (`@Service`, `@Component`, `@RestController`, etc.) in both the `core` module and any loaded plugins (e.g., `io.zabbixplus.framework.exampleplugin.controller`).
*   **Core Services:**
    *   **`ExampleTableService.java`:** This is an *example* of a core service that plugins *can* (but are not required to) use. It demonstrates how a service within the `core` module can provide common functionalities, such as database interaction.
        *   It is implemented as a Spring `@Service`.
        *   It uses Ebean ORM for database interactions, with `ExampleEntity.java` as an example entity. This showcases a way to manage data persistence using an ORM.
        *   Plugins can obtain an instance of this service (or other core services) via the `ApplicationContext` provided in their `PluginContext`.
        *   It provides methods like `createRecord(String name)` (which creates an `ExampleEntity`) and `getRecords()` (which returns a list of `ExampleEntity` objects) as examples of database operations.
*   **Backend API for UI Plugins:**
    *   **`PluginUiController.java`:**
        *   **Endpoint:** `GET /api/ui/plugin-metadata`
        *   **Purpose:** Provides a consolidated list of UI-related information from all active `UiPlugin` instances to the `main-ui`. This allows the frontend to dynamically build its navigation menu and know which components to render for each plugin.
        *   **Response Structure:** Returns a list of `PluginClientInfo` objects, where each object contains:
            *   `name` (String): The plugin's name.
            *   `uiMetadata` (Map<String, Object>): Metadata from `UiPlugin.getUiMetadata()` (e.g., `mainComponent`, `bundleUrl`).
            *   `navigationItems` (List<NavigationItem>): Navigation links from `UiPlugin.getNavigationItems()`.

## 4. Plugin API (`plugin-api`)

This module defines the essential contracts for all plugins.

*   **Purpose:** To ensure loose coupling between the `core` framework and individual plugins, allowing plugins to be developed and deployed independently.
*   **Key Interfaces & Classes:**
    *   **`Plugin.java`:** The base interface for all plugins.
        *   `String getName()`: Returns the unique name of the plugin.
        *   `String getDescription()`: A brief description of the plugin.
        *   `void load()`: Called when the plugin is initially loaded. For basic setup.
        *   `void init(PluginContext context)`: Called after `load()`. Receives a `PluginContext` object. Ideal for more complex initialization, accessing core services, or loading plugin configuration.
        *   `void unload()`: Called when the plugin is being unloaded. For resource cleanup.
    *   **`UiPlugin.java`:** Extends `Plugin`. For plugins that contribute to the user interface.
        *   `String getVueComponentName()`: (Potentially deprecated in favor of `mainComponent` in `getUiMetadata`) Returns the name of the main Vue component for this plugin.
        *   `List<NavigationItem> getNavigationItems()`: Returns a list of navigation items to be added to the `main-ui`'s menu.
        *   `Map<String, Object> getUiMetadata()`: Returns a map of UI-specific metadata. Essential keys include:
            *   `mainComponent` (String): The name of the primary Vue component to render for this plugin's UI (e.g., "ExamplePluginDashboard").
            *   `pluginName` (String): The plugin's name.
            *   `bundleUrl` (String, conceptual for true micro-frontends): The URL to the plugin's frontend JavaScript bundle.
            *   `description` (String): Plugin description.
    *   **`PluginContext.java`:**
        *   `ApplicationContext getApplicationContext()`: Provides the Spring `ApplicationContext` of the main `core` application.
        *   `Map<String, Object> getConfiguration()`: Provides the plugin's specific configuration, parsed from its `config.yml`.
    *   **`NavigationItem.java`:** A simple DTO class representing a navigation link.
        *   `name` (String): Display text for the link.
        *   `path` (String): The Vue router path (e.g., `/ui/plugin/MyPluginName`).
        *   `icon` (String, optional): CSS class for an icon (e.g., `fas fa-home`).

## 5. Plugin Development Guide

Follow these steps to create and integrate a new plugin.

### Creating a New Plugin

1.  **Project Setup:**
    *   Create a new Java/Kotlin project, typically as a Gradle module alongside `core`, `plugin-api`, etc.
    *   Add `plugin-api` as a dependency. Also include Spring Boot dependencies if creating Spring components like controllers or services within the plugin.
2.  **Implement Interfaces:**
    *   Create a class that implements `io.zabbixplus.framework.plugin.Plugin`.
    *   If the plugin has a UI, also implement `io.zabbixplus.framework.plugin.UiPlugin`.
3.  **ServiceLoader Declaration:**
    *   In your plugin's `src/main/resources/META-INF/services/` directory, create a file named `io.zabbixplus.framework.plugin.Plugin`.
    *   Inside this file, write the fully qualified name of your class that implements the `Plugin` interface (e.g., `com.example.myplugin.MyPluginImplementation`).

### Plugin Configuration

1.  **Create `config.yml`:**
    *   In your plugin's `src/main/resources/` directory, create a `config.yml` or `config.yaml` file.
    *   Define plugin-specific settings here using YAML syntax. Example:
        ```yaml
        featureFlags:
          newFeatureEnabled: true
        apiSettings:
          url: "https://api.example.com/data"
          timeoutSeconds: 30
        ```
2.  **Access Configuration:**
    *   In your plugin's `init(PluginContext context)` method, you can retrieve the parsed configuration:
        ```java
        Map<String, Object> config = context.getConfiguration();
        Map<String, Object> apiSettings = (Map<String, Object>) config.get("apiSettings");
        String apiUrl = (String) apiSettings.get("url");
        ```

### Backend Logic & Services

1.  **Accessing Core Services:**
    *   If your plugin needs to use services from the `core` module (e.g., `ExampleTableService`), get the `ApplicationContext` from the `PluginContext` in your `init` method.
    *   Then, retrieve beans using `applicationContext.getBean(MyCoreService.class)`.
        ```java
        // In your Plugin implementation's init method:
        this.applicationContext = context.getApplicationContext();
        if (this.applicationContext != null) {
            try {
                this.exampleTableService = this.applicationContext.getBean(ExampleTableService.class);
            } catch (NoSuchBeanDefinitionException e) {
                logger.error("Could not find ExampleTableService bean.", e);
            }
        }
        ```
2.  **Creating Plugin-Specific APIs:**
    *   You can define Spring `@RestController` classes within your plugin to expose plugin-specific APIs.
    *   Place these controllers in a sub-package (e.g., `com.example.myplugin.controller`).
    *   **Important:** Ensure these controllers are covered by the main application's component scan. The recommended `scanBasePackages = {"io.zabbixplus.framework"}` in `CoreApplication.java` should cover controllers in packages like `io.zabbixplus.framework.yourplugin.controller`.

### Frontend Components (Vue.js)

1.  **Store Vue Files:**
    *   Develop your Vue components (`.vue` files) and store them within your plugin's source tree, for example, in `my-plugin/src/main/frontend/`.
2.  **Current UI Integration Strategy ("Option A"):**
    *   The framework currently supports an "Option A" integration for plugin Vue components. This means the `main-ui` application needs to be aware of and bundle these components.
    *   In your `UiPlugin` implementation, the `getUiMetadata()` method must return a `mainComponent` key. The value is the String name under which the component will be registered in `main-ui` (e.g., `"MyPluginMainViewer"`).
        ```java
        // In your UiPlugin implementation
        @Override
        public Map<String, Object> getUiMetadata() {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("mainComponent", "MyPluginMainViewer");
            // ... other metadata
            return metadata;
        }
        ```
    *   **Manual Steps for `main-ui` Developer:**
        1.  **Copy/Link Component:** The Vue component file (e.g., `MyPluginMainViewer.vue`) from the plugin's `frontend` directory must be copied or linked into a location accessible by `main-ui`'s build process (e.g., `main-ui/src/main/frontend/src/components/plugins/MyPluginMainViewer.vue`).
        2.  **Global Registration:** The component must be globally registered in `main-ui`'s entry point (`main-ui/src/main/frontend/src/main.js`) using the exact name specified in `mainComponent`:
            ```javascript
            // In main-ui/src/main/frontend/src/main.js
            import MyPluginMainViewer from './components/plugins/MyPluginMainViewer.vue'; // Adjust path
            // ...
            app.component("MyPluginMainViewer", MyPluginMainViewer);
            ```

### Packaging & Deployment

1.  **Build JAR:** Build your plugin project to produce a JAR file (e.g., using `./gradlew build`).
2.  **Deploy:** Copy the generated plugin JAR file into the `plugins` directory of your Zabbix Plus Framework main application deployment. The `core` application will load it on startup.

## 6. Packaging and Deployment

This section outlines the process for packaging the Zabbix Plus Framework application and deploying it as a service on various operating systems.

### Prerequisites (General)

Before you begin, ensure you have the following installed:

*   **Java Development Kit (JDK):** Version 17 or higher.
*   **Gradle:** The project uses the Gradle wrapper (`gradlew` or `gradlew.bat`), so a separate Gradle installation is typically not required. If you encounter issues, ensure you have a compatible Gradle version.
*   **Administrative/Root Access:** Required for installing services on the respective operating systems.

Specific operating system sections below may have additional prerequisites.

### Packaging the Application

The core application is packaged as an executable JAR file.

1.  **Build the Core Application JAR:**
    Navigate to the root project directory and run the following command:
    ```bash
    ./gradlew :core:bootJar
    ```
    Alternatively, you can run `./gradlew :core:build`.
    The resulting JAR file will typically be found in `core/build/libs/` and named something like `core-<version>.jar` (e.g., `core-0.0.1-SNAPSHOT.jar`). This is the main application JAR.

2.  **Plugins:**
    Plugins are developed and built as separate JAR files. Once built, these plugin JARs should be placed into a `plugins` directory located in the application's installation/runtime directory. The framework will load them from there. Refer to Section 5 ("Plugin Development Guide") for details on creating plugins.

### Deploying as a Service

#### Database Setup (Important for First Run)

Before deploying the service for the first time, ensure your database is set up and the application can connect to it.
*   **Create Database:** Create a database (e.g., `zabbixplus_dev`).
*   **Configure Connection:** Update `core/src/main/resources/application.properties` (or its YAML equivalent, or use environment variables) with your database connection details:
    *   `spring.datasource.url`
    *   `spring.datasource.username`
    *   `spring.datasource.password`
*   **Schema:** The `example_table` (with columns `id` SERIAL PRIMARY KEY, `name` VARCHAR(255), `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP) needs to exist for the example plugin. For production, a proper database migration tool like Liquibase or Flyway is recommended to manage your schema. This setup is typically done once before the first deployment.

#### Windows

Comprehensive instructions and scripts for running the application as a Windows service are provided in the `deployment/windows/` directory.

*   **Key Files:**
    *   `deployment/windows/README_Windows_Service.txt`: Detailed instructions.
    *   `deployment/windows/zabbix-plus-framework.bat`: Script to launch the Java application (customize JAR name, JAVA_OPTS here).
    *   `deployment/windows/install-service.bat`: Script to install the service using `sc.exe`.
    *   `deployment/windows/uninstall-service.bat`: Script to uninstall the service.

*   **General Steps:**
    1.  **Package & Prepare:** Build the `core-<version>.jar`. Create an installation directory (e.g., `C:\ZabbixPlusFramework`).
        *   Copy `core-<version>.jar` to this directory.
        *   Create a `plugins` subdirectory and place your plugin JARs into it.
        *   Copy all files from the `deployment/windows/` directory into your installation directory.
    2.  **Customize Scripts:**
        *   Edit `zabbix-plus-framework.bat` to set the correct `APP_JAR` (your `core-<version>.jar` filename) and any `JAVA_OPTS`.
        *   Edit `install-service.bat` to set `APP_ROOT_DIR` to your installation directory path, and customize `SERVICE_NAME`, `DISPLAY_NAME`, and `LOG_DIR` as needed.
    3.  **Install Service:** Run `install-service.bat` as an Administrator.
    4.  **Manage Service:** Use `sc start/stop/query %SERVICE_NAME%` or the Services management console (`services.msc`).

*   **Logging:** The `README_Windows_Service.txt` strongly recommends using a service wrapper like **NSSM (Non-Sucking Service Manager)** (https://nssm.cc/) for robust log redirection and management, as `sc.exe` has limitations with batch scripts.

#### Linux (systemd)

The framework can be run as a systemd service on Linux distributions.

*   **Key File:**
    *   `deployment/systemd/zabbix-plus-framework.service`: A template systemd service unit file.

*   **Instructions:**
    1.  **Package & Prepare:**
        *   Build the `core-<version>.jar`.
        *   Create an installation directory (e.g., `/opt/zabbix-plus-framework`).
        *   Place the `core-<version>.jar` into this directory.
        *   Create `plugins` and `logs` subdirectories within the installation directory:
            ```bash
            sudo mkdir -p /opt/zabbix-plus-framework/plugins
            sudo mkdir -p /opt/zabbix-plus-framework/logs
            ```
        *   Copy your plugin JARs into `/opt/zabbix-plus-framework/plugins/`.
        *   Ensure your database is set up as per "Database Setup" above.
    2.  **Create Startup Script:**
        Create a startup script, for example, at `/opt/zabbix-plus-framework/bin/zabbix-plus-framework.sh`:
        ```bash
        #!/bin/bash
        APP_DIR="$(cd "$(dirname "$0")/.." && pwd)" # Assumes script is in a 'bin' subdirectory
        JAR_NAME="core-0.0.1-SNAPSHOT.jar" # !!! CHANGE THIS to your actual core JAR filename !!!
        JAVA_OPTS="-Xmx512m -Dfile.encoding=UTF-8" # Add other Java options, ensure UTF-8
        LOG_DIR="${APP_DIR}/logs"
        PID_FILE="${APP_DIR}/app.pid"

        # Ensure the user running the service has write access to APP_DIR and LOG_DIR
        mkdir -p "${LOG_DIR}"
        cd "${APP_DIR}"

        echo "Starting Zabbix Plus Framework..." >> "${LOG_DIR}/stdout.log"
        nohup java $JAVA_OPTS -jar "${JAR_NAME}" >> "${LOG_DIR}/stdout.log" 2>> "${LOG_DIR}/stderr.log" &
        echo $! > "${PID_FILE}"
        echo "Zabbix Plus Framework started with PID $(cat ${PID_FILE}). Logs in ${LOG_DIR}" >> "${LOG_DIR}/stdout.log"
        ```
        *   **Important:**
            *   Modify `JAR_NAME` in the script to match your application's JAR file name.
            *   Set appropriate `JAVA_OPTS`.
            *   Make the script executable: `sudo chmod +x /opt/zabbix-plus-framework/bin/zabbix-plus-framework.sh`.
    3.  **Configure systemd Service File:**
        *   Copy the provided `deployment/systemd/zabbix-plus-framework.service` file to `/etc/systemd/system/zabbix-plus-framework.service`.
            ```bash
            sudo cp deployment/systemd/zabbix-plus-framework.service /etc/systemd/system/zabbix-plus-framework.service
            ```
        *   Edit `/etc/systemd/system/zabbix-plus-framework.service`:
            *   Set `User` and `Group` to an appropriate user/group (e.g., a dedicated `zabbixplus` user, or `youruser`). This user needs write access to the installation and log directories.
            *   Update `WorkingDirectory` to your installation directory (e.g., `/opt/zabbix-plus-framework`).
            *   Update `ExecStart` to point to your startup script: `ExecStart=/opt/zabbix-plus-framework/bin/zabbix-plus-framework.sh`.
            *   Update `PIDFile` to point to the `app.pid` created by the script: `PIDFile=/opt/zabbix-plus-framework/app.pid`.
            *   Remove or comment out the `Environment` lines for `JAVA_OPTS` and `APP_JAR_PATH` if you are defining these in the `.sh` script.
    4.  **Manage Service:**
        *   Reload systemd configuration: `sudo systemctl daemon-reload`
        *   Enable the service to start on boot: `sudo systemctl enable zabbix-plus-framework`
        *   Start the service: `sudo systemctl start zabbix-plus-framework`
        *   Check status: `sudo systemctl status zabbix-plus-framework`
        *   Stop the service: `sudo systemctl stop zabbix-plus-framework`
    5.  **View Logs:**
        *   Using journalctl: `sudo journalctl -u zabbix-plus-framework`
        *   Directly from log files: Check `stdout.log` and `stderr.log` in the `logs` directory you configured (e.g., `/opt/zabbix-plus-framework/logs/`).

#### macOS (launchd)

The framework can be run as a launchd service on macOS.

*   **Key File:**
    *   `deployment/launchd/io.zabbixplus.framework.plist`: A template launchd property list file.

*   **Instructions:**
    1.  **Package & Prepare:**
        *   Build the `core-<version>.jar`.
        *   Create an installation directory. Common locations:
            *   System-wide: `/opt/zabbix-plus-framework`
            *   User-specific: `~/Library/Application Support/ZabbixPlusFramework`
            (Let's use `/opt/zabbix-plus-framework` for this example, adjust as needed).
        *   Place `core-<version>.jar` into this directory.
        *   Create `plugins` and `logs` subdirectories:
            ```bash
            sudo mkdir -p /opt/zabbix-plus-framework/plugins # Use sudo if system-wide
            sudo mkdir -p /opt/zabbix-plus-framework/logs   # Use sudo if system-wide
            # If user-specific, omit sudo and adjust path e.g. mkdir -p ~/Library/Application\ Support/ZabbixPlusFramework/plugins
            ```
        *   Copy plugin JARs into the `plugins` directory.
        *   Ensure your database is set up as per "Database Setup" above.
    2.  **Create Startup Script:**
        Create a startup script, e.g., `/opt/zabbix-plus-framework/bin/zabbix-plus-framework.sh` (similar to the Linux one):
        ```bash
        #!/bin/bash
        APP_DIR="$(cd "$(dirname "$0")/.." && pwd)" # Assumes script is in a 'bin' subdirectory
        JAR_NAME="core-0.0.1-SNAPSHOT.jar" # !!! CHANGE THIS to your actual core JAR filename !!!
        JAVA_OPTS="-Xmx512m -Dfile.encoding=UTF-8" # Add other Java options, ensure UTF-8
        # LOG_DIR is not strictly needed here if using .plist for redirection, but good for consistency
        # LOG_DIR="${APP_DIR}/logs"
        # mkdir -p "${LOG_DIR}" # Ensure this directory exists if .plist redirects here

        cd "${APP_DIR}"

        # For launchd, direct output is often better than backgrounding with &
        # The .plist file will handle stdout/stderr redirection.
        exec java $JAVA_OPTS -jar "${JAR_NAME}"
        ```
        *   **Important:**
            *   Modify `JAR_NAME` to your application's JAR file.
            *   Set `JAVA_OPTS`.
            *   Make the script executable: `sudo chmod +x /opt/zabbix-plus-framework/bin/zabbix-plus-framework.sh` (use `sudo` if in `/opt`).
    3.  **Configure launchd `.plist` File:**
        *   Decide service scope:
            *   User agent (runs when user logs in): `~/Library/LaunchAgents/io.zabbixplus.framework.plist`
            *   System daemon (runs on boot, requires root): `/Library/LaunchDaemons/io.zabbixplus.framework.plist`
        *   Copy `deployment/launchd/io.zabbixplus.framework.plist` to the chosen location. Example for system daemon:
            ```bash
            sudo cp deployment/launchd/io.zabbixplus.framework.plist /Library/LaunchDaemons/io.zabbixplus.framework.plist
            ```
        *   Edit the `.plist` file (e.g., `sudo nano /Library/LaunchDaemons/io.zabbixplus.framework.plist`):
            *   Update `Label` to be unique (e.g., `io.zabbixplus.framework`).
            *   Modify `ProgramArguments`: The first string should be the full path to your startup script (e.g.,`/opt/zabbix-plus-framework/bin/zabbix-plus-framework.sh`).
            *   Set `WorkingDirectory` to your installation directory (e.g., `/opt/zabbix-plus-framework`).
            *   Set `StandardOutPath` to your desired stdout log file (e.g., `/opt/zabbix-plus-framework/logs/stdout.log`).
            *   Set `StandardErrorPath` to your desired stderr log file (e.g., `/opt/zabbix-plus-framework/logs/stderr.log`).
            *   If running as a system daemon (`/Library/LaunchDaemons`), you might need to add a `UserName` key specifying the user to run as (e.g., `<key>UserName</key><string>youruser</string>`). This user needs write access to the installation and log directories.
            *   Ensure `KeepAlive` is set to `true` or as desired for automatic restarts.
    4.  **Manage Service:**
        *   **Load:**
            *   LaunchAgent: `launchctl load ~/Library/LaunchAgents/io.zabbixplus.framework.plist`
            *   LaunchDaemon: `sudo launchctl load /Library/LaunchDaemons/io.zabbixplus.framework.plist`
        *   **Start (often automatic on load, but can be explicit):**
            *   `launchctl start io.zabbixplus.framework` (use `sudo` if it's a LaunchDaemon and you're not root)
        *   **Stop:**
            *   `launchctl stop io.zabbixplus.framework` (use `sudo` if LaunchDaemon; may not stop if KeepAlive is aggressive)
        *   **Unload (to prevent automatic startup):**
            *   LaunchAgent: `launchctl unload ~/Library/LaunchAgents/io.zabbixplus.framework.plist`
            *   LaunchDaemon: `sudo launchctl unload /Library/LaunchDaemons/io.zabbixplus.framework.plist`
    5.  **View Logs:** Check the files specified in `StandardOutPath` and `StandardErrorPath`. Console.app can also show launchd logs.

### Configuration Notes (General)

*   **Application Configuration:** The primary application configuration (e.g., database connections, server port) is typically managed via `application.properties` or `application.yml`. This file can be placed in a `config` directory alongside the `core-<version>.jar`, or directly next to the JAR. For service deployments, it's common to use an externalized configuration file. You can specify its location using the `spring.config.location` Java system property in `JAVA_OPTS`:
    ```
    -Dspring.config.location=file:/path/to/your/application.properties
    ```
*   **Plugin Configuration:** Each plugin can have its own `config.yml` within its JAR resources, accessible via `PluginContext`. See Section 5 ("Plugin Development Guide").
*   **Plugins Directory:** Ensure the `plugins` directory is present in the `WorkingDirectory` of the application and contains all your plugin JARs.

## 7. Main UI (`main-ui`) Integration

The `main-ui` is a Vue.js application designed to provide a dynamic frontend for the framework and its plugins.

*   **Key Services:**
    *   **`PluginRegistryService.js` (`src/services/PluginRegistryService.js`):**
        *   Responsible for fetching plugin UI data from the backend API endpoint `GET /api/ui/plugin-metadata`.
        *   Caches the fetched data.
        *   Provides reactive Vue `ref`s and `computed` properties for:
            *   `plugins`: List of all plugin client information.
            *   `navItems`: Aggregated list of navigation items from all UI plugins.
            *   `pluginMetadataMap`: A map for easy lookup of plugin data by name.
            *   `isLoading`, `error`: For managing API call state.
        *   **Initialization:** Exposes an `initialize()` function that should be called when the Vue app starts (e.g., in `main.js` or root `App.vue`'s `onMounted` hook) to load plugin data.
*   **Dynamic Features:**
    *   **Navigation Menu (`App.vue`):**
        *   Uses `PluginRegistryService.navItems` to dynamically render `<router-link>` components in the main navigation bar.
        *   The menu automatically reflects the navigation items contributed by active `UiPlugin`s.
    *   **Plugin Component Rendering (`PluginPlaceholderView.vue`):**
        *   Routed via `/ui/plugin/:pluginName`.
        *   Uses `PluginRegistryService` to get the `uiMetadata` for the specified `pluginName`.
        *   Dynamically renders the plugin's main component using `<component :is="pluginUiMeta.mainComponent">`.
        *   **Crucially relies on the "Option A" integration strategy:** The Vue component name obtained from `mainComponent` metadata (e.g., "ExamplePluginDashboard") must correspond to a component globally registered in `main-ui` (see Plugin Development Guide for details).
*   **Developer Workflow (Adding a new plugin's UI to `main-ui`):**
    *   When a new plugin with a Vue UI is developed, the `main-ui` developer needs to:
        1.  Obtain the plugin's Vue component file(s) (e.g., `ExamplePluginDashboard.vue`).
        2.  Place them into `main-ui/src/main/frontend/src/components/plugins/`.
        3.  Globally register the component(s) in `main-ui/src/main/frontend/src/main.js` with the name(s) that the plugin will provide in its `uiMetadata.mainComponent`.

## 8. Example Plugin (`example-plugin`)

The `example-plugin` serves as a working demonstration of the framework's plugin capabilities.

*   **Purpose:** Illustrates how to create a plugin with both frontend and backend functionalities.
*   **Key Features Implemented:**
    *   **`SimpleExamplePlugin.java`:** Implements `UiPlugin`, provides metadata, and includes logic for database interaction via `ExampleTableService`.
    *   **`ExamplePluginDashboard.vue`:** A Vue component located in `example-plugin/src/main/frontend/`. This is the UI for the plugin, allowing users to view and add data.
        *   *Integration Note:* For this component to be usable, it must be manually copied to `main-ui/src/main/frontend/src/components/plugins/` and registered in `main-ui/src/main/frontend/src/main.js` as "ExamplePluginDashboard".
    *   **`ExamplePluginApiController.java`:** A `@RestController` within the plugin that exposes API endpoints (`GET` and `POST` at `/api/plugins/simpleexampleplugin/data`) for the `ExamplePluginDashboard.vue` to interact with.
    *   **`config.yml`:** Includes a sample configuration file (`example-plugin/src/main/resources/config.yml`) demonstrating the plugin configuration feature.
*   **Functionality:**
    *   Provides a UI page ("Example Dashboard") accessible from the main navigation.
    *   The UI allows fetching a list of records from the database (via its backend API).
    *   The UI allows adding new records to the database (via its backend API). The backend currently only processes the "name" field.

## 9. Developer Setup and Running

This section provides guidance for developers looking to set up a local development environment. For deployment as a service, see Section 6.

### Prerequisites (Development)
*   **Java Development Kit (JDK):** Version 17 or higher.
*   **Node.js:** Version 18 or higher, with npm or yarn (for `main-ui` development).
*   **Gradle:** The project uses the Gradle wrapper.
*   **PostgreSQL Database:** Or adapt `ExampleTableService` (which uses Ebean and `ExampleEntity`) and configuration for another SQL database. The initial database setup (creating the database and tables, e.g., `example_table`) is required.

### Backend (`core` and plugins) - Development
1.  **Clone Repository.**
2.  **Database Setup:** As described in Section 6 under "Database Setup (Important for First Run)".
3.  **Build:** `./gradlew clean build` (builds all modules).
4.  **Run `core` Application (IDE or Command Line):**
    *   From your IDE: Run the `CoreApplication.java` main method.
    *   Command Line: `java -jar core/build/libs/core-<version>.jar`.
5.  **Plugins:** Ensure plugin JARs are in the `./plugins` directory relative to where `core` is run, or that your IDE includes them in the classpath if running directly.

### Frontend (`main-ui`) - Development
1.  **Navigate:** `cd main-ui/src/main/frontend/`
2.  **Install Dependencies:** `npm install`
3.  **Integrate Plugin Vue Components:** Follow "Option A" as per Section 5 ("Plugin Development Guide") and Section 7 ("Main UI (`main-ui`) Integration"). This involves copying/linking Vue files to `main-ui` and registering them.
4.  **Run Dev Server:** `npm run serve`. This usually starts on `http://localhost:8081`.
    *   Configure `vue.config.js` for proxying API requests to the backend (typically `http://localhost:8080`).
5.  **Production Build for UI:** `npm run build`. Output goes to `main-ui/src/main/frontend/dist/`. For the `core` app to serve these, they should be copied to `core/src/main/resources/static/` before packaging the `core` JAR.

## 10. Future Enhancements (Micro-Frontend Vision)

The current "Option A" for UI integration (where `main-ui` bundles plugin Vue components) provides dynamic rendering based on metadata but requires `main-ui` to be aware of these components at build time.

Future enhancements could move towards a true micro-frontend architecture ("Option B"):

*   **Dynamic Bundle Loading:** Plugins would build their Vue UIs into separate JavaScript bundles. The `UiPlugin.getUiMetadata()` would provide a `bundleUrl` for these.
*   **Runtime Loading:** `main-ui` (specifically `PluginPlaceholderView.vue` or a similar mechanism) would dynamically fetch and execute these bundles at runtime when a plugin's UI is requested.
*   **Benefits:**
    *   Independent deployment of plugin frontends.
    *   Reduced initial load size for `main-ui`.
    *   Technology diversity (though sticking to Vue for consistency is simpler).
*   **Challenges:** Shared dependency management, routing integration, inter-frontend communication, build complexity. Tools like Webpack Module Federation could be explored.

## Testing

This project uses JUnit 5 for unit and integration tests.

### Running Tests

You can run all tests in the project using the Gradle wrapper:

```bash
./gradlew test
```

To run tests for a specific module (e.g., `core` or `example-plugin`):

```bash
./gradlew :core:test
# or
./gradlew :example-plugin:test
```

### Code Coverage

Code coverage reports are generated using JaCoCo.

To generate the reports after running tests:

```bash
./gradlew jacocoTestReport
```

Or for a specific module:

```bash
./gradlew :core:jacocoTestReport
# or
./gradlew :example-plugin:jacocoTestReport
```

The HTML reports can be found in the respective module's build directory:

*   `core/build/reports/jacoco/test/html/index.html`
*   `example-plugin/build/reports/jacoco/test/html/index.html`
*   (and so on for other modules)

**Note:** The `core` module's tests currently have issues loading the Spring context with Ebean and H2 for full integration testing, though compilation is successful. One specific test in `example-plugin` (`testPluginIdentity`) is also currently failing due to a string assertion.

## 11. Contribution Guidelines

*   Fork the repository.
*   Create a new branch for your feature or bug fix.
*   Follow the existing code style and conventions.
*   Ensure your changes include appropriate tests.
*   Submit a pull request with a clear description of your changes.
