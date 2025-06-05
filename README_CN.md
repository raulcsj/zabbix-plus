# Zabbix Plus Framework (Zabbix增强框架)

## 1. 简介

Zabbix Plus Framework 是一个基于 Spring Boot 的应用程序，旨在通过强大的插件架构扩展 Zabbix（或相关监控解决方案）的功能。它允许开发人员将自定义功能（包括后端逻辑和用户界面组件）无缝集成到中央平台中。

**主要特性:**

*   **可扩展插件架构:** 动态加载和管理打包为 JAR 文件的插件。
*   **后端与前端集成:** 插件可以贡献后端服务 (Java/Kotlin) 和前端 UI 元素 (Vue.js)。
*   **核心服务:** 提供插件可以利用的通用服务和基础设施。
*   **动态 UI 组合:** 主 UI 可以动态呈现活动插件提供的导航项和组件。
*   **集中配置:** 插件可以拥有自己的配置文件。

## 2. 框架架构

该框架包含以下几个关键模块：

*   **`core` (核心模块):** 主要的 Spring Boot 应用程序。它处理插件加载、生命周期管理、提供核心服务，并为前端公开 API。
*   **`plugin-api` (插件 API):** 定义所有插件必须实现的接口 (`Plugin`, `UiPlugin`, `PluginContext`)。这确保了核心框架和插件之间的一致契约。
*   **Plugins (插件，例如 `example-plugin`):** 包含自定义逻辑和 UI 组件的独立模块。它们被打包为 JAR 文件并放置在 `plugins` 目录中。
*   **`main-ui` (主用户界面):** 一个 Vue.js 单页应用程序，作为主要的用户界面。它通过获取元数据并呈现适当的 UI 元素来动态适应加载的插件。
*   **`database` (数据库):** (用于 jOOQ 生成代码、模式管理的概念性或实际模块)。框架使用数据库，并通过 `core` 模块中的 jOOQ 与之交互。

**插件加载机制:**
`core` 模块中的 `PluginService` 负责扫描指定的 `plugins` 目录 (默认为 `./plugins`) 中的 JAR 文件。它使用 Java 的 `ServiceLoader` 机制来发现和实例化这些 JAR 中的 `Plugin` 实现。每个插件都在其自己的 `URLClassLoader` 中加载，以实现一定程度的隔离。

**请求流程 (简化):**
*   **UI 交互:** 用户与 `main-ui` (Vue.js 应用程序) 交互。
*   **前端到后端 API:** `main-ui` 调用后端控制器的 API。
    *   对于通用 UI 元数据: `GET /api/ui/plugin-metadata` (由 `core` 中的 `PluginUiController` 处理)。
    *   对于插件特定操作: 例如 `GET /api/plugins/simpleexampleplugin/data` (由特定插件内的控制器处理，如 `ExamplePluginApiController`)。
*   **核心/插件逻辑:** `core` 或插件中的后端控制器处理这些请求，可能与核心服务 (如 `ExampleTableService`) 或插件特定逻辑交互。

### 插件数据流示例

此示例概述了用户与插件 UI 交互时发生的典型事件序列，从而导致数据获取和显示：

1.  **用户交互 (前端):** 用户在加载的插件 UI 的一部分 Vue 组件（例如，由 `PluginPlaceholderView` 呈现的 `MyPluginComponent.vue` 内部）中单击按钮或执行操作。
2.  **插件 API 调用 (前端):** Vue 组件向插件特定的后端 API 端点发出异步请求（例如，使用 `axios` 或 `fetch`）。此端点通常为插件命名空间，例如：`POST /api/plugins/myplugin/data` 或 `GET /api/plugins/myplugin/items/{itemId}`。
3.  **请求路由到插件控制器 (后端):** Spring Boot 应用程序接收请求。根据 URL 模式（例如 `/api/plugins/myplugin/**`），它将请求路由到特定插件 JAR 文件中定义的 `@RestController`（例如 `MyPluginApiController.java`）。
4.  **插件控制器逻辑 (Backend):** 插件的控制器方法处理请求。它可能：
    *   与插件特定的服务（例如，同样在插件内的 `MyPluginService.java`）交互。
    *   或者，与框架提供的核心服务（例如，如果插件需要访问共享数据，则为 `ExampleTableService`）交互。
5.  **服务层 (后端):** 服务（插件特定或核心）执行业务逻辑。这可能涉及：
    *   从数据库获取数据（例如，通过 `ExampleTableService` 使用 jOOQ 或插件自己的数据访问层）。
    *   调用外部 API。
    *   执行计算或数据转换。
6.  **数据返回流程 (后端到前端):**
    *   服务将数据返回到插件控制器。
    *   插件控制器构造一个 HTTP 响应（例如，一个 JSON 有效负载）并返回它。
    *   Spring Boot 将此响应发送回 `main-ui`。
