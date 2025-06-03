package com.csj.framework.ui.controller;

import com.csj.framework.core.plugin.PluginService;
import com.csj.framework.plugin.Plugin; // From plugin-api
import com.csj.framework.plugin.UiPlugin; // From plugin-api
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class PluginListController {

    private final PluginService pluginService;

    @Autowired
    public PluginListController(PluginService pluginService) {
        this.pluginService = pluginService;
    }

    @GetMapping("/ui/plugins/list")
    public String listLoadedPlugins(Model model) {
        // Get all loaded plugins
        Map<String, Plugin> plugins = pluginService.getLoadedPlugins();

        // For display, perhaps filter or transform them
        // Here, we'll just pass their names and whether they are UiPlugins
        Map<String, String> pluginInfo = plugins.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().getDescription() +
                         (entry.getValue() instanceof UiPlugin ? " (UiPlugin)" : " (Generic Plugin)")
            ));

        model.addAttribute("loadedPlugins", pluginInfo);
        model.addAttribute("pluginDirectory", pluginService.getPluginDirectoryPath());
        return "system/plugins-list"; // Path to a new Thymeleaf template
    }
}
