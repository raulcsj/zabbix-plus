package io.zabbixplus.framework.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);

    // Assuming the application is launched from the root directory of the distribution
    // where 'ui' is a subdirectory.
    private static final String EXTERNAL_UI_PATH = "file:./ui/";
    private static final String CLASSPATH_UI_PATH = "classpath:/static/"; // Default Spring Boot static path

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uiPath = Paths.get("./ui").toAbsolutePath().normalize().toString();
        logger.info("Attempting to serve static UI content. Configured external UI directory: {}", uiPath);


        // Serve static assets from the external 'ui/' directory.
        // This will be the primary location for UI assets in the packaged distribution.
        // "/**" matches all requests. Spring Boot orders resource handlers,
        // so this should be general enough. Specific controllers will still take precedence.
        registry.addResourceHandler("/**")
                .addResourceLocations(EXTERNAL_UI_PATH)
                .setCachePeriod(0); // Disable caching for development, configure for production
        logger.info("Registered resource handler for external UI: path='/**', locations='{}'", EXTERNAL_UI_PATH);

        // As a fallback, or for development when UI assets might be on classpath (e.g. from main-ui.jar),
        // keep the default Spring Boot static resource location.
        // Spring Boot auto-configures this, but explicitly adding it ensures clarity or if auto-config is modified.
        // This handler has lower precedence if added after a more specific one or if the external one resolves first.
        // However, Spring Boot's default static resource handling is quite robust.
        // We might not even need to explicitly add this if Spring Boot's default is sufficient as a fallback.
        // For a packaged app, we primarily rely on EXTERNAL_UI_PATH.

        // registry.addResourceHandler("/**")
        //        .addResourceLocations(CLASSPATH_UI_PATH);
        // logger.info("Also registered resource handler for classpath UI (fallback): path='/**', locations='{}'", CLASSPATH_UI_PATH);


        // Example of a more specific path if you don't want '/**' for the external UI
        /*
        registry.addResourceHandler("/assets/**") // If your assets are under /assets path in URL
                .addResourceLocations(EXTERNAL_UI_PATH + "assets/") // And map to ui/assets/ directory
                .setCachePeriod(0);
        registry.addResourceHandler("/index.html", "/") // To serve index.html from root
                .addResourceLocations(EXTERNAL_UI_PATH + "index.html")
                .setCachePeriod(0);
        */
    }

    // Other web configurations can go here (interceptors, formatters, etc.)
    // For example, if RequestLoggingInterceptor was previously configured here, keep it.
}
