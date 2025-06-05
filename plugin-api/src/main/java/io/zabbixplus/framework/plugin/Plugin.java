package io.zabbixplus.framework.plugin; // Updated package

// Potentially add ApplicationContext or other framework services if plugins need them
// import org.springframework.context.ApplicationContext;

public interface Plugin {
    String getPluginId();       // Unique identifier for the plugin
    String getPluginName();     // Human-readable name of the plugin
    String getVendor();         // Vendor of the plugin
    String getVersion();        // Version of the plugin
    String getDescription();
    void load(); // Called when the plugin is loaded
    void init(PluginContext context);
    void unload(); // Called when the plugin is gracefully unloaded

    // Optional: if plugins need access to core Spring context
    // void initialize(ApplicationContext context);
}
