plugins {
    id("org.springframework.boot")
    // id("io.spring.dependency-management") // Already managed by root project's plugin
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation(project(":core")) // Dependency on core module

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

// Apply Java plugin if not already applied by subprojects block in root (it is, so this is optional)
// apply(plugin = "java")
