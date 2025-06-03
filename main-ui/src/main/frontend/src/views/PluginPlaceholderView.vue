<template>
  <div class="plugin-placeholder-view">
    <div v-if="isLoading" class="loading-message">
      Loading plugin information for '{{ pluginName }}'...
    </div>
    <div v-else-if="error" class="error-message">
      <p>Error loading data for plugin '{{ pluginName }}': {{ error }}</p>
    </div>
    <div v-else-if="pluginUiMeta && pluginUiMeta.mainComponent" class="plugin-content-wrapper">
      <!-- Dynamically render the plugin's main component -->
      <component :is="pluginUiMeta.mainComponent" :pluginName="pluginName" />
      <!--
        Passing pluginName as a prop to the loaded component is optional,
        but might be useful for the plugin component itself.
      -->
    </div>
    <div v-else class="info-message">
      <p>Plugin '{{ pluginName }}' loaded, but no UI component (mainComponent) is specified in its metadata, or the plugin was not found.</p>
      <p v-if="pluginData">Raw Plugin Data: <pre>{{ pluginData }}</pre></p>
    </div>
  </div>
</template>

<script>
import { ref, computed, watch, onMounted } from 'vue';
import { usePluginRegistry } from '@/services/PluginRegistryService';

export default {
  name: 'PluginPlaceholderView',
  props: {
    pluginName: {
      type: String,
      required: true
    }
  },
  setup(props) {
    const {
      getPluginUiMetadata,
      getPluginByName,
      isLoading: isRegistryLoading,
      error: registryError,
      initialize: initializeRegistry // Ensure registry is initialized
    } = usePluginRegistry();

    const pluginUiMeta = ref(null);
    const pluginData = ref(null); // For debugging if component not found

    async function loadPluginComponentInfo() {
      // Ensure plugin registry is initialized before trying to get data
      // This might be redundant if globally initialized but good for safety
      await initializeRegistry();

      pluginUiMeta.value = getPluginUiMetadata(props.pluginName);
      pluginData.value = getPluginByName(props.pluginName); // Get full data for debugging

      if (!pluginUiMeta.value || !pluginUiMeta.value.mainComponent) {
        console.warn(`PluginPlaceholderView: No mainComponent found for plugin '${props.pluginName}'. Metadata:`, pluginUiMeta.value);
      } else {
        // Conceptual: Here, if we were using dynamic bundle loading (Option B),
        // we might trigger the loading of a script URL from pluginUiMeta.bundleUrl
        // and then register the component before trying to render it.
        // For Option A, we assume 'pluginUiMeta.value.mainComponent' (e.g., "SimpleExamplePluginViewer")
        // is a globally registered component name.
        console.log(`PluginPlaceholderView: Attempting to render component '${pluginUiMeta.value.mainComponent}' for plugin '${props.pluginName}'.`);
      }
    }

    // Watch for changes in pluginName prop if the route can change without remounting the component
    watch(() => props.pluginName, loadPluginComponentInfo, { immediate: true });

    // Also call onMounted in case immediate watch doesn't cover initial load correctly under all circumstances
    // or if initializeRegistry needs to be awaited properly on first load.
    onMounted(loadPluginComponentInfo);

    return {
      pluginUiMeta,
      pluginData, // for debugging
      // Expose registry's loading and error states, specific to the overall data fetching
      isLoading: isRegistryLoading,
      error: registryError
    };
  }
}
</script>

<style scoped>
.plugin-placeholder-view {
  padding: 20px;
}
.loading-message, .error-message, .info-message {
  margin-top: 15px;
  padding: 10px;
  border: 1px solid #eee;
  border-radius: 4px;
}
.error-message {
  color: red;
  background-color: #ffe0e0;
  border-color: red;
}
.info-message {
  background-color: #e0e0ff;
  border-color: blue;
}
.plugin-content-wrapper {
  margin-top:10px;
  border: 1px dashed #ccc;
  padding: 15px;
}
pre {
  white-space: pre-wrap;
  word-wrap: break-word;
  background-color: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
}
</style>
