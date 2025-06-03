plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")
    implementation(project(":database")) // Added dependency on database module
    implementation("org.yaml:snakeyaml:2.0") // Added for plugin configuration
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
