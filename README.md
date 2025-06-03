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
*   **`database`:** (Conceptual or actual module for jOOQ generated code, schema management). The framework uses a database, interacted with via jOOQ in the `core` module.

**Plugin Loading Mechanism:**
The `PluginService` in the `core` module is responsible for scanning a designated `plugins` directory (default: `./plugins`) for JAR files. It uses Java's `ServiceLoader` mechanism to discover and instantiate `Plugin` implementations within these JARs. Each plugin is loaded in its own `URLClassLoader` for some degree of isolation.

**Request Flow (Simplified):**
*   **UI Interaction:** User interacts with the `main-ui` (Vue.js application).
*   **Frontend to Backend API:** `main-ui` makes API calls to backend controllers.
    *   For general UI metadata: `GET /api/ui/plugin-metadata` (handled by `PluginUiController` in `core`).
    *   For plugin-specific actions: e.g., `GET /api/plugins/simpleexampleplugin/data` (handled by controllers within the specific plugin, like `ExamplePluginApiController`).
*   **Core/Plugin Logic:** Backend controllers in `core` or plugins process these requests, potentially interacting with core services (like `ExampleTableService`) or plugin-specific logic.

## 3. Core Module (`core`)

The `core` module is the heart of the Zabbix Plus Framework.

*   **Responsibilities:**
    *   Application entry point (`CoreApplication.java`).
    *   Managing the lifecycle of plugins via `PluginService`.
    *   Providing shared services accessible to plugins.
    *   Serving the main UI and its associated APIs.
*   **Key Components:**
    *   **`PluginService.java`:**
        *   Loads plugin JARs from the `plugins` directory at startup.
        *   Manages plugin lifecycle (`load`, `init`, `unload`).
        *   Provides access to loaded plugin instances.
    *   **`PluginContext.java`:** (Defined in `plugin-api`, but used by `core`)
        *   Passed to each plugin during its `init` phase.
        *   Provides access to the main `ApplicationContext` (allowing plugins to retrieve core beans) and the plugin's specific configuration (from its `config.yml`).
    *   **Component Scanning (`CoreApplication.java`):**
        *   `CoreApplication` uses `@SpringBootApplication(scanBasePackages = {"io.zabbixplus.framework"})`.
        *   It's crucial that this `scanBasePackages` is broad enough to discover Spring components (`@Service`, `@Component`, `@RestController`, etc.) in both the `core` module and any loaded plugins (e.g., `io.zabbixplus.framework.exampleplugin.controller`).
*   **Core Services:**
    *   **`ExampleTableService.java`:** An example service demonstrating database interaction using jOOQ. It provides methods like `createRecord(String name)` and `getRecords()`.
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

## 6. Main UI (`main-ui`) Integration

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

## 7. Example Plugin (`example-plugin`)

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

## 8. Setup & Running (Conceptual)

### Prerequisites
*   Java Development Kit (JDK), version 17 or higher.
*   Node.js (for `main-ui`), version 18 or higher, with npm or yarn.
*   Gradle (version compatible with the project, typically uses Gradle Wrapper).
*   A PostgreSQL database (or adapt `ExampleTableService` and configuration for another SQL database).

### Backend (`core` and plugins)
1.  **Clone Repository:** `git clone <repository-url>`
2.  **Database Setup:**
    *   Create a database (e.g., `zabbixplus_dev`).
    *   Configure database connection details in `core/src/main/resources/application.properties` (or its YAML equivalent). You'll likely need to set `spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password`.
    *   The `example_table` (with columns `id` SERIAL PRIMARY KEY, `name` VARCHAR(255), `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP) needs to exist. A Liquibase/Flyway setup would be ideal for managing schema, but is not currently detailed.
3.  **Build:**
    *   Navigate to the project root directory.
    *   Run `./gradlew clean build`. This will build all modules, including `core` and `example-plugin`.
4.  **Run `core` Application:**
    *   Execute `java -jar core/build/libs/core-<version>.jar`. Replace `<version>` with the actual version.
5.  **Deploy Plugins:**
    *   Create a `plugins` directory in the same location where you run the `core` JAR.
    *   Copy plugin JARs (e.g., `example-plugin/build/libs/example-plugin-<version>.jar`) into this `plugins` directory.
    *   Restart the `core` application to load the plugins.

### Frontend (`main-ui`)
1.  **Navigate to UI Directory:** `cd main-ui/src/main/frontend/`
2.  **Install Dependencies:** `npm install` (or `yarn install`).
3.  **Integrate Plugin Vue Components (Manual Step):**
    *   For each plugin providing UI (like `example-plugin`):
        *   Copy its Vue component files (e.g., `ExamplePluginDashboard.vue` from `example-plugin/src/main/frontend/`) to `main-ui/src/main/frontend/src/components/plugins/`.
        *   Register these components globally in `main-ui/src/main/frontend/src/main.js`.
4.  **Run Development Server:**
    *   `npm run serve` (or `yarn serve`).
    *   This will typically start the Vue app on `http://localhost:8081` (or another port).
    *   Ensure `vue.config.js` in `main-ui/src/main/frontend/` has the correct `devServer.proxy` settings to forward API requests (like `/api/*`) to your backend server (usually running on `http://localhost:8080`).
    Example `vue.config.js` proxy:
    ```javascript
    module.exports = {
      devServer: {
        port: 8081, // Or your preferred frontend port
        proxy: {
          '/api': {
            target: 'http://localhost:8080', // Your backend server
            ws: true,
            changeOrigin: true
          }
        }
      }
    };
    ```
5.  **Build for Production:**
    *   `npm run build` (or `yarn build`).
    *   The static assets will be generated in `main-ui/src/main/frontend/dist/`. These assets should be served by the `core` application (Spring Boot is typically configured to serve static content from `classpath:/static/` or `classpath:/public/`; ensure the `main-ui` build output is copied to `core/src/main/resources/static/` or similar before `core` is packaged).

## 9. Future Enhancements (Micro-Frontend Vision)

The current "Option A" for UI integration (where `main-ui` bundles plugin Vue components) provides dynamic rendering based on metadata but requires `main-ui` to be aware of these components at build time.

Future enhancements could move towards a true micro-frontend architecture ("Option B"):

*   **Dynamic Bundle Loading:** Plugins would build their Vue UIs into separate JavaScript bundles. The `UiPlugin.getUiMetadata()` would provide a `bundleUrl` for these.
*   **Runtime Loading:** `main-ui` (specifically `PluginPlaceholderView.vue` or a similar mechanism) would dynamically fetch and execute these bundles at runtime when a plugin's UI is requested.
*   **Benefits:**
    *   Independent deployment of plugin frontends.
    *   Reduced initial load size for `main-ui`.
    *   Technology diversity (though sticking to Vue for consistency is simpler).
*   **Challenges:** Shared dependency management, routing integration, inter-frontend communication, build complexity. Tools like Webpack Module Federation could be explored.

## 10. Contribution Guidelines

*   Fork the repository.
*   Create a new branch for your feature or bug fix.
*   Follow the existing code style and conventions.
*   Ensure your changes include appropriate tests.
*   Submit a pull request with a clear description of your changes.
