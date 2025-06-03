import { ref, computed } from 'vue';

// Holds the raw data fetched from the API
const rawPluginData = ref([]);
const isLoading = ref(false);
const error = ref(null);

// Indicates if initialization has been attempted
let initialized = false;

// Fetches plugin data from the backend API
async function fetchPluginData() {
  if (rawPluginData.value.length > 0) {
    // Data already fetched
    return;
  }

  isLoading.value = true;
  error.value = null;
  try {
    const response = await fetch('/api/ui/plugin-metadata'); // Adjust if your API base path is different
    if (!response.ok) {
      throw new Error(`Failed to fetch plugin metadata: ${response.status} ${response.statusText}`);
    }
    const data = await response.json();
    rawPluginData.value = Array.isArray(data) ? data : [];
    console.log("Plugin data fetched and processed:", rawPluginData.value);
  } catch (e) {
    console.error("Error fetching plugin data:", e);
    error.value = e.message;
    rawPluginData.value = []; // Ensure it's an array even on error
  } finally {
    isLoading.value = false;
  }
}

// Call this function when your Vue application initializes (e.g., in main.js or App.vue's setup)
async function initialize() {
  if (initialized) {
    return;
  }
  initialized = true;
  await fetchPluginData();
}

// Computed property for all loaded plugins' full info
const plugins = computed(() => rawPluginData.value);

// Computed property for combined navigation items from all UI plugins
const navItems = computed(() => {
  return rawPluginData.value.reduce((acc, plugin) => {
    if (plugin.navigationItems && Array.isArray(plugin.navigationItems)) {
      // Add pluginName to each navItem for context if needed later
      const itemsWithContext = plugin.navigationItems.map(item => ({
        ...item,
        pluginName: plugin.name
      }));
      return acc.concat(itemsWithContext);
    }
    return acc;
  }, []);
});

// Computed property to easily access a plugin's metadata by its name
const pluginMetadataMap = computed(() => {
  return rawPluginData.value.reduce((map, plugin) => {
    map[plugin.name] = plugin; // Stores the whole plugin object (name, uiMetadata, navigationItems)
    return map;
  }, {});
});

// Function to get a specific plugin's full metadata by name
function getPluginByName(name) {
  return pluginMetadataMap.value[name];
}

// Function to get a specific plugin's UI metadata (the content of uiMetadata map)
function getPluginUiMetadata(name) {
  const plugin = getPluginByName(name);
  return plugin ? plugin.uiMetadata : null;
}

export function usePluginRegistry() {
  return {
    initialize,
    plugins,
    navItems,
    pluginMetadataMap, // Exposing the map directly
    getPluginByName,   // Exposing the lookup function
    getPluginUiMetadata, // Exposing specific UI metadata lookup
    isLoading: computed(() => isLoading.value),
    error: computed(() => error.value),
  };
}

// Conceptual: How to make it available globally or provide it
// 1. In main.js:
//    import { usePluginRegistry } from './services/PluginRegistryService';
//    const app = createApp(App);
//    const pluginRegistry = usePluginRegistry();
//    app.provide('pluginRegistry', pluginRegistry);
//    pluginRegistry.initialize(); // Initialize early
//    app.mount('#app');
//
// 2. In a component (e.g., App.vue setup):
//    import { inject } from 'vue';
//    const pluginRegistry = inject('pluginRegistry');
//    // Use pluginRegistry.navItems, pluginRegistry.plugins etc.
//
//    Or, if not provided globally, import directly:
//    import { usePluginRegistry } from '@/services/PluginRegistryService'; // Assuming @ alias for src
//    const { initialize, plugins, navItems, isLoading, error } = usePluginRegistry();
//    onMounted(async () => {
//      await initialize(); // Ensure initialized if not done globally
//    });
