package io.zabbixplus.framework.exampleplugin;

import io.zabbixplus.framework.plugin.NavigationItem;
import io.zabbixplus.framework.plugin.PluginContext;
import io.zabbixplus.framework.plugin.UiPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleExamplePlugin implements UiPlugin {

    private static final Logger logger = LoggerFactory.getLogger(SimpleExamplePlugin.class);
    public static final String PLUGIN_NAME = "SimpleExamplePlugin";
    public static final String VUE_COMPONENT_NAME = "SimpleExamplePluginViewer"; // New constant

    private PluginContext pluginContext;

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
        logger.info(PLUGIN_NAME + " loaded.");
    }

    @Override
    public void init(PluginContext context) {
        this.pluginContext = context;
        logger.info(PLUGIN_NAME + " initialized.");
        if (context != null && context.getApplicationContext() != null) {
            logger.info("ApplicationContext ID: {}", context.getApplicationContext().getId());
            logger.info("ApplicationName: {}", context.getApplicationContext().getApplicationName());
        } else {
            logger.warn("PluginContext or ApplicationContext is null.");
        }

        if (context != null && context.getConfiguration() != null) {
            logger.info("Plugin configuration empty? {}", context.getConfiguration().isEmpty());
            for (Map.Entry<String, Object> entry : context.getConfiguration().entrySet()) {
                logger.info("Config: {} = {}", entry.getKey(), entry.getValue());
            }
        } else {
            logger.warn("PluginContext or Configuration is null.");
        }
    }

    @Override
    public void unload() {
        logger.info(PLUGIN_NAME + " unloaded.");
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

    @Override
    public Map<String, Object> getUiMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("mainComponent", getVueComponentName());
        metadata.put("pluginName", getName());
        metadata.put("bundleUrl", "/plugins/" + getName() + "/remoteEntry.js");
        // Add other metadata as needed
        return metadata;
    }
}
