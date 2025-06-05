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

    /**
     * Gets the base path for this plugin's static UI assets.
     * This path is typically relative to a common plugins asset directory,
     * e.g., "my-plugin/assets". The framework might use this to construct full URLs.
     * @return A string representing the assets path.
     */
    String getAssetsPath();

    /**
     * Gets the name of the entry component or module for this plugin's UI,
     * particularly if it's a micro-frontend that needs a specific bootstrap identifier.
     * This might be the same as getVueComponentName() or different depending on the architecture.
     * @return A string representing the entry component/module name.
     */
    String getEntryComponent();
}
