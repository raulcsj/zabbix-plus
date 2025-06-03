plugins {
    id("java-library")
}

dependencies {
    // core module will depend on this database module.
    // No explicit dependency on core here to avoid circularity for now.

    implementation("org.springframework.boot:spring-boot-starter-data-jpa") // Provides DataSource, transaction management
    implementation("org.xerial:sqlite-jdbc:3.45.1.0") // SQLite driver (using a recent version)
    implementation("org.jooq:jooq:3.19.0") // jOOQ library (using a recent version)

    // Dependencies for jOOQ code generation (will be used later, not in this subtask for execution)
    // implementation("org.jooq:jooq-meta:3.19.0")
    // implementation("org.jooq:jooq-codegen:3.19.0")
    // runtimeOnly("org.xerial:sqlite-jdbc:3.45.1.0") // For codegen if run separately

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