7.  **UI 更新 (前端):** 发出初始调用的插件的 Vue 组件接收响应。然后它更新其状态，导致 UI 重新呈现并显示新数据或操作结果。

## 3. 核心模块 (`core`)

`core` 模块是 Zabbix Plus Framework 的核心。

*   **职责:**
    *   应用程序入口点 (`CoreApplication.java`)。
    *   通过 `PluginService` 管理插件的生命周期。
    *   提供可供插件访问的共享服务。
    *   服务主 UI 及其相关的 API。
*   **关键组件:**
    *   **`PluginService.java`:**
        *   在启动时从 `plugins` 目录加载插件 JAR。它为每个插件使用单独的 `URLClassLoader`，在插件之间以及插件与核心应用程序之间提供一定程度的类加载器隔离。
        *   管理插件生命周期 (`load`, `init`, `unload`)。
        *   提供对已加载插件实例的访问。
        *   在每个插件的 `init` 阶段，会创建一个 `PluginContext`。此上下文包含对主 `ApplicationContext`（允许插件访问核心 Spring bean）的引用以及插件的特定配置（从其 `config.yml` 或 `config.yaml` 文件解析而来，如果存在）。然后将此 `PluginContext` 传递给插件的 `init` 方法。
    *   **`PluginContext.java`:** (在 `plugin-api` 中定义，但由 `core` 使用)
        *   在插件的 `init` 阶段传递给每个插件。
        *   提供对主 `ApplicationContext` (允许插件检索核心 bean) 和插件特定配置 (来自其 `config.yml`) 的访问。
    *   **组件扫描 (`CoreApplication.java`):**
        *   `CoreApplication` 使用 `@SpringBootApplication(scanBasePackages = {"io.zabbixplus.framework"})`。
        *   至关重要的是，这个 `scanBasePackages` 范围要足够广，以便发现 `core` 模块和任何已加载插件中的 Spring 组件 (`@Service`, `@Component`, `@RestController` 等) (例如 `io.zabbixplus.framework.exampleplugin.controller`)。
*   **核心服务:**
    *   **`ExampleTableService.java`:** 这是一个核心服务的*示例*，插件*可以*（但非必须）使用它。它演示了 `core` 模块中的服务如何提供通用功能，例如数据库交互。
        *   它被实现为一个 Spring `@Service`。
        *   它使用 jOOQ 进行类型安全的 SQL 数据库交互，展示了一种管理数据持久性的方式。
        *   插件可以通过其 `PluginContext` 中提供的 `ApplicationContext` 获取此服务（或其他核心服务）的实例。
        *   它提供了诸如 `createRecord(String name)` 和 `getRecords()` 之类的方法作为数据库操作的示例。
*   **UI 插件的后端 API:**
    *   **`PluginUiController.java`:**
        *   **端点:** `GET /api/ui/plugin-metadata`
        *   **目的:** 向 `main-ui` 提供来自所有活动 `UiPlugin` 实例的 UI 相关信息的合并列表。这使得前端能够动态构建其导航菜单，并知道为每个插件呈现哪些组件。
        *   **响应结构:** 返回 `PluginClientInfo` 对象列表，其中每个对象包含：
            *   `name` (String): 插件的名称。
            *   `uiMetadata` (Map<String, Object>): 来自 `UiPlugin.getUiMetadata()` 的元数据 (例如 `mainComponent`, `bundleUrl`)。
            *   `navigationItems` (List<NavigationItem>): 来自 `UiPlugin.getNavigationItems()` 的导航链接。

## 4. 插件 API (`plugin-api`)

此模块定义了所有插件的基本合约。

*   **目的:** 确保 `core` 框架和各个插件之间的松散耦合，允许插件独立开发和部署。
*   **关键接口和类:**
    *   **`Plugin.java`:** 所有插件的基础接口。
        *   `String getName()`: 返回插件的唯一名称。
        *   `String getDescription()`: 插件的简要描述。
        *   `void load()`: 插件初始加载时调用。用于基本设置。
        *   `void init(PluginContext context)`: 在 `load()` 之后调用。接收一个 `PluginContext` 对象。适用于更复杂的初始化、访问核心服务或加载插件配置。
        *   `void unload()`: 插件卸载时调用。用于资源清理。
    *   **`UiPlugin.java`:** 扩展 `Plugin`。用于贡献用户界面的插件。
        *   `String getVueComponentName()`: (可能已弃用，推荐使用 `getUiMetadata` 中的 `mainComponent`) 返回此插件主 Vue 组件的名称。
        *   `List<NavigationItem> getNavigationItems()`: 返回要添加到 `main-ui` 菜单的导航项列表。
        *   `Map<String, Object> getUiMetadata()`: 返回 UI 特定元数据的映射。基本键包括：
            *   `mainComponent` (String): 为此插件 UI 呈现的主要 Vue 组件的名称 (例如 "ExamplePluginDashboard")。
            *   `pluginName` (String): 插件的名称。
            *   `bundleUrl` (String, 真实微前端的概念性属性): 插件前端 JavaScript 包的 URL。
            *   `description` (String): 插件描述。
    *   **`PluginContext.java`:**
        *   `ApplicationContext getApplicationContext()`: 提供主 `core` 应用程序的 Spring `ApplicationContext`。
        *   `Map<String, Object> getConfiguration()`: 提供从其 `config.yml` 解析的插件特定配置。
    *   **`NavigationItem.java`:** 表示导航链接的简单 DTO 类。
        *   `name` (String): 链接的显示文本。
        *   `path` (String): Vue 路由路径 (例如 `/ui/plugin/MyPluginName`)。
        *   `icon` (String, 可选): 图标的 CSS 类 (例如 `fas fa-home`)。

