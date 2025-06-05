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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

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
        when(uiPlugin1.getPluginId()).thenReturn("plugin1");
        when(uiPlugin1.getPluginName()).thenReturn("Plugin One");
        when(uiPlugin1.getVendor()).thenReturn("Vendor A");
        when(uiPlugin1.getVersion()).thenReturn("1.0.0");
        when(uiPlugin1.getAssetsPath()).thenReturn("/assets/plugin1");
        when(uiPlugin1.getEntryComponent()).thenReturn("plugin-one-entry");

        // Basic setup for uiPlugin2 if it's used in a multi-plugin test
        when(uiPlugin2.getPluginId()).thenReturn("plugin2");
        when(uiPlugin2.getPluginName()).thenReturn("Plugin Two");
        when(uiPlugin2.getVendor()).thenReturn("Vendor B");
        when(uiPlugin2.getVersion()).thenReturn("1.0.1");
        when(uiPlugin2.getAssetsPath()).thenReturn("/assets/plugin2");
        when(uiPlugin2.getEntryComponent()).thenReturn("plugin-two-entry");
    }

    @Test
    void testGetUiPlugins_NoUiPluginsLoaded() {
        when(pluginService.getLoadedPlugins()).thenReturn(Collections.singletonList(genericPlugin));
        List<PluginClientInfo> uiPlugins = pluginUiController.getUiPlugins();
        assertNotNull(uiPlugins);
        assertTrue(uiPlugins.isEmpty());
    }

    @Test
    void testGetUiPlugins_OneUiPluginLoaded() {
        when(pluginService.getLoadedPlugins()).thenReturn(Collections.singletonList(uiPlugin1));

        List<PluginClientInfo> uiPlugins = pluginUiController.getUiPlugins();

        assertNotNull(uiPlugins);
        assertEquals(1, uiPlugins.size());
        PluginClientInfo info = uiPlugins.get(0);
        assertEquals("plugin1", info.getPluginId());
        assertEquals("Plugin One", info.getPluginName());
        assertEquals("Vendor A", info.getVendor());
        assertEquals("1.0.0", info.getVersion());
        assertEquals("/assets/plugin1", info.getAssetsPath());
        assertEquals("plugin-one-entry", info.getEntryComponent());
    }

    @Test
    void testGetUiPlugins_MultipleUiPluginsLoaded() {
        when(pluginService.getLoadedPlugins()).thenReturn(Arrays.asList(uiPlugin1, genericPlugin, uiPlugin2));

        List<PluginClientInfo> uiPlugins = pluginUiController.getUiPlugins();

        assertNotNull(uiPlugins);
        assertEquals(2, uiPlugins.size());

        PluginClientInfo info1 = uiPlugins.stream().filter(p -> "plugin1".equals(p.getPluginId())).findFirst().orElse(null);
        assertNotNull(info1);
        assertEquals("Plugin One", info1.getPluginName());

        PluginClientInfo info2 = uiPlugins.stream().filter(p -> "plugin2".equals(p.getPluginId())).findFirst().orElse(null);
        assertNotNull(info2);
        assertEquals("Plugin Two", info2.getPluginName());
    }

    @Test
    void testGetUiPlugins_CorrectMappingToPluginClientInfo() {
        when(pluginService.getLoadedPlugins()).thenReturn(Collections.singletonList(uiPlugin1));

        List<PluginClientInfo> uiPlugins = pluginUiController.getUiPlugins();

        assertEquals(1, uiPlugins.size());
        PluginClientInfo info = uiPlugins.get(0);

        // Verify all fields are correctly mapped
        assertEquals(uiPlugin1.getPluginId(), info.getPluginId());
        assertEquals(uiPlugin1.getPluginName(), info.getPluginName());
        assertEquals(uiPlugin1.getVendor(), info.getVendor());
        assertEquals(uiPlugin1.getVersion(), info.getVersion());
        assertEquals(uiPlugin1.getDescription(), info.getDescription()); // Assuming description can be null
        assertEquals(uiPlugin1.getAssetsPath(), info.getAssetsPath());
        assertEquals(uiPlugin1.getEntryComponent(), info.getEntryComponent());
        assertEquals(uiPlugin1.getRequiredPrivileges(), info.getRequiredPrivileges()); // Assuming can be null
    }

    @Test
    void testGetUiPlugins_NoPluginsLoadedAtAll() {
        when(pluginService.getLoadedPlugins()).thenReturn(Collections.emptyList());
        List<PluginClientInfo> uiPlugins = pluginUiController.getUiPlugins();
        assertNotNull(uiPlugins);
        assertTrue(uiPlugins.isEmpty());
    }
}
