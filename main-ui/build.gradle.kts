plugins {
    id("org.springframework.boot")
    // id("io.spring.dependency-management") // Managed by root project
    id("java") // Explicitly add java plugin if not already inherited for clarity with Spring Boot
    id("com.github.node-gradle.node") version "7.0.1" // Node Gradle plugin
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation(project(":core")) // Dependency on core module

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

node {
    version.set("18.18.0") // Specify Node.js version
    // Place the node_modules directory and frontend project in src/main/frontend
    nodeProjectDir.set(file("${project.projectDir}/src/main/frontend"))
    // Download Node.js if not already available
    download.set(true)
}

// Define tasks for frontend build
val npmInstallFrontend = tasks.register<com.github.gradle.node.npm.task.NpmTask>("npmInstallFrontend") {
    description = "Installs frontend dependencies using npm."
    dependsOn(tasks.named("npmSetup")) // Depends on npmSetup task from node-gradle plugin
    npmCommand.set(listOf("install"))
    // Optional: Add inputs and outputs for Gradle's up-to-date checking
    inputs.files("src/main/frontend/package.json", "src/main/frontend/package-lock.json")
    outputs.dir("src/main/frontend/node_modules")
}

val buildFrontend = tasks.register<com.github.gradle.node.npm.task.NpmTask>("buildFrontend") {
    description = "Builds the frontend application using npm run build."
    dependsOn(npmInstallFrontend)
    npmCommand.set(listOf("run", "build"))
    // Optional: Add inputs and outputs
    inputs.dir("src/main/frontend/src")
    inputs.dir("src/main/frontend/public")
    inputs.file("src/main/frontend/vue.config.js")
    inputs.file("src/main/frontend/babel.config.js") // If you have babel.config.js
    outputs.dir("src/main/frontend/dist")
}

val copyFrontendDist = tasks.register<Copy>("copyFrontendDist") {
    description = "Copies the built frontend static files to Spring Boot static resources directory."
    dependsOn(buildFrontend)
    from("${project.projectDir}/src/main/frontend/dist")
    into("${project.buildDir}/resources/main/static/") // Standard output for Spring Boot static resources
}

// Hook into the processResources task to ensure frontend is built and copied
tasks.named("processResources") {
    dependsOn(copyFrontendDist)
}

// Add frontend build artifacts to clean task
tasks.named<Delete>("clean") {
    delete("${project.projectDir}/src/main/frontend/dist")
    delete("${project.projectDir}/src/main/frontend/node_modules")
    // Also clean the copied static resources if needed, though buildDir is usually cleaned by default
    delete("${project.buildDir}/resources/main/static")
}
