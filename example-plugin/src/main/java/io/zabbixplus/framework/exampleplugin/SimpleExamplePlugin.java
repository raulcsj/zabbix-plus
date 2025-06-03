package io.zabbixplus.framework.exampleplugin;

import io.zabbixplus.framework.plugin.NavigationItem;
import io.zabbixplus.framework.plugin.UiPlugin;

import java.util.Collections;
import java.util.List;

public class SimpleExamplePlugin implements UiPlugin {

    public static final String PLUGIN_NAME = "SimpleExamplePlugin";
    public static final String VUE_COMPONENT_NAME = "SimpleExamplePluginViewer"; // New constant

    @Override
    public String getName() {
        return PLUGIN_NAME;
    }

    @Override
    public String getDescription() {
        return "A simple example plugin that demonstrates Vue.js UI contributions.";
    }

    @Override
    public void load() {
        System.out.println(PLUGIN_NAME + " (UiPlugin for Vue) loaded successfully!");
    }

    @Override
    public void unload() {
        System.out.println(PLUGIN_NAME + " (UiPlugin for Vue) unloaded.");
    }

    @Override
    public String getVueComponentName() { // Changed from getUiEntryPoint
        return VUE_COMPONENT_NAME;
    }

    @Override
    public List<NavigationItem> getNavigationItems() {
        return Collections.singletonList(
            new NavigationItem(
                "Example Plugin (Vue)",
                "/ui/plugin/" + PLUGIN_NAME, // Path handled by Vue Router
                "fas fa-puzzle-piece"
            )
        );
    }
}
