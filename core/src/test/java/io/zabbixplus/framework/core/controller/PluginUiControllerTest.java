package io.zabbixplus.framework.core.controller;

import io.zabbixplus.framework.plugin.Plugin;
import io.zabbixplus.framework.plugin.UiPlugin;
import io.zabbixplus.framework.core.plugin.PluginService; // Corrected path
import io.zabbixplus.framework.core.web.dto.PluginClientInfo; // Corrected path
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap; // Added for Map
import java.util.List;
import java.util.Map; // Added for Map

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient; // Added for lenient stubbing

@ExtendWith(MockitoExtension.class)
class PluginUiControllerTest {

    @Mock
    private PluginService pluginService;

    @InjectMocks
    private PluginUiController pluginUiController;

    @Mock
    private UiPlugin uiPlugin1;

    @Mock
    private UiPlugin uiPlugin2;

    @Mock
    private Plugin genericPlugin; // A plugin that is not a UiPlugin

    @BeforeEach
    void setUp() {
        // Basic setup for UiPlugin mocks
        // These methods are now on Plugin or UiPlugin interface
        // Using lenient() to avoid UnnecessaryStubbingException for stubs not used in all tests
        lenient().when(uiPlugin1.getPluginId()).thenReturn("plugin1");
        lenient().when(uiPlugin1.getPluginName()).thenReturn("Plugin One");
        lenient().when(uiPlugin1.getVendor()).thenReturn("Vendor A");
        lenient().when(uiPlugin1.getVersion()).thenReturn("1.0.0");
        lenient().when(uiPlugin1.getDescription()).thenReturn("Description for Plugin One");
        lenient().when(uiPlugin1.getAssetsPath()).thenReturn("/assets/plugin1");
        lenient().when(uiPlugin1.getEntryComponent()).thenReturn("plugin-one-entry");
        lenient().when(uiPlugin1.getRequiredPrivileges()).thenReturn(Collections.singletonList("PRIV1"));
        lenient().when(uiPlugin1.getNavigationItems()).thenReturn(Collections.emptyList()); // Mock nav items
        lenient().when(uiPlugin1.getUiMetadata()).thenReturn(new HashMap<>()); // Mock UI metadata

        lenient().when(uiPlugin2.getPluginId()).thenReturn("plugin2");
        lenient().when(uiPlugin2.getPluginName()).thenReturn("Plugin Two");
        lenient().when(uiPlugin2.getVendor()).thenReturn("Vendor B");
        lenient().when(uiPlugin2.getVersion()).thenReturn("1.0.1");
        lenient().when(uiPlugin2.getAssetsPath()).thenReturn("/assets/plugin2");
        lenient().when(uiPlugin2.getEntryComponent()).thenReturn("plugin-two-entry");
        lenient().when(uiPlugin2.getNavigationItems()).thenReturn(Collections.emptyList());
        lenient().when(uiPlugin2.getUiMetadata()).thenReturn(new HashMap<>());

        // Stub for genericPlugin's getPluginId as it's used in map keys
        lenient().when(genericPlugin.getPluginId()).thenReturn("generic-plugin");
    }

    @Test
    void testGetPluginUiMetadata_NoUiPluginsLoaded() {
        Map<String, Plugin> loadedPluginsMap = new HashMap<>();
        loadedPluginsMap.put(genericPlugin.getPluginId(), genericPlugin); // Assuming genericPlugin has getPluginId
        when(pluginService.getLoadedPlugins()).thenReturn(loadedPluginsMap);

        List<PluginClientInfo> uiPlugins = pluginUiController.getPluginUiMetadata(); // Changed method name
        assertNotNull(uiPlugins);
        assertTrue(uiPlugins.isEmpty());
    }