## 5. 插件开发指南

按照以下步骤创建和集成新插件。

### 创建新插件

1.  **项目设置:**
    *   创建一个新的 Java/Kotlin 项目，通常作为与 `core`, `plugin-api` 等并列的 Gradle 模块。
    *   添加 `plugin-api` 作为依赖项。如果要在插件中创建 Spring 组件（如控制器或服务），还需包含 Spring Boot 依赖项。
2.  **实现接口:**
    *   创建一个实现 `io.zabbixplus.framework.plugin.Plugin` 的类。
    *   如果插件具有 UI，则还需实现 `io.zabbixplus.framework.plugin.UiPlugin`。
3.  **ServiceLoader 声明:**
    *   在插件的 `src/main/resources/META-INF/services/` 目录中，创建一个名为 `io.zabbixplus.framework.plugin.Plugin` 的文件。
    *   在此文件中，写入实现 `Plugin` 接口的类的完全限定名称 (例如 `com.example.myplugin.MyPluginImplementation`)。

### 插件配置

1.  **创建 `config.yml`:**
    *   在插件的 `src/main/resources/` 目录中，创建一个 `config.yml` 或 `config.yaml` 文件。
    *   在此处使用 YAML 语法定义插件特定的设置。示例：
        ```yaml
        featureFlags:
          newFeatureEnabled: true
        apiSettings:
          url: "https://api.example.com/data"
          timeoutSeconds: 30
        ```
2.  **访问配置:**
    *   在插件的 `init(PluginContext context)` 方法中，您可以检索已解析的配置：
        ```java
        Map<String, Object> config = context.getConfiguration();
        Map<String, Object> apiSettings = (Map<String, Object>) config.get("apiSettings");
        String apiUrl = (String) apiSettings.get("url");
        ```

### 后端逻辑与服务

1.  **访问核心服务:**
    *   如果您的插件需要使用 `core` 模块中的服务 (例如 `ExampleTableService`)，请在 `init` 方法中从 `PluginContext` 获取 `ApplicationContext`。
    *   然后，使用 `applicationContext.getBean(MyCoreService.class)` 检索 bean。
        ```java
        // 在您的 Plugin 实现的 init 方法中：
        this.applicationContext = context.getApplicationContext();
        if (this.applicationContext != null) {
            try {
                this.exampleTableService = this.applicationContext.getBean(ExampleTableService.class);
            } catch (NoSuchBeanDefinitionException e) {
                logger.error("Could not find ExampleTableService bean.", e);
            }
        }
        ```
2.  **创建插件特定的 API:**
    *   您可以在插件中定义 Spring `@RestController` 类，以公开插件特定的 API。
    *   将这些控制器放置在子包中 (例如 `com.example.myplugin.controller`)。
    *   **重要提示:** 确保这些控制器被主应用程序的组件扫描覆盖。`CoreApplication.java` 中推荐的 `scanBasePackages = {"io.zabbixplus.framework"}` 应能覆盖类似 `io.zabbixplus.framework.yourplugin.controller` 包中的控制器。

### 前端组件 (Vue.js)

1.  **存储 Vue 文件:**
    *   开发您的 Vue 组件 (`.vue` 文件) 并将其存储在插件的源树中，例如 `my-plugin/src/main/frontend/`。
