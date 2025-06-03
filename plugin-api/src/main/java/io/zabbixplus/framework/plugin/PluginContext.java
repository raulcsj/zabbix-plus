package io.zabbixplus.framework.plugin;

import org.springframework.context.ApplicationContext;
import java.util.Map;

public class PluginContext {

    private final ApplicationContext applicationContext;
    private final Map<String, Object> configuration;

    public PluginContext(ApplicationContext applicationContext, Map<String, Object> configuration) {
        this.applicationContext = applicationContext;
        this.configuration = configuration;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public Map<String, Object> getConfiguration() {
        return configuration;
    }
}
