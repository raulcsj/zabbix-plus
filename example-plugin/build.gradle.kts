plugins {
    `java-library` // It's a library that core will load
}

dependencies {
    implementation(project(":plugin-api")) // Depends on the API
    // Add any other dependencies the plugin might need
    // implementation("org.slf4j:slf4j-api:2.0.9") // If plugin wants to log (example)
}

// Task to package as a JAR
tasks.jar {
    manifest {
        attributes(
            // "Main-Class": "com.csj.framework.exampleplugin.ExamplePluginMain" // Not needed for ServiceLoader
        )
    }
    // If plugin has dependencies not provided by core or plugin-api,
    // they need to be bundled (fat JAR) or placed in the plugins directory alongside the plugin JAR.
    // For simplicity, this example plugin has no external dependencies beyond plugin-api.
    // Example for fat JAR (uncomment if needed):
    // from(configurations.runtimeClasspath.collect { it.isDirectory() ? it : zipTree(it) })
}
