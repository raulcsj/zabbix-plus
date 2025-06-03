plugins {
    `java-library`
}

dependencies {
    // No Spring related dependencies for plugin-api itself
    // Plugins can choose to use Spring if they are loaded into a Spring context,
    // but the API itself is plain Java.
    // Example if ApplicationContext was part of the Plugin interface:
    // api("org.springframework:spring-context:6.1.1") // 'api' so it's on the compile classpath of dependent projects
}
