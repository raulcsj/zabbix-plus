package com.csj.framework.ui;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// Ensure component scan can find core services if they are not in a sub-package of this.
// For now, assuming direct autowiring of core beans will work if core is a library with @Service.
// If PluginService from core is not found later, add:
// @SpringBootApplication(scanBasePackages = {"com.csj.framework.ui", "com.csj.framework.core"})

// To ensure Core services (like PluginService, ExampleTableService) are found,
// and also its configurations (like WebConfig for interceptors),
// it's better to specify scanBasePackages to include com.csj.framework.core.
// The JooqConfig (if it were added) and other DB related beans are typically found via auto-configuration
// triggered by spring-boot-starter-data-jpa and properties.
@SpringBootApplication(scanBasePackages = {"com.csj.framework.ui", "com.csj.framework.core"})
public class MainUIApplication {
    public static void main(String[] args) {
        SpringApplication.run(MainUIApplication.class, args);
    }
}
