<template>
  <div class="loaded-plugins-view">
    <h2>Loaded Plugins</h2>

    <button @click="refreshPlugins" :disabled="isLoading" class="refresh-button">
      {{ isLoading ? 'Loading...' : 'Refresh Plugin List' }}
    </button>

    <div v-if="isLoading && pluginsList.length === 0" class="loading-message">
      Fetching plugin list...
    </div>
    <div v-else-if="error" class="error-message">
      <p>Error fetching plugins: {{ error }}</p>
    </div>
    <table v-else-if="pluginsList.length > 0">
      <thead>
        <tr>
          <th>Name</th>
          <th>Description</th>
          <th>Main UI Component</th>
          <th>Navigation Items</th>
          <th>Raw UI Metadata</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="plugin in pluginsList" :key="plugin.name">
          <td>{{ plugin.name }}</td>
          <td>{{ plugin.description || (plugin.uiMetadata && plugin.uiMetadata.description) || 'N/A' }}</td>
          <td>{{ (plugin.uiMetadata && plugin.uiMetadata.mainComponent) || 'N/A' }}</td>
          <td>
            <ul v-if="plugin.navigationItems && plugin.navigationItems.length > 0">
              <li v-for="navItem in plugin.navigationItems" :key="navItem.path">
                {{ navItem.name }} ({{ navItem.path }})
              </li>
            </ul>
            <span v-else>None</span>
          </td>
          <td>
            <pre>{{ plugin.uiMetadata || {} }}</pre>
          </td>
        </tr>
      </tbody>
    </table>
    <p v-else-if="!isLoading && pluginsList.length === 0 && !error" class="info-message">
      No plugins loaded or data not fetched yet.
    </p>
  </div>
</template>

<script>
import { onMounted, computed } from 'vue';
import { usePluginRegistry } from '@/services/PluginRegistryService'; // Assuming @ alias for src

export default {
  name: 'LoadedPluginsView',
  setup() {
    const {
      initialize: initializePluginRegistry,
      plugins, // This is the reactive ref holding the list of all plugin client info
      isLoading,
      error,
    } = usePluginRegistry();

    onMounted(async () => {
      // Ensure data is fetched if not already done globally
      await initializePluginRegistry();
    });

    const pluginsList = computed(() => plugins.value);

    // The initialize function in usePluginRegistry is designed to fetch only once.
    // If a true refresh is needed, the service would need a dedicated refresh function
    // that clears rawPluginData and calls fetchPluginData again.
    // For now, this button re-runs initialization, which will only refetch if data is empty.
    const refreshPlugins = async () => {
      // To implement a true refresh, PluginRegistryService would need a public
      // function to clear rawPluginData.value and then call fetchPluginData.
      // For now, initialize will re-fetch if rawPluginData is empty or not yet set.
      console.log("Refresh requested. Current service implementation will re-fetch if data was cleared or never fetched.");
      await initializePluginRegistry();
    };

    return {
      pluginsList,
      isLoading,
      error,
      refreshPlugins
    };
  }
}
</script>

<style scoped>
.loaded-plugins-view {
  padding: 20px;
}
table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 15px;
}
th, td {
  border: 1px solid #ddd;
  padding: 8px;
  text-align: left;
  vertical-align: top;
}
th {
  background-color: #f2f2f2;
}
.error-message, .loading-message, .info-message {
  color: red;
  margin-top: 15px;
  padding:10px;
  border: 1px solid #eee;
  border-radius: 4px;
}
.loading-message {
  color: #555;
}
.info-message {
  color: blue;
}
.refresh-button {
  padding: 8px 15px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-bottom:15px;
}
.refresh-button:disabled {
  background-color: #aaa;
}
.refresh-button:hover:not(:disabled) {
  background-color: #0056b3;
}
ul {
  margin: 0;
  padding-left: 20px;
}
pre {
  white-space: pre-wrap;
  word-wrap: break-word;
  background-color: #f5f5f5;
  padding: 5px;
  border-radius: 3px;
  font-size: 0.9em;
}
</style>
