package io.zabbixplus.framework.exampleplugin.controller;

import io.zabbixplus.framework.core.plugin.PluginService;
import io.zabbixplus.framework.exampleplugin.SimpleExamplePlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class ExamplePluginApiController {

    private static final Logger logger = LoggerFactory.getLogger(ExamplePluginApiController.class);

    private final PluginService pluginService;

    @Autowired
    public ExamplePluginApiController(PluginService pluginService) {
        this.pluginService = pluginService;
    }

    private SimpleExamplePlugin getPluginInstance() {
        io.zabbixplus.framework.plugin.Plugin plugin = pluginService.getPlugin(SimpleExamplePlugin.PLUGIN_NAME);
        if (plugin instanceof SimpleExamplePlugin) {
            return (SimpleExamplePlugin) plugin;
        }
        return null;
    }

    @GetMapping("/api/plugins/simpleexampleplugin/data")
    public ResponseEntity<?> getExampleData() {
        SimpleExamplePlugin pluginInstance = getPluginInstance();
        if (pluginInstance == null) {
            logger.warn("SimpleExamplePlugin instance not found for getExampleData(). This might happen if the plugin is not loaded or failed to initialize.");
            // It's important to also check if exampleTableService was initialized within the plugin
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Plugin not available or not properly initialized.");
        }
        try {
            // Directly call the method that uses the service
            List<Map<String, Object>> data = pluginInstance.getPluginDataRecords();
            // Check if the service was available - getPluginDataRecords returns empty list if not.
            // A more explicit check might be needed if getPluginDataRecords could return empty for other reasons.
            // For now, assume an empty list might also mean the service wasn't available.
            // Consider if the plugin should throw an exception if service is null to be caught here.
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            logger.error("Error fetching data via SimpleExamplePlugin", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching data: " + e.getMessage());
        }
    }

    @PostMapping("/api/plugins/simpleexampleplugin/data")
    public ResponseEntity<?> addExampleData(@RequestBody Map<String, String> payload) {
        SimpleExamplePlugin pluginInstance = getPluginInstance();
        if (pluginInstance == null) {
            logger.warn("SimpleExamplePlugin instance not found for addExampleData(). This might happen if the plugin is not loaded or failed to initialize.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Plugin not available or not properly initialized.");
        }
        try {
            // The addPluginDataRecord method in SimpleExamplePlugin already logs if service is unavailable.
            // It currently doesn't throw an exception, so this call will proceed, and service will handle it.
            pluginInstance.addPluginDataRecord(payload);
            // Check if 'name' was actually provided, as addPluginDataRecord handles missing 'name' internally.
            if (payload == null || payload.get("name") == null || payload.get("name").trim().isEmpty()) {
                 return ResponseEntity.badRequest().body("Missing 'name' in payload.");
            }
            return ResponseEntity.ok().body(Map.of("message", "Data processing initiated via plugin. Check server logs for confirmation."));
        } catch (Exception e) {
            // This catch block would handle unexpected errors from the plugin logic itself,
            // not necessarily from the ExampleTableService if it's handled within the plugin.
            logger.error("Error adding data via SimpleExamplePlugin", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding data: " + e.getMessage());
        }
    }
}