2.  **当前 UI 集成策略 ("选项 A"):**
    *   框架当前支持插件 Vue 组件的 "选项 A" 集成。这意味着 `main-ui` 应用程序需要能够感知并打包这些组件。
    *   在您的 `UiPlugin` 实现中，`getUiMetadata()` 方法必须返回一个 `mainComponent` 键。该值是在 `main-ui` 中注册组件时使用的字符串名称 (例如 `"MyPluginMainViewer"`)。
        ```java
        // 在您的 UiPlugin 实现中
        @Override
        public Map<String, Object> getUiMetadata() {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("mainComponent", "MyPluginMainViewer");
            // ... 其他元数据
            return metadata;
        }
        ```
    *   **`main-ui` 开发人员的手动步骤:**
        1.  **复制/链接组件:** 必须将插件 `frontend` 目录中的 Vue 组件文件 (例如 `MyPluginMainViewer.vue`) 复制或链接到 `main-ui` 构建过程可访问的位置 (例如 `main-ui/src/main/frontend/src/components/plugins/MyPluginMainViewer.vue`)。
        2.  **全局注册:** 必须在 `main-ui` 的入口点 (`main-ui/src/main/frontend/src/main.js`) 中使用 `mainComponent` 中指定的
            确切名称全局注册该组件：
            ```javascript
            // 在 main-ui/src/main/frontend/src/main.js 中
            import MyPluginMainViewer from './components/plugins/MyPluginMainViewer.vue'; // 调整路径
            // ...
            app.component("MyPluginMainViewer", MyPluginMainViewer);
            ```

### 打包与部署

1.  **构建 JAR:** 构建您的插件项目以生成 JAR 文件 (例如，使用 `./gradlew build`)。
2.  **部署:** 将生成的插件 JAR 文件复制到 Zabbix Plus Framework 主应用程序部署的 `plugins` 目录中。`core` 应用程序将在启动时加载它。

## 6. 打包和部署

本节概述了打包 Zabbix Plus Framework 应用程序并将其部署为各种操作系统上的服务的过程。

### 先决条件 (通用)

在开始之前，请确保已安装以下各项：

*   **Java Development Kit (JDK):** 版本 17 或更高。
*   **Gradle:** 项目使用 Gradle 包装器 (`gradlew` 或 `gradlew.bat`)，因此通常不需要单独安装 Gradle。如果遇到问题，请确保您拥有兼容的 Gradle 版本。
*   **管理/Root 访问权限:** 在相应操作系统上安装服务时需要。

下面的特定操作系统部分可能有其他先决条件。

### 打包应用程序

核心应用程序打包为可执行 JAR 文件。

1.  **构建核心应用程序 JAR:**
    导航到项目根目录并运行以下命令：
    ```bash
    ./gradlew :core:bootJar
    ```
    或者，您可以运行 `./gradlew :core:build`。
    生成的 JAR 文件通常位于 `core/build/libs/` 中，并命名为类似 `core-<version>.jar` (例如 `core-0.0.1-SNAPSHOT.jar`)。这是主应用程序 JAR。

2.  **插件:**
    插件作为单独的 JAR 文件开发和构建。构建完成后，这些插件 JAR 应放置在应用程序安装/运行时目录中的 `plugins` 目录中。框架将从那里加载它们。有关创建插件的详细信息，请参阅第 5 节 ("插件开发指南")。

### 部署为服务

#### 数据库设置 (首次运行重要提示)

首次部署服务之前，请确保您的数据库已设置并且应用程序可以连接到它。
*   **创建数据库:** 创建一个数据库 (例如 `zabbixplus_dev`)。
*   **配置连接:** 使用您的数据库连接详细信息更新 `core/src/main/resources/application.properties` (或其 YAML 等效文件，或使用环境变量)：
    *   `spring.datasource.url`
    *   `spring.datasource.username`
    *   `spring.datasource.password`
