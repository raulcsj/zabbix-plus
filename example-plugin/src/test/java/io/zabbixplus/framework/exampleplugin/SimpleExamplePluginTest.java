package io.zabbixplus.framework.exampleplugin;

import io.zabbixplus.framework.core.plugin.PluginContext;
import io.zabbixplus.framework.core.service.ExampleTableService;
import io.zabbixplus.framework.plugin.UiElement;
import io.zabbixplus.framework.plugin.UiMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimpleExamplePluginTest {

    private SimpleExamplePlugin plugin;

    @Mock
    private PluginContext mockPluginContext;

    @Mock
    private ApplicationContext mockApplicationContext;

    @Mock
    private ExampleTableService mockExampleTableService;

    @BeforeEach
    void setUp() {
        plugin = new SimpleExamplePlugin();
    }

    @Test
    void testPluginIdentity() {
        assertEquals("simple-example-plugin", plugin.getPluginId());
        assertEquals("Simple Example Plugin", plugin.getPluginName());
        assertEquals("A simple example plugin for ZabbixPlus Framework.", plugin.getDescription());
        assertEquals("ZabbixPlus", plugin.getVendor());
        assertEquals("1.0.0", plugin.getVersion());
    }

    @Test
    void testGetUiMetadata() {
        UiMetadata uiMetadata = plugin.getUiMetadata();
        assertNotNull(uiMetadata);
        assertEquals("simple-example-plugin-main", uiMetadata.getMainComponent());
        assertEquals("Simple Example Plugin", uiMetadata.getPluginName());
        assertEquals("A simple example plugin for ZabbixPlus Framework.", uiMetadata.getDescription());
        // Check other fields if they have default values, e.g., assetsPath
        assertEquals("plugin/simple-example-plugin/assets", uiMetadata.getAssetsPath());
        assertEquals("simple-example-plugin-entry", uiMetadata.getEntryComponent());
    }

    @Test
    void testGetNavigationItems() {
        List<UiElement.NavigationItem> navigationItems = plugin.getNavigationItems();
        assertNotNull(navigationItems);
        assertEquals(1, navigationItems.size());

        UiElement.NavigationItem item = navigationItems.get(0);
        assertEquals("Simple Example", item.getLabel());
        assertEquals("simple-example-plugin-nav", item.getComponentId()); // Or component name based on your routing
        //assertEquals("/simple-example", item.getPath()); // If using path-based routing
        assertNull(item.getParentId()); // Assuming it's a top-level item
        // Check icon if you have one defined
    }

    @Test
    void testInit_Successful() {
        Map<String, Object> config = new HashMap<>();
        config.put("sampleConfigKey", "sampleConfigValue");

        when(mockPluginContext.getConfiguration()).thenReturn(config);
        when(mockPluginContext.getApplicationContext()).thenReturn(mockApplicationContext);
        when(mockApplicationContext.getBean(ExampleTableService.class)).thenReturn(mockExampleTableService);

        assertDoesNotThrow(() -> plugin.init(mockPluginContext));

        // Verify ExampleTableService was retrieved and possibly used if init method does more
        // For now, just verify it was fetched.
        // If plugin stores the service, we could assertNotNull(plugin.getExampleTableService());
        // This might require adding a getter in the plugin or making the field package-private for testing.

        // Verify configuration was processed (e.g. if it sets a field)
        // For example, if plugin has: private String sampleConfig;
        // and init does: this.sampleConfig = (String) config.get("sampleConfigKey");
        // then we could assert: assertEquals("sampleConfigValue", plugin.getSampleConfig());
        // As per current SimpleExamplePlugin, it just logs. We can't directly test the log output here
        // without more complex setup, so we focus on interactions.
    }

    @Test
    void testInit_ExampleTableServiceNotFound() {
        Map<String, Object> config = Collections.emptyMap();
        when(mockPluginContext.getConfiguration()).thenReturn(config);
        when(mockPluginContext.getApplicationContext()).thenReturn(mockApplicationContext);
        when(mockApplicationContext.getBean(ExampleTableService.class))
                .thenThrow(new NoSuchBeanDefinitionException(ExampleTableService.class));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            plugin.init(mockPluginContext);
        });
        assertTrue(exception.getMessage().contains("Could not retrieve ExampleTableService"));
    }

    @Test
    void testInit_NullConfiguration() {
        // Test how the plugin handles null configuration from context
        when(mockPluginContext.getConfiguration()).thenReturn(null); // Simulate null config
        when(mockPluginContext.getApplicationContext()).thenReturn(mockApplicationContext);
        when(mockApplicationContext.getBean(ExampleTableService.class)).thenReturn(mockExampleTableService);

        // Assuming the plugin is robust enough to handle null config (e.g. by using an empty map or specific checks)
        // If it's expected to throw an NPE or other exception, adjust the assertion.
        assertDoesNotThrow(() -> plugin.init(mockPluginContext), "Plugin should handle null configuration gracefully.");
        // If SimpleExamplePlugin's init method logs "Configuration: null", this call would pass.
    }


    @Test
    void testLoad() {
        // Assuming load() only logs, just call it to ensure no exceptions
        assertDoesNotThrow(() -> plugin.load());
        // If load() had specific logic, we would test that.
    }

    @Test
    void testUnload() {
        // Assuming unload() only logs, just call it to ensure no exceptions
        assertDoesNotThrow(() -> plugin.unload());
        // If unload() had specific logic, we would test that.
    }
}
