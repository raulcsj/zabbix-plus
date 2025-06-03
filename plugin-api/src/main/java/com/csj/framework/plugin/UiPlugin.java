package com.csj.framework.plugin;

import java.util.List;
// Consider adding: import org.springframework.ui.Model; // If plugins need to contribute to model directly for their fragment
// Consider adding: import org.springframework.web.servlet.ModelAndView; // Alternative for more control

public interface UiPlugin extends Plugin {

    /**
     * Gets the primary UI entry point for this plugin.
     * For Thymeleaf, this could be a fragment identifier like "plugin-specific-templates/my-plugin-main :: content".
     * The actual template file (e.g., my-plugin-main.html) would be packaged within the plugin's JAR.
     * @return A string identifying the plugin's main UI view or fragment.
     */
    String getUiEntryPoint();

    /**
     * Provides a list of navigation items that this plugin wishes to add to the main application menu.
     * @return A list of {@link NavigationItem} objects. Should not be null; return empty list if no items.
     */
    List<NavigationItem> getNavigationItems();

    /**
     * Optional: Allows a plugin to add specific attributes to the Spring Model
     * right before its main UI entry point is rendered.
     * This method would be called by the main UI controller when rendering this specific plugin's UI.
     * @param model The Spring Model object to which attributes can be added.
     */
    // void prepareModelForUiEntryPoint(Model model); // Example of a more advanced feature
}
