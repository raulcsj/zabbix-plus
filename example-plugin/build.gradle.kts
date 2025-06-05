plugins {
    `java-library` // It's a library that core will load
    id("io.ebean") version "13.25.0" // Ebean plugin for entity enhancement
}

dependencies {
    implementation(project(":plugin-api")) // Depends on the API
    implementation(project(":core")) // Now directly uses ExampleTableService and ExampleEntity from core

    // Spring Boot and Web for @RestController, etc.
    implementation("org.springframework.boot:spring-boot-starter-web")
    // SLF4J for logging
    implementation("org.slf4j:slf4j-api:2.0.12") // Use a recent version

    // Ebean
    implementation("io.ebean:ebean:13.25.0")
    annotationProcessor("io.ebean:querybean-generator:13.25.0")

    // Test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0") // Already in root, but good practice for explicitness
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0") // Already in root
    testImplementation("org.mockito:mockito-core:5.10.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.10.0")

    // Add any other dependencies the plugin might need
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
