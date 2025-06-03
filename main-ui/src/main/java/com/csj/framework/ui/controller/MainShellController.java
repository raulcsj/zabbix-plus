package com.csj.framework.ui.controller;

import com.csj.framework.core.plugin.PluginService;
import com.csj.framework.plugin.NavigationItem;
import com.csj.framework.plugin.Plugin;
import com.csj.framework.plugin.UiPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class MainShellController {

    private static final Logger logger = LoggerFactory.getLogger(MainShellController.class);
    private final PluginService pluginService;

    @Autowired
    public MainShellController(PluginService pluginService) {
        this.pluginService = pluginService;
    }

    // Helper method to add common model attributes (like navigation)
    private void addCommonAttributes(Model model) {
        List<NavigationItem> navItems = pluginService.getLoadedPlugins().values().stream()
            .filter(UiPlugin.class::isInstance)
            .map(UiPlugin.class::cast)
            .flatMap(uiPlugin -> {
                try {
                    List<NavigationItem> items = uiPlugin.getNavigationItems();
                    return items == null ? Collections.<NavigationItem>emptyList().stream() : items.stream();
                } catch (Exception e) {
                    logger.error("Error getting navigation items from plugin: " + uiPlugin.getName(), e);
                    return Collections.<NavigationItem>emptyList().stream();
                }
            })
            .collect(Collectors.toList());
        model.addAttribute("navItems", navItems);
    }

    @GetMapping("/")
    public String homePage(Model model) {
        addCommonAttributes(model);
        model.addAttribute("pageTitle", "Welcome");
        model.addAttribute("pageContentFragment", "fragments/welcome :: content"); // A new welcome fragment
        return "main-shell"; // Renders src/main/resources/templates/main-shell.html
    }

    // This endpoint will handle showing a plugin's specific UI
    // It will render the main-shell and tell it which plugin fragment to include.
    @GetMapping("/ui/show/plugin/{pluginName}")
    public String showPluginPage(@PathVariable String pluginName, Model model) {
        addCommonAttributes(model);
        Plugin plugin = pluginService.getPlugin(pluginName);

        if (plugin instanceof UiPlugin) {
            UiPlugin uiPlugin = (UiPlugin) plugin;
            model.addAttribute("pageTitle", uiPlugin.getName());
            // The getUiEntryPoint() should give something like "plugin-templates/plugin-main :: content"
            String entryPoint = uiPlugin.getUiEntryPoint();
            logger.info("Plugin {} UI Entry Point: {}", uiPlugin.getName(), entryPoint);
            model.addAttribute("pageContentFragment", entryPoint);
            model.addAttribute("currentPluginName", uiPlugin.getName()); // For context in the plugin template
        } else {
            model.addAttribute("pageTitle", "Plugin Not Found");
            model.addAttribute("pageContentFragment", "fragments/error :: plugin-not-found");
            String errorMessage = "Plugin '" + pluginName + "' not found or is not a UI plugin.";
            if (plugin != null) {
                errorMessage += " (Found generic plugin: " + plugin.getName() + ")";
            }
            logger.warn(errorMessage);
            model.addAttribute("errorMessage", errorMessage);
        }
        return "main-shell";
    }
}
