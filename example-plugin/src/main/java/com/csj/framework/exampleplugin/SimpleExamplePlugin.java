package com.csj.framework.exampleplugin;

import com.csj.framework.plugin.NavigationItem;
import com.csj.framework.plugin.UiPlugin; // Import UiPlugin

import java.util.Collections;
import java.util.List;

public class SimpleExamplePlugin implements UiPlugin { // Implement UiPlugin

    public static final String PLUGIN_NAME = "SimpleExamplePlugin"; // Make name a constant

    @Override
    public String getName() {
        return PLUGIN_NAME;
    }

    @Override
    public String getDescription() {
        return "A simple example plugin that demonstrates UI contributions.";
    }

    @Override
    public void load() {
        System.out.println(PLUGIN_NAME + " (UiPlugin) loaded successfully!");
    }

    @Override
    public void unload() {
        System.out.println(PLUGIN_NAME + " (UiPlugin) unloaded.");
    }

    @Override
    public String getUiEntryPoint() {
        // For now, this can be a placeholder. It will be used when rendering the plugin's actual UI.
        // It refers to a Thymeleaf fragment: "templates/example-plugin-main.html :: content"
        // The HTML file would be in: example-plugin/src/main/resources/templates/example-plugin-main.html
        // Convention: pluginName-templates/templateName :: fragmentName
        // To make it more specific to the plugin JAR structure, let's use a path that implies it's inside the plugin.
        return "example-plugin-templates/example-plugin-main :: content";
    }

    @Override
    public List<NavigationItem> getNavigationItems() {
        // Define a navigation item for this plugin
        // The path should be unique and ideally link to a page in main-ui that can render this plugin's UI.
        return Collections.singletonList(
            new NavigationItem(
                "Example Plugin UI", // Display name in navigation
                "/ui/show/plugin/" + PLUGIN_NAME, // Path to view this plugin's UI
                "fas fa-puzzle-piece" // Example Font Awesome icon class (ensure you have Font Awesome or similar if you want to see icons)
            )
        );
    }
}