    @Test
    void testGetPluginUiMetadata_OneUiPluginLoaded() {
        Map<String, Plugin> loadedPluginsMap = new HashMap<>();
        loadedPluginsMap.put(uiPlugin1.getPluginId(), uiPlugin1);
        when(pluginService.getLoadedPlugins()).thenReturn(loadedPluginsMap);

        List<PluginClientInfo> uiPlugins = pluginUiController.getPluginUiMetadata(); // Changed method name

        assertNotNull(uiPlugins);
        assertEquals(1, uiPlugins.size());
        PluginClientInfo info = uiPlugins.get(0);
        assertEquals("plugin1", info.getPluginId());
        assertEquals("Plugin One", info.getPluginName());
        assertEquals("Vendor A", info.getVendor());
        assertEquals("1.0.0", info.getVersion());
        assertEquals("/assets/plugin1", info.getAssetsPath());
        assertEquals("plugin-one-entry", info.getEntryComponent());
        // RequiredPrivileges assertion could be added if needed
    }

    @Test
    void testGetPluginUiMetadata_MultipleUiPluginsLoaded() {
        // Need getPluginId() on genericPlugin for this map construction
        when(genericPlugin.getPluginId()).thenReturn("genericPlugin");

        Map<String, Plugin> loadedPluginsMap = new HashMap<>();
        loadedPluginsMap.put(uiPlugin1.getPluginId(), uiPlugin1);
        loadedPluginsMap.put(genericPlugin.getPluginId(), genericPlugin);
        loadedPluginsMap.put(uiPlugin2.getPluginId(), uiPlugin2);
        when(pluginService.getLoadedPlugins()).thenReturn(loadedPluginsMap);

        List<PluginClientInfo> uiPlugins = pluginUiController.getPluginUiMetadata(); // Changed method name

        assertNotNull(uiPlugins);
        assertEquals(2, uiPlugins.size()); // Only UiPlugin instances should be mapped

        PluginClientInfo info1 = uiPlugins.stream().filter(p -> "plugin1".equals(p.getPluginId())).findFirst().orElse(null);
        assertNotNull(info1);
        assertEquals("Plugin One", info1.getPluginName());

        PluginClientInfo info2 = uiPlugins.stream().filter(p -> "plugin2".equals(p.getPluginId())).findFirst().orElse(null);
        assertNotNull(info2);
        assertEquals("Plugin Two", info2.getPluginName());
    }

    @Test
    void testGetPluginUiMetadata_CorrectMappingToPluginClientInfo() {
        Map<String, Plugin> loadedPluginsMap = new HashMap<>();
        loadedPluginsMap.put(uiPlugin1.getPluginId(), uiPlugin1);
        when(pluginService.getLoadedPlugins()).thenReturn(loadedPluginsMap);
        // Ensure all methods called by PluginUiController's mapping logic are mocked on uiPlugin1 in setUp()

        List<PluginClientInfo> uiPlugins = pluginUiController.getPluginUiMetadata(); // Changed method name

        assertEquals(1, uiPlugins.size());
        PluginClientInfo info = uiPlugins.get(0);

        // Verify all fields are correctly mapped
        assertEquals(uiPlugin1.getPluginId(), info.getPluginId());
        assertEquals(uiPlugin1.getPluginName(), info.getPluginName());
        assertEquals(uiPlugin1.getVendor(), info.getVendor());
        assertEquals(uiPlugin1.getVersion(), info.getVersion());
        assertEquals(uiPlugin1.getDescription(), info.getDescription());
        assertEquals(uiPlugin1.getAssetsPath(), info.getAssetsPath());
        assertEquals(uiPlugin1.getEntryComponent(), info.getEntryComponent());
        assertEquals(uiPlugin1.getRequiredPrivileges(), info.getRequiredPrivileges());
        // also check uiMetadata and navigationItems if the controller copies them directly
        assertEquals(uiPlugin1.getUiMetadata(), info.getUiMetadata());
        assertEquals(uiPlugin1.getNavigationItems(), info.getNavigationItems());
    }

    @Test
    void testGetPluginUiMetadata_NoPluginsLoadedAtAll() {
        when(pluginService.getLoadedPlugins()).thenReturn(Collections.emptyMap()); // Return empty map
        List<PluginClientInfo> uiPlugins = pluginUiController.getPluginUiMetadata(); // Changed method name
        assertNotNull(uiPlugins);
        assertTrue(uiPlugins.isEmpty());
    }
}
