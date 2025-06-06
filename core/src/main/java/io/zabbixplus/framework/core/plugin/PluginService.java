package io.zabbixplus.framework.core.plugin; // Updated package

import io.zabbixplus.framework.plugin.Plugin; // Updated import from plugin-api
import io.zabbixplus.framework.plugin.PluginContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class PluginService {
    private static final Logger logger = LoggerFactory.getLogger(PluginService.class);
    private final Map<String, Plugin> loadedPlugins = new ConcurrentHashMap<>();
    private final List<URLClassLoader> pluginClassLoaders = new ArrayList<>();
    private final ApplicationContext applicationContext;

    @Value("${framework.plugin.directory:./plugins}")
    private String pluginDirectoryPath;

    public PluginService(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void loadPlugins() {
        File pluginDir = new File(pluginDirectoryPath);
        if (!pluginDir.exists() || !pluginDir.isDirectory()) {
            logger.warn("Plugin directory '{}' does not exist or is not a directory. Creating it.", pluginDirectoryPath);
            if (!pluginDir.mkdirs()) {
                logger.error("Could not create plugin directory '{}'. No plugins will be loaded.", pluginDirectoryPath);
                return;
            }
        }

        File[] pluginFiles = pluginDir.listFiles((dir, name) -> name.endsWith(".jar"));

        if (pluginFiles == null || pluginFiles.length == 0) {
            logger.info("No plugins found in directory '{}'.", pluginDirectoryPath);
            return;
        }

        for (File pluginFile : pluginFiles) {
            try {
                logger.info("Attempting to load plugin from: {}", pluginFile.getAbsolutePath());
                URL pluginUrl = pluginFile.toURI().toURL();
                URLClassLoader pluginClassLoader = new URLClassLoader(new URL[]{pluginUrl}, getClass().getClassLoader());
                pluginClassLoaders.add(pluginClassLoader);

                ServiceLoader<Plugin> serviceLoader = ServiceLoader.load(Plugin.class, pluginClassLoader);
                for (Plugin plugin : serviceLoader) {
                    if (loadedPlugins.containsKey(plugin.getPluginName())) {
                        logger.warn("Plugin with name '{}' already loaded. Skipping duplicate from {}.", plugin.getPluginName(), pluginFile.getName());
                        continue;
                    }
                    plugin.load(); // Existing load call

                    // Attempt to load plugin-specific configuration
                    Map<String, Object> pluginConfig = loadPluginConfiguration(pluginClassLoader);

                    // Create PluginContext
                    PluginContext pluginContext = new PluginContext(applicationContext, pluginConfig);

                    // Initialize plugin with context
                    plugin.init(pluginContext);

                    loadedPlugins.put(plugin.getPluginName(), plugin);
                    logger.info("Successfully loaded and initialized plugin: {} - {}", plugin.getPluginName(), plugin.getDescription());
                }
            } catch (Exception e) {
                logger.error("Failed to load or initialize plugin from file: " + pluginFile.getName(), e);
            }
        }
    }

    private Map<String, Object> loadPluginConfiguration(URLClassLoader pluginClassLoader) {
        Yaml yaml = new Yaml();
        InputStream configStream = pluginClassLoader.getResourceAsStream("config.yml");
        if (configStream == null) {
            configStream = pluginClassLoader.getResourceAsStream("config.yaml");
        }

        if (configStream != null) {
            try {
                Map<String, Object> config = yaml.load(configStream);
                logger.info("Successfully loaded configuration for plugin.");
                return config != null ? config : Collections.emptyMap();
            } catch (Exception e) {
                logger.warn("Failed to parse config.yml/config.yaml for plugin. Using empty configuration.", e);
            }
        } else {
            logger.info("No config.yml or config.yaml found for plugin. Using empty configuration.");
        }
        return Collections.emptyMap();
    }

    public Map<String, Plugin> getLoadedPlugins() {
        return new ConcurrentHashMap<>(loadedPlugins);
    }

    public Plugin getPlugin(String name) {
        return loadedPlugins.get(name);
    }

    public String getPluginDirectoryPath() {
        return pluginDirectoryPath;
    }

    @PreDestroy
    public void unloadPlugins() {
        logger.info("Unloading all plugins...");
        loadedPlugins.forEach((name, plugin) -> {
            try {
                plugin.unload();
                logger.info("Unloaded plugin: {}", name);
            } catch (Exception e) {
                logger.error("Error unloading plugin: " + name, e);
            }
        });
        loadedPlugins.clear();
        for (URLClassLoader classLoader : pluginClassLoaders) {
            try {
                classLoader.close();
                logger.info("Closed plugin classloader: {}", classLoader);
            } catch (Exception e) {
                logger.error("Error closing plugin classloader", e);
            }
        }
        pluginClassLoaders.clear();
        logger.info("All plugins unloaded and classloaders closed.");
    }
}