*   **模式:** `example_table` (包含列 `id` SERIAL PRIMARY KEY, `name` VARCHAR(255), `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP) 需要存在于示例插件中。对于生产环境，建议使用像 Liquibase 或 Flyway 这样的专业数据库迁移工具来管理您的模式。此设置通常在首次部署之前完成一次。

#### Windows

在 `deployment/windows/` 目录中提供了将应用程序作为 Windows 服务运行的综合说明和脚本。

*   **关键文件:**
    *   `deployment/windows/README_Windows_Service.txt`: 详细说明。
    *   `deployment/windows/zabbix-plus-framework.bat`: 启动 Java 应用程序的脚本 (在此处自定义 JAR 名称, JAVA_OPTS)。
    *   `deployment/windows/install-service.bat`: 使用 `sc.exe` 安装服务的脚本。
    *   `deployment/windows/uninstall-service.bat`: 卸载服务的脚本。

*   **常规步骤:**
    1.  **打包和准备:** 构建 `core-<version>.jar`。创建一个安装目录 (例如 `C:\ZabbixPlusFramework`)。
        *   将 `core-<version>.jar` 复制到此目录。
        *   创建一个 `plugins` 子目录并将您的插件 JAR 放入其中。
        *   将 `deployment/windows/` 目录中的所有文件复制到您的安装目录中。
    2.  **自定义脚本:**
        *   编辑 `zabbix-plus-framework.bat` 以设置正确的 `APP_JAR` (您的 `core-<version>.jar` 文件名) 和任何 `JAVA_OPTS`。
        *   编辑 `install-service.bat` 以将 `APP_ROOT_DIR` 设置为您的安装目录路径，并根据需要自定义 `SERVICE_NAME`、`DISPLAY_NAME` 和 `LOG_DIR`。
    3.  **安装服务:** 以管理员身份运行 `install-service.bat`。
    4.  **管理服务:** 使用 `sc start/stop/query %SERVICE_NAME%` 或服务管理控制台 (`services.msc`)。

*   **日志记录:** `README_Windows_Service.txt` 强烈建议使用像 **NSSM (Non-Sucking Service Manager)** (https://nssm.cc/) 这样的服务包装器来进行健壮的日志重定向和管理，因为 `sc.exe` 在处理批处理脚本方面存在限制。

#### Linux (systemd)

该框架可以在 Linux 发行版上作为 systemd 服务运行。

*   **关键文件:**
    *   `deployment/systemd/zabbix-plus-framework.service`: 一个 systemd 服务单元文件模板。

*   **说明:**
    1.  **打包和准备:**
        *   构建 `core-<version>.jar`。
        *   创建一个安装目录 (例如 `/opt/zabbix-plus-framework`)。
        *   将 `core-<version>.jar` 放入此目录。
        *   在安装目录中创建 `plugins` 和 `logs` 子目录：
            ```bash
            sudo mkdir -p /opt/zabbix-plus-framework/plugins
            sudo mkdir -p /opt/zabbix-plus-framework/logs
            ```
        *   将您的插件 JAR 复制到 `/opt/zabbix-plus-framework/plugins/`。
        *   确保按照上面的 "数据库设置" 设置您的数据库。
    2.  **创建启动脚本:**
        例如，在 `/opt/zabbix-plus-framework/bin/zabbix-plus-framework.sh` 创建一个启动脚本：
        ```bash
        #!/bin/bash
        APP_DIR="$(cd "$(dirname "$0")/.." && pwd)" # 假设脚本位于 'bin' 子目录中
        JAR_NAME="core-0.0.1-SNAPSHOT.jar" # !!! 将此更改为您的实际核心 JAR 文件名 !!!
        JAVA_OPTS="-Xmx512m -Dfile.encoding=UTF-8" # 添加其他 Java 选项，确保使用 UTF-8
        LOG_DIR="${APP_DIR}/logs"
        PID_FILE="${APP_DIR}/app.pid"

        # 确保运行服务的用户对 APP_DIR 和 LOG_DIR 具有写入权限
        mkdir -p "${LOG_DIR}"
        cd "${APP_DIR}"

        echo "Starting Zabbix Plus Framework..." >> "${LOG_DIR}/stdout.log"
        nohup java $JAVA_OPTS -jar "${JAR_NAME}" >> "${LOG_DIR}/stdout.log" 2>> "${LOG_DIR}/stderr.log" &
        echo $! > "${PID_FILE}"
        echo "Zabbix Plus Framework started with PID $(cat ${PID_FILE}). Logs in ${LOG_DIR}" >> "${LOG_DIR}/stdout.log"
        ```
        *   **重要提示:**
            *   修改脚本中的 `JAR_NAME` 以匹配您的应用程序 JAR 文件名。
            *   设置适当的 `JAVA_OPTS`。
            *   使脚本可执行：`sudo chmod +x /opt/zabbix-plus-framework/bin/zabbix-plus-framework.sh`。
    3.  **配置 systemd 服务文件:**
        *   将提供的 `deployment/systemd/zabbix-plus-framework.service` 文件复制到 `/etc/systemd/system/zabbix-plus-framework.service`。
            ```bash
            sudo cp deployment/systemd/zabbix-plus-framework.service /etc/systemd/system/zabbix-plus-framework.service
            ```
        *   编辑 `/etc/systemd/system/zabbix-plus-framework.service`:
            *   将 `User` 和 `Group` 设置为适当的用户/组 (例如，专用的 `zabbixplus` 用户，或 `youruser`)。此用户需要对安装和日志目录具有写入权限。
            *   将 `WorkingDirectory` 更新为您的安装目录 (例如 `/opt/zabbix-plus-framework`)。
            *   将 `ExecStart` 更新为指向您的启动脚本：`ExecStart=/opt/zabbix-plus-framework/bin/zabbix-plus-framework.sh`。
            *   将 `PIDFile` 更新为指向脚本创建的 `app.pid`：`PIDFile=/opt/zabbix-plus-framework/app.pid`。
            *   如果您在 `.sh` 脚本中定义 `JAVA_OPTS` 和 `APP_JAR_PATH`，请删除或注释掉 `Environment` 行。
    4.  **管理服务:**
        *   重新加载 systemd 配置: `sudo systemctl daemon-reload`
        *   使服务在引导时启动: `sudo systemctl enable zabbix-plus-framework`
        *   启动服务: `sudo systemctl start zabbix-plus-framework`
        *   检查状态: `sudo systemctl status zabbix-plus-framework`
        *   停止服务: `sudo systemctl stop zabbix-plus-framework`
    5.  **查看日志:**
        *   使用 journalctl: `sudo journalctl -u zabbix-plus-framework`
        *   直接从日志文件: 检查您配置的 `logs` 目录中的 `stdout.log` 和 `stderr.log` (例如 `/opt/zabbix-plus-framework/logs/`)。

#### macOS (launchd)

该框架可以在 macOS 上作为 launchd 服务运行。

*   **关键文件:**
    *   `deployment/launchd/io.zabbixplus.framework.plist`: 一个 launchd 属性列表文件模板。

*   **说明:**
    1.  **打包和准备:**
        *   构建 `core-<version>.jar`。
        *   创建一个安装目录。常见位置：
            *   系统范围: `/opt/zabbix-plus-framework`
            *   用户特定: `~/Library/Application Support/ZabbixPlusFramework`
            (本示例使用 `/opt/zabbix-plus-framework`，请根据需要进行调整)。
        *   将 `core-<version>.jar` 放入此目录。
        *   创建 `plugins` 和 `logs` 子目录：
            ```bash
            sudo mkdir -p /opt/zabbix-plus-framework/plugins # 如果是系统范围，请使用 sudo
            sudo mkdir -p /opt/zabbix-plus-framework/logs   # 如果是系统范围，请使用 sudo
            # 如果是用户特定，请省略 sudo 并调整路径，例如 mkdir -p ~/Library/Application\ Support/ZabbixPlusFramework/plugins
            ```
        *   将插件 JAR 复制到 `plugins` 目录中。
        *   确保按照上面的 "数据库设置" 设置您的数据库。
    2.  **创建启动脚本:**
        例如，在 `/opt/zabbix-plus-framework/bin/zabbix-plus-framework.sh` 创建一个启动脚本 (类似于 Linux 的脚本)：
        ```bash
        #!/bin/bash
        APP_DIR="$(cd "$(dirname "$0")/.." && pwd)" # 假设脚本位于 'bin' 子目录中
        JAR_NAME="core-0.0.1-SNAPSHOT.jar" # !!! 将此更改为您的实际核心 JAR 文件名 !!!
        JAVA_OPTS="-Xmx512m -Dfile.encoding=UTF-8" # 添加其他 Java 选项，确保使用 UTF-8
        # 如果使用 .plist 进行重定向，则此处不严格需要 LOG_DIR，但为了保持一致性
        # LOG_DIR="${APP_DIR}/logs"
        # mkdir -p "${LOG_DIR}" # 如果 .plist 在此处重定向，请确保此目录存在

        cd "${APP_DIR}"

        # 对于 launchd，直接输出通常比使用 & 在后台运行更好
        # .plist 文件将处理 stdout/stderr 重定向。
        exec java $JAVA_OPTS -jar "${JAR_NAME}"
        ```
        *   **重要提示:**
            *   将 `JAR_NAME` 修改为您的应用程序 JAR 文件。
            *   设置 `JAVA_OPTS`。
            *   使脚本可执行：`sudo chmod +x /opt/zabbix-plus-framework/bin/zabbix-plus-framework.sh` (如果在 `/opt` 中，请使用 `sudo`)。
    3.  **配置 launchd `.plist` 文件:**
        *   确定服务范围：
            *   用户代理 (用户登录时运行): `~/Library/LaunchAgents/io.zabbixplus.framework.plist`
            *   系统守护进程 (引导时运行，需要 root): `/Library/LaunchDaemons/io.zabbixplus.framework.plist`
        *   将 `deployment/launchd/io.zabbixplus.framework.plist` 复制到所选位置。系统守护进程示例：
            ```bash
            sudo cp deployment/launchd/io.zabbixplus.framework.plist /Library/LaunchDaemons/io.zabbixplus.framework.plist
            ```
        *   编辑 `.plist` 文件 (例如 `sudo nano /Library/LaunchDaemons/io.zabbixplus.framework.plist`):
            *   将 `Label` 更新为唯一值 (例如 `io.zabbixplus.framework`)。
            *   修改 `ProgramArguments`: 第一个字符串应为启动脚本的完整路径 (例如 `/opt/zabbix-plus-framework/bin/zabbix-plus-framework.sh`)。
            *   将 `WorkingDirectory` 设置为您的安装目录 (例如 `/opt/zabbix-plus-framework`)。
            *   将 `StandardOutPath` 设置为您所需的 stdout 日志文件 (例如 `/opt/zabbix-plus-framework/logs/stdout.log`)。
            *   将 `StandardErrorPath` 设置为您所需的 stderr 日志文件 (例如 `/opt/zabbix-plus-framework/logs/stderr.log`)。
            *   如果作为系统守护进程运行 (`/Library/LaunchDaemons`)，您可能需要添加一个 `UserName` 键来指定运行用户 (例如 `<key>UserName</key><string>youruser</string>`)。此用户需要对安装和日志目录具有写入权限。
            *   确保 `KeepAlive` 设置为 `true` 或根据需要进行自动重启。
    4.  **管理服务:**
        *   **加载:**
            *   LaunchAgent: `launchctl load ~/Library/LaunchAgents/io.zabbixplus.framework.plist`
            *   LaunchDaemon: `sudo launchctl load /Library/LaunchDaemons/io.zabbixplus.framework.plist`
        *   **启动 (通常在加载时自动启动，但可以显式启动):**
            *   `launchctl start io.zabbixplus.framework` (如果是 LaunchDaemon 并且您不是 root 用户，请使用 `sudo`)
        *   **停止:**
            *   `launchctl stop io.zabbixplus.framework` (如果是 LaunchDaemon，请使用 `sudo`；如果 KeepAlive 设置比较激进，可能无法停止)
        *   **卸载 (以防止自动启动):**
            *   LaunchAgent: `launchctl unload ~/Library/LaunchAgents/io.zabbixplus.framework.plist`
            *   LaunchDaemon: `sudo launchctl unload /Library/LaunchDaemons/io.zabbixplus.framework.plist`
    5.  **查看日志:** 检查 `StandardOutPath` 和 `StandardErrorPath` 中指定的文件。Console.app 也可以显示 launchd 日志。

### 配置说明 (通用)

*   **应用程序配置:** 主要的应用程序配置 (例如数据库连接、服务器端口) 通常通过 `application.properties` 或 `application.yml` 进行管理。此文件可以放置在 `core-<version>.jar` 旁边的 `config` 目录中，或直接与 JAR 文件放在一起。对于服务部署，通常使用外部化配置文件。您可以在 `JAVA_OPTS` 中使用 `spring.config.location` Java 系统属性指定其位置：
    ```
    -Dspring.config.location=file:/path/to/your/application.properties
    ```
*   **插件配置:** 每个插件都可以在其 JAR 资源中拥有自己的 `config.yml`，可通过 `PluginContext` 访问。请参阅第 5 节 ("插件开发指南")。
*   **插件目录:** 确保 `plugins` 目录存在于应用程序的 `WorkingDirectory` 中，并包含所有插件 JAR。

## 7. 主 UI (`main-ui`) 集成

`main-ui` 是一个 Vue.js 应用程序，旨在为框架及其插件提供动态前端。

*   **关键服务:**
    *   **`PluginRegistryService.js` (`src/services/PluginRegistryService.js`):**
        *   负责从后端 API 端点 `GET /api/ui/plugin-metadata` 获取插件 UI 数据。
        *   缓存获取的数据。
        *   为以下内容提供响应式 Vue `ref` 和 `computed` 属性：
            *   `plugins`: 所有插件客户端信息的列表。
            *   `navItems`: 来自所有 UI 插件的导航项的聚合列表。
            *   `pluginMetadataMap`: 按名称轻松查找插件数据的映射。
            *   `isLoading`, `error`: 用于管理 API 调用状态。
        *   **初始化:** 公开一个 `initialize()` 函数，该函数应在 Vue 应用程序启动时调用 (例如，在 `main.js` 或根 `App.vue` 的 `onMounted` 钩子中) 以加载插件数据。
*   **动态特性:**
    *   **导航菜单 (`App.vue`):**
        *   使用 `PluginRegistryService.navItems` 在主导航栏中动态呈现 `<router-link>` 组件。
        *   菜单自动反映活动 `UiPlugin` 贡献的导航项。
    *   **插件组件渲染 (`PluginPlaceholderView.vue`):**
        *   通过 `/ui/plugin/:pluginName` 路由。
        *   使用 `PluginRegistryService` 获取指定 `pluginName` 的 `uiMetadata`。
        *   使用 `<component :is="pluginUiMeta.mainComponent">` 动态呈现插件的主组件。
        *   **关键依赖于 "选项 A" 集成策略:** 从 `mainComponent` 元数据获取的 Vue 组件名称 (例如 "ExamplePluginDashboard") 必须对应于在 `main-ui` 中全局注册的组件 (有关详细信息，请参阅插件开发指南)。
*   **开发人员工作流程 (将新插件的 UI 添加到 `main-ui`):**
    *   当开发具有 Vue UI 的新插件时，`main-ui` 开发人员需要：
        1.  获取插件的 Vue 组件文件 (例如 `ExamplePluginDashboard.vue`)。
        2.  将它们放入 `main-ui/src/main/frontend/src/components/plugins/`。
        3.  在 `main-ui/src/main/frontend/src/main.js` 中使用插件在其 `uiMetadata.mainComponent` 中提供的名称全局注册组件。

## 8. 示例插件 (`example-plugin`)

`example-plugin` 作为框架插件功能的工作演示。

*   **目的:** 说明如何创建一个同时具有前端和后端功能的插件。
*   **实现的关键特性:**
    *   **`SimpleExamplePlugin.java`:** 实现 `UiPlugin`，提供元数据，并包含通过 `ExampleTableService` 进行数据库交互的逻辑。
    *   **`ExamplePluginDashboard.vue`:** 位于 `example-plugin/src/main/frontend/` 的 Vue 组件。这是插件的 UI，允许用户查看和添加数据。
        *   *集成说明:* 为使此组件可用，必须将其手动复制到 `main-ui/src/main/frontend/src/components/plugins/` 并在 `main-ui/src/main/frontend/src/main.js` 中注册为 "ExamplePluginDashboard"。
    *   **`ExamplePluginApiController.java`:** 插件内的 `@RestController`，公开 API 端点 (`GET` 和 `POST` at `/api/plugins/simpleexampleplugin/data`) 供 `ExamplePluginDashboard.vue` 交互。
    *   **`config.yml`:** 包含一个示例配置文件 (`example-plugin/src/main/resources/config.yml`)，演示插件配置功能。
*   **功能:**
    *   提供一个可从主导航访问的 UI 页面 ("Example Dashboard")。
    *   UI 允许从数据库获取记录列表 (通过其后端 API)。
    *   UI 允许向数据库添加新记录 (通过其后端 API)。后端当前仅处理 "name" 字段。

## 9. 开发人员设置和运行

本节为希望设置本地开发环境的开发人员提供指导。有关作为服务部署的信息，请参阅第 6 节。

### 先决条件 (开发)
*   **Java Development Kit (JDK):** 版本 17 或更高。
*   **Node.js:** 版本 18 或更高，包含 npm 或 yarn (用于 `main-ui` 开发)。
*   **Gradle:** 项目使用 Gradle 包装器。
*   **PostgreSQL 数据库:** 或调整 `ExampleTableService` 和配置以适应其他 SQL 数据库。需要进行初始数据库设置 (创建数据库和表)。

### 后端 (`core` 和插件) - 开发
1.  **克隆存储库。**
2.  **数据库设置:** 如第 6 节 "数据库设置 (首次运行重要提示)" 中所述。
3.  **构建:** `./gradlew clean build` (构建所有模块)。
4.  **运行 `core` 应用程序 (IDE 或命令行):**
    *   从您的 IDE: 运行 `CoreApplication.java` 主方法。
    *   命令行: `java -jar core/build/libs/core-<version>.jar`。
5.  **插件:** 确保插件 JAR 位于运行 `core` 的相对路径 `./plugins` 目录中，或者如果直接运行，您的 IDE 将它们包含在类路径中。

### 前端 (`main-ui`) - 开发
1.  **导航:** `cd main-ui/src/main/frontend/`
2.  **安装依赖:** `npm install`
3.  **集成插件 Vue 组件:** 遵循第 5 节 ("插件开发指南") 和第 7 节 ("主 UI (`main-ui`) 集成") 中的 "选项 A"。这涉及将 Vue 文件复制/链接到 `main-ui` 并注册它们。
4.  **运行开发服务器:** `npm run serve`。通常在 `http://localhost:8081` 启动。
    *   配置 `vue.config.js` 以将 API 请求代理到后端 (通常是 `http://localhost:8080`)。
5.  **为 UI 构建生产版本:** `npm run build`。输出到 `main-ui/src/main/frontend/dist/`。为使 `core` 应用程序能够提供这些文件，应在打包 `core` JAR 之前将它们复制到 `core/src/main/resources/static/`。

## 10. 未来增强 (微前端愿景)

当前 UI 集成的 "选项 A" (其中 `main-ui` 打包插件 Vue 组件) 提供了基于元数据的动态渲染，但要求 `main-ui` 在构建时知道这些组件。

未来的增强功能可能会转向真正的微前端架构 ("选项 B")：

*   **动态包加载:** 插件会将其 Vue UI 构建为单独的 JavaScript 包。`UiPlugin.getUiMetadata()` 将为这些包提供 `bundleUrl`。
*   **运行时加载:** `main-ui` (特别是 `PluginPlaceholderView.vue` 或类似机制) 将在请求插件 UI 时动态获取并执行这些包。
*   **优点:**
    *   插件前端的独立部署。
    *   减少 `main-ui` 的初始加载大小。
    *   技术多样性 (尽管为保持一致性而坚持使用 Vue 更简单)。
*   **挑战:** 共享依赖管理、路由集成、前端间通信、构建复杂性。可以探索像 Webpack Module Federation 这样的工具。

## 11. 贡献指南

*   Fork (复刻) 该存储库。
*   为您的特性或错误修复创建一个新分支。
*   遵循现有的代码风格和约定。
*   确保您的更改包含适当的测试。
*   提交一个包含清晰更改描述的拉取请求。
```
