package io.zabbixplus.framework.core.service;

import io.zabbixplus.framework.plugin.Plugin;
import io.zabbixplus.framework.plugin.PluginContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.ServiceLoader;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PluginServiceTest {

    private PluginService pluginService;

    @Mock
    private Plugin mockPlugin;

    @Mock
    private ServiceLoader<Plugin> mockServiceLoader;

    @Mock
    private URLClassLoader mockUrlClassLoader;

    @BeforeEach
    void setUp() {
        pluginService = new PluginService();
    }

    @Test
    void testPluginServiceInitialization() {
        assertNotNull(pluginService);
        assertTrue(pluginService.getLoadedPlugins().isEmpty());
    }

    @Test
    void testLoadPlugin_ValidPlugin() {
        // Mocking ServiceLoader behavior
        when(mockServiceLoader.iterator()).thenReturn(Collections.singletonList(mockPlugin).iterator());

        // Mocking URLClassLoader creation and ServiceLoader loading
        try (var mockedStaticServiceLoader = mockStatic(ServiceLoader.class);
             var mockedStaticURLClassLoader = mockStatic(URLClassLoader.class)) {

            mockedStaticURLClassLoader.when(() -> URLClassLoader.newInstance(any(URL[].class))).thenReturn(mockUrlClassLoader);
            mockedStaticServiceLoader.when(() -> ServiceLoader.load(Plugin.class, mockUrlClassLoader)).thenReturn(mockServiceLoader);

            pluginService.loadPlugin("dummy.jar");

            verify(mockPlugin).init(any(PluginContext.class));
            assertEquals(1, pluginService.getLoadedPlugins().size());
            assertTrue(pluginService.getLoadedPlugins().contains(mockPlugin));
        }
    }

    @Test
    void testLoadPlugin_NonExistentJar() {
        // Mocking URLClassLoader creation to throw an exception
        try (var mockedStaticURLClassLoader = mockStatic(URLClassLoader.class)) {
            mockedStaticURLClassLoader.when(() -> URLClassLoader.newInstance(any(URL[].class)))
                    .thenThrow(new RuntimeException("Simulated Jar loading error"));

            Exception exception = assertThrows(RuntimeException.class, () -> {
                pluginService.loadPlugin("nonexistent.jar");
            });

            assertTrue(exception.getMessage().contains("Simulated Jar loading error"));
            assertTrue(pluginService.getLoadedPlugins().isEmpty());
        }
    }

    @Test
    void testLoadPlugin_JarWithoutValidPlugin() {
        // Mocking ServiceLoader to return an empty iterator
        when(mockServiceLoader.iterator()).thenReturn(Collections.emptyIterator());

        // Mocking URLClassLoader creation and ServiceLoader loading
        try (var mockedStaticServiceLoader = mockStatic(ServiceLoader.class);
             var mockedStaticURLClassLoader = mockStatic(URLClassLoader.class)) {

            mockedStaticURLClassLoader.when(() -> URLClassLoader.newInstance(any(URL[].class))).thenReturn(mockUrlClassLoader);
            mockedStaticServiceLoader.when(() -> ServiceLoader.load(Plugin.class, mockUrlClassLoader)).thenReturn(mockServiceLoader);

            pluginService.loadPlugin("invalid.jar");

            assertTrue(pluginService.getLoadedPlugins().isEmpty());
        }
    }

    @Test
    void testPluginContextProvidedToPlugin() {
        // Mocking ServiceLoader behavior
        when(mockServiceLoader.iterator()).thenReturn(Collections.singletonList(mockPlugin).iterator());

        // Mocking URLClassLoader creation and ServiceLoader loading
        try (var mockedStaticServiceLoader = mockStatic(ServiceLoader.class);
             var mockedStaticURLClassLoader = mockStatic(URLClassLoader.class)) {

            mockedStaticURLClassLoader.when(() -> URLClassLoader.newInstance(any(URL[].class))).thenReturn(mockUrlClassLoader);
            mockedStaticServiceLoader.when(() -> ServiceLoader.load(Plugin.class, mockUrlClassLoader)).thenReturn(mockServiceLoader);

            pluginService.loadPlugin("dummy.jar");

            verify(mockPlugin).init(argThat(context -> {
                assertNotNull(context);
                // Add more assertions for PluginContext if needed
                return true;
            }));
        }
    }
}
