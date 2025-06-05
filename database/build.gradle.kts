plugins {
    id("java-library")
}

dependencies {
    // core module will depend on this database module.
    // No explicit dependency on core here to avoid circularity for now.

    implementation("org.springframework.boot:spring-boot-starter-data-jpa") // Provides DataSource, transaction management
    implementation("org.xerial:sqlite-jdbc:3.45.1.0") // SQLite driver (using a recent version)
    // Removed jOOQ library: implementation("org.jooq:jooq:3.18.7")

    // Removed jOOQ code generation dependencies
    // jooqGenerator("org.jooq:jooq-meta:3.18.7")
    // jooqGenerator("org.jooq:jooq-codegen:3.18.7")
    // jooqGenerator("org.xerial:sqlite-jdbc:3.45.1.0")
    // jooqGenerator("com.h2database:h2:2.2.224")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

// Removed entire jooq { ... } configuration block
// Removed sourceSet configuration for jOOQ generated sources
// Removed compileJava dependency on jOOQ codegen task
