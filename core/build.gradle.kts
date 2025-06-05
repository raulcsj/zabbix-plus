plugins {
    id("org.springframework.boot")
    id("application") // Apply the application plugin
}

val distributionName = "zabbix-plus-framework" // Define distributionName earlier

// Configure the application plugin
application {
    // Define the main class for the application
    mainClass.set("io.zabbixplus.framework.core.CoreApplication")
    applicationName = "zabbix-plus-framework" // Set the application name using direct assignment

    distributions.named("main") {
        distributionBaseName.set(distributionName) // Sets the base name for the archive (e.g., zabbix-plus-framework.zip)
        contents {
            // Copy core JAR and its dependencies (handled by application plugin by default into its own lib structure)
            // We need to ensure plugin-api.jar is also there.
            // The application plugin by default puts runtime dependencies of the 'application' project (core) into 'lib'.
            // Since :plugin-api is now a dependency of :core, it should be included.

            // Copy plugins
            into("plugins") {
                from(project(":example-plugin").tasks.named("jar")) { // Depends on example-plugin's jar task
                    rename { "${it.replace("-${project(":example-plugin").version}", "")}" } // Optional: remove version from plugin jar name
                }
                // Add other plugins here similarly
            }

            // Copy main-ui static assets
        // Temporarily commented out to avoid 'buildFrontend' task not found error
        // into("ui") {
        //     from(project(":main-ui").tasks.named("buildFrontend").map { it.outputs.files }) // Depends on main-ui's buildFrontend task
        //     // This will copy the contents of main-ui/src/main/frontend/dist into the ui/ directory
        // }

            // Copy custom bin scripts
            into("bin") {
                from("src/main/dist") { // Assuming scripts are placed in src/main/dist/
                    include("*.sh", "*.bat")
                    // Ensure .sh script has execute permissions in the archive
                    // For .sh scripts, it's better to set permissions directly in the file system
                    // or use a more specific way to set permissions in the archive if needed.
                    // The 'fileMode' here applies to all files copied in this block.
                    // We'll ensure the .sh script has execute permissions when created or committed.
                    // For Gradle, using standard unix permissions for .sh files in src/main/dist is common.
                }
                // The applicationName being set means default scripts are zabbix-plus-framework and zabbix-plus-framework.bat
                // Our custom scripts with these names will replace them.
                fileMode = "0755".toInt(8) // Set execute permissions for all files copied to bin; primarily for .sh
            }

            // Copy sample configuration
            into("config") {
                from("src/main/resources") { // Assuming application.properties is in core's resources
                    include("application.properties") // Or application.yml if that's what you use
                    // Potentially rename to a sample or default if the main one is packaged in JAR
                    // rename("application.properties", "application.properties.sample")
                }
                // You can also create a dedicated sample config file here
                // from("src/dist/config") // If you have specific sample configs for distribution
            }
        }
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-jooq") // For jOOQ and Spring Transaction support
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")
    implementation(project(":database"))
    implementation(project(":plugin-api")) // Add plugin-api as a direct dependency to core
                                         // This ensures it's part of core's classpath and available to plugins
                                         // And also makes it easier to include in the 'lib' dir of the distribution.
    implementation("org.yaml:snakeyaml:2.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito:mockito-core:5.10.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.10.0")
    testImplementation("com.h2database:h2")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Configure the distribution packaging (distZip, distTar)
tasks.withType<CreateStartScripts> {
    // Customize script creation if needed, e.g., defaultJvmOpts
    // For now, we'll create script content manually in a later step
    // but these tasks will generate placeholder scripts.
}

// Ensure that building the distribution depends on assembling necessary JARs and UI assets
tasks.named("distTar") {
    dependsOn(project(":core").tasks.named("bootJar")) // Ensure core.jar is the Spring Boot executable JAR
    dependsOn(project(":plugin-api").tasks.named("jar"))
    dependsOn(project(":example-plugin").tasks.named("jar"))
    dependsOn(project(":main-ui").tasks.named("buildFrontend"))
}
tasks.named("distZip") {
    dependsOn(project(":core").tasks.named("bootJar"))
    dependsOn(project(":plugin-api").tasks.named("jar"))
    dependsOn(project(":example-plugin").tasks.named("jar"))
    dependsOn(project(":main-ui").tasks.named("buildFrontend"))
}

// Spring Boot specific configuration for the executable JAR
// The application plugin creates a 'run' task and scripts that use the standard JAR,
// not the bootJar. For the distribution, we want the 'lib' dir to contain dependencies,
// and core.jar to be runnable with `java -jar core.jar` but also via scripts that build a classpath.
// The default 'jar' task by Spring Boot is disabled and 'bootJar' is enabled.
// We need to ensure the 'jar' task (non-boot) is also available or that scripts can use bootJar.
// The application plugin's 'applicationDistribution' will use the output of the 'jar' task by default.
// Let's re-enable the standard 'jar' task for the 'core' module if 'application' plugin needs it,
// or configure 'application' plugin to use 'bootJar' as the main JAR.

// Option 1: Make application plugin use bootJar (simpler if scripts are complex)
tasks.named<org.gradle.jvm.tasks.Jar>(JavaPlugin.JAR_TASK_NAME) {
    enabled = true // Spring Boot plugin might disable it. Ensure it's enabled.
}
// The application plugin uses the output of the `jar` task for the main library.
// If we want the core.jar in lib/ to be the bootJar, it's more complex.
// Usually, for the application plugin, the main JAR in lib/ is a standard JAR, not a fat JAR.
// The classpath is then constructed from all JARs in lib/.

// For Spring Boot, the 'application' plugin and 'spring-boot' plugin need careful integration
// if you want the scripts to run the *unpacked* version (main jar + deps in lib).
// If scripts run `java -jar core-version.jar` (the bootJar), then `lib` is not used by that command.

// Let's assume scripts will build a classpath and run.
// The Spring Boot plugin changes the `jar` task to produce a plain jar and `bootJar` for the executable.
// The `application` plugin will use the output of `jar` for `core.jar` in `lib/`. This is fine.
// The main manifest attributes will be set by `application.mainClass`.
