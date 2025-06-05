plugins {
    java
    id("org.springframework.boot") version "3.2.0" apply false // Apply false so it's not applied to root
    id("io.spring.dependency-management") version "1.1.4"
    jacoco // Add this line
}

allprojects {
    group = "io.zabbixplus.framework"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "io.spring.dependency-management")

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    dependencyManagement {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:3.2.0")
        }
    }

    dependencies {
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
        // Add other common dependencies for sub-projects if any
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

// Specific configurations for plugin-api module
project(":plugin-api") {
    // No Spring Boot plugin here, it's a plain Java library
    dependencies {
        // Dependencies for plugin API, if any (e.g., annotations)
    }
}

// Specific configurations for example-plugin module
project(":example-plugin") {
    dependencies {
        implementation(project(":plugin-api"))
        implementation(project(":core")) // If plugins need access to core components/services directly
                                      // Or, better, core provides interfaces that plugins implement
        // Potentially Spring Boot if plugins are Spring components themselves
        // implementation("org.springframework.boot:spring-boot-starter")
    }
}

// Specific configurations for main-ui module
project(":main-ui") {
    apply(plugin = "org.springframework.boot")

    dependencies {
        implementation("org.springframework.boot:spring-boot-starter-web")
        implementation("org.springframework.boot:spring-boot-starter-thymeleaf") // Example for serving HTML
        implementation(project(":core")) // To interact with core services
        // Dependencies for micro-frontend integration later
    }
}

// Specific configurations for database module
project(":database") {
    // Might need Spring Data JPA or other database-related Spring Boot starters
    dependencies {
        // implementation("org.springframework.boot:spring-boot-starter-data-jpa") // Example
        // implementation("org.xerial:sqlite-jdbc:3.43.0.0") // SQLite driver
        // implementation("org.jooq:jooq:3.18.7") // jOOQ, if chosen
        // implementation("org.jooq:jooq-meta:3.18.7")
        // implementation("org.jooq:jooq-codegen:3.18.7")
    }
}

// Add this block at the end of the file or after the subprojects block

allprojects {
    // Apply JaCoCo to all projects that have the Java plugin
    plugins.withType<JavaPlugin> {
        apply(plugin = "jacoco")

        tasks.withType<JacocoReport> {
            reports {
                xml.required.set(true)
                html.required.set(true)
                csv.required.set(false)
            }
        }

        // Ensure that the test task runs before jacocoTestReport
        tasks.named<JacocoReport>("jacocoTestReport") {
            dependsOn(tasks.named<Test>("test"))
        }
    }
}

// Specific JaCoCo configuration for modules where we want to enforce coverage/reporting
listOf(project(":core"), project(":example-plugin")).forEach { proj ->
    proj.plugins.apply("jacoco") // Ensure jacoco is applied
    proj.tasks.withType<Test> {
        finalizedBy(proj.tasks.named("jacocoTestReport")) // Run jacocoTestReport after tests
    }
    proj.tasks.withType<JacocoReport> {
        // Configure source sets and class directories for each project
        sourceSets(proj.the<JavaPluginExtension>().sourceSets.getByName("main"))
    }
}
