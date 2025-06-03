package io.zabbixplus.framework.plugin;

import java.util.List;
import java.util.Map; // For potential future getUiMetadata()

public interface UiPlugin extends Plugin {

    /**
     * Gets the name of the main Vue.js component that serves as the entry point for this plugin's UI.
     * This component is expected to be registered (e.g., globally or dynamically loaded)
     * within the main Vue.js application.
     * @return A string representing the Vue.js component name (e.g., "ExamplePluginDashboard").
     */
    String getVueComponentName(); // Changed from getUiEntryPoint

    /**
     * Provides a list of navigation items that this plugin wishes to add to the main application menu.
     * The 'path' in each NavigationItem should correspond to a route defined in the main Vue.js application's
     * router (Vue Router), which will typically render the component specified by {@link #getVueComponentName()}
     * or another component from this plugin.
     * @return A list of {@link NavigationItem} objects. Should not be null; return empty list if no items.
     */
    List<NavigationItem> getNavigationItems();

    /**
     * Optional: Provides metadata for the plugin's UI, such as information about
     * how to load its Vue.js component bundle if not globally registered.
     * For now, this is conceptual.
     * @return A map of UI metadata (e.g., { "bundleUrl": "/plugins/myplugin/bundle.js" })
     */
    Map<String, Object> getUiMetadata();
}
