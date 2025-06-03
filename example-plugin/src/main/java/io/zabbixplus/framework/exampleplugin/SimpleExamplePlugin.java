package io.zabbixplus.framework.exampleplugin;

import io.zabbixplus.framework.plugin.NavigationItem;
import io.zabbixplus.framework.plugin.PluginContext;
import io.zabbixplus.framework.plugin.UiPlugin;
import io.zabbixplus.framework.core.service.ExampleTableService; // Added import
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException; // Added import
import org.springframework.context.ApplicationContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleExamplePlugin implements UiPlugin {

    private static final Logger logger = LoggerFactory.getLogger(SimpleExamplePlugin.class);

    public static final String PLUGIN_NAME = "SimpleExamplePlugin";
    public static final String VUE_COMPONENT_NAME = "ExamplePluginDashboard";

    private ApplicationContext applicationContext;
    private Map<String, Object> pluginConfiguration;
    private ExampleTableService exampleTableService; // Added field

    @Override
    public String getName() {
        return PLUGIN_NAME;
    }

    @Override
    public String getDescription() {
        return "A simple example plugin that demonstrates Vue.js UI contributions and backend logic.";
    }

    @Override
    public void load() {
        logger.info("{} (UiPlugin for Vue) loaded.", PLUGIN_NAME);
    }

    @Override
    public void init(PluginContext context) {
        this.applicationContext = context.getApplicationContext();
        this.pluginConfiguration = context.getConfiguration();
        logger.info("{} initialized. ApplicationContext: {}, Configuration: {}",
                PLUGIN_NAME,
                this.applicationContext != null ? this.applicationContext.getId() : "null",
                this.pluginConfiguration);

        if (this.applicationContext != null) {
            try {
                this.exampleTableService = this.applicationContext.getBean(ExampleTableService.class);
                logger.info("Successfully retrieved ExampleTableService bean from core.");
            } catch (NoSuchBeanDefinitionException e) {
                logger.error("Could not find ExampleTableService bean in ApplicationContext. Database operations will not be available.", e);
            }
        } else {
            logger.warn("ApplicationContext is null in plugin. ExampleTableService cannot be retrieved.");
        }
    }

    @Override
    public void unload() {
        logger.info("{} (UiPlugin for Vue) unloaded.", PLUGIN_NAME);
    }

    @Override
    public String getVueComponentName() {
        return VUE_COMPONENT_NAME;
    }

    @Override
    public List<NavigationItem> getNavigationItems() {
        return Collections.singletonList(
            new NavigationItem(
                "Example Dashboard",
                "/ui/plugin/" + PLUGIN_NAME,
                "fas fa-tachometer-alt"
            )
        );
    }

    @Override
    public Map<String, Object> getUiMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("mainComponent", VUE_COMPONENT_NAME);
        metadata.put("pluginName", PLUGIN_NAME);
        metadata.put("bundleUrl", "/plugins/" + PLUGIN_NAME + "/remoteEntry.js");
        metadata.put("description", getDescription());
        return metadata;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    // New methods for data interaction
    public List<Map<String, Object>> getPluginDataRecords() {
        if (exampleTableService == null) {
            logger.warn("ExampleTableService not available. Cannot fetch records.");
            return Collections.emptyList();
        }
        return exampleTableService.getRecords();
    }

    public void addPluginDataRecord(Map<String, String> dataPayload) {
        if (exampleTableService == null) {
            logger.warn("ExampleTableService not available. Cannot add record.");
            return; // Or throw an exception
        }
        String name = dataPayload.get("name");
        if (name != null && !name.trim().isEmpty()) {
            exampleTableService.createRecord(name);
            logger.info("Record added via plugin: {}", name);
        } else {
            logger.warn("No 'name' provided in payload for addPluginDataRecord. Record not added.");
        }
    }
}
