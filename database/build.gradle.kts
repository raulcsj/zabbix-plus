buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal() // Added for completeness
    }
    dependencies {
        classpath("org.jooq:jooq-codegen-gradle:3.18.7") // Classpath for the legacy plugin application
    }
}

plugins {
    id("java-library")
    // id("org.jooq.jooq-codegen") version "3.18.7" // Removed from modern plugins block
}

apply(plugin = "org.jooq.jooq-codegen") // Apply plugin using legacy method

dependencies {
    // core module will depend on this database module.
    // No explicit dependency on core here to avoid circularity for now.

    implementation("org.springframework.boot:spring-boot-starter-data-jpa") // Provides DataSource, transaction management
    implementation("org.xerial:sqlite-jdbc:3.45.1.0") // SQLite driver (using a recent version)
    implementation("org.jooq:jooq:3.18.7") // Align jOOQ library version

    // Dependencies for jOOQ code generation
    jooqGenerator("org.jooq:jooq-meta:3.18.7")
    jooqGenerator("org.jooq:jooq-codegen:3.18.7")
    jooqGenerator("org.xerial:sqlite-jdbc:3.45.1.0") // Driver for generator if using SQLite for schema def
    jooqGenerator("com.h2database:h2:2.2.224") // H2 driver for generator if using H2

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

jooq {
    version.set("3.18.7") // Align jOOQ version
    edition.set(org.jooq.meta.Edition.OSS) // Or PRO, PRO_JAVA_11, etc. if you have a license

    configurations {
        create("main") { // Name of the configuration
            generateSchemaSourceOnCompilation.set(true) // Attempt to generate before Java compilation

            jdbc {
                driver.set("org.h2.Driver")
                url.set("jdbc:h2:mem:testdb_jooq_gen;DB_CLOSE_DELAY=-1;INIT=RUNSCRIPT FROM 'classpath:schema.sql'") // Example: use an init script
                user.set("sa")
                password.set("password")
            }
            generator {
                name.set("org.jooq.codegen.JavaGenerator")
                database {
                    name.set("org.jooq.meta.h2.H2Database")
                    inputSchema.set("PUBLIC") // Default H2 schema
                    // Forcing types for timestamp columns if they are created as TIMESTAMP WITH TIME ZONE by Hibernate
                    // but H2 driver or jOOQ interprets them as OffsetDateTime by default.
                    // This ensures they are treated as LocalDateTime if that's what your Java code expects.
                    forcedTypes.addAll(listOf(
                        org.jooq.meta.jaxb.ForcedType().apply {
                            userType.set("java.time.LocalDateTime")
                            includeTypes.set("TIMESTAMP.*") // Regex to match TIMESTAMP and TIMESTAMP WITH TIME ZONE
                            // For H2, "TIMESTAMP WITH TIME ZONE" is often the actual type for LocalDateTime with Hibernate
                        }
                    ))
                }
                target {
                    packageName.set("io.zabbixplus.framework.database.generated") // Package for generated classes
                    directory.set(project.layout.buildDirectory.dir("generated/sources/jooq/main/java").get().asFile.path)
                }
                strategy {
                    name.set("org.jooq.codegen.DefaultGeneratorStrategy")
                }
            }
        }
    }
}

// Add generated jOOQ sources to the main source set
sourceSets.main.get().java.srcDir(layout.buildDirectory.dir("generated/sources/jooq/main/java"))

// Ensure compileJava depends on jooqCodegen
tasks.named("compileJava") {
    dependsOn(tasks.withType(org.jooq.codegen.gradle. মানে.CodegenTask::class.java))
}
