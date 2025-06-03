package io.zabbixplus.framework.core.plugin; // Updated package

import io.zabbixplus.framework.plugin.Plugin; // Updated import from plugin-api
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class PluginService {
    private static final Logger logger = LoggerFactory.getLogger(PluginService.class);
    private final Map<String, Plugin> loadedPlugins = new ConcurrentHashMap<>();
    private final List<URLClassLoader> pluginClassLoaders = new ArrayList<>();

    @Value("${framework.plugin.directory:./plugins}")
    private String pluginDirectoryPath;

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
                    if (loadedPlugins.containsKey(plugin.getName())) {
                        logger.warn("Plugin with name '{}' already loaded. Skipping duplicate from {}.", plugin.getName(), pluginFile.getName());
                        continue;
                    }
                    plugin.load();
                    loadedPlugins.put(plugin.getName(), plugin);
                    logger.info("Successfully loaded plugin: {} - {}", plugin.getName(), plugin.getDescription());
                }
            } catch (Exception e) {
                logger.error("Failed to load plugin from file: " + pluginFile.getName(), e);
            }
        }
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
