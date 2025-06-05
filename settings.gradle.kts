pluginManagement {
    repositories {
        gradlePluginPortal() // Standard Gradle plugin portal
        mavenCentral()       // Maven Central for other plugins
    }
}

rootProject.name = "my-framework"
include("core")
include("plugin-api")
include("example-plugin")
include("main-ui")
include("database")
