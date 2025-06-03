<template>
  <div class="loaded-plugins-view">
    <h2>Loaded Plugins (Vue Frontend)</h2>
    <p>This data would conceptually be fetched from a backend API endpoint like <code>/api/plugins</code>, which in turn would use <code>PluginService.getLoadedPlugins()</code>.</p>
    <button @click="fetchPlugins" :disabled="loading">
      {{ loading ? 'Loading...' : 'Refresh Plugin List' }}
    </button>
    <div v-if="error" class="error-message">
      <p>Error fetching plugins: {{ error }}</p>
    </div>
    <table v-if="plugins.length">
      <thead>
        <tr>
          <th>Name</th>
          <th>Description</th>
          <th>Type</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="plugin in plugins" :key="plugin.name">
          <td>{{ plugin.name }}</td>
          <td>{{ plugin.description }}</td>
          <td>{{ plugin.type }}</td>
        </tr>
      </tbody>
    </table>
    <p v-if="!loading && !plugins.length && !error">No plugins loaded or data not fetched yet.</p>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';

export default {
  name: 'LoadedPluginsView',
  setup() {
    const plugins = ref([]);
    const loading = ref(false);
    const error = ref(null);

    const fetchPlugins = async () => {
      loading.value = true;
      error.value = null;
      try {
        // This is where you would make an actual API call
        // const response = await fetch('/api/plugins'); // Replace with your actual API endpoint
        // if (!response.ok) {
        //   throw new Error(`HTTP error! status: ${response.status}`);
        // }
        // const data = await response.json();

        // Simulating API call with placeholder data for now
        await new Promise(resolve => setTimeout(resolve, 500)); // Simulate network delay
        const data = [
          // { name: "SimpleExamplePlugin", description: "A simple example plugin.", type: "UiPlugin" },
          // { name: "AnotherConceptualPlugin", description: "Does something else.", type: "Generic Plugin" }
        ];
        // In a real app, the 'type' (UiPlugin vs Generic) would be determined from backend data
        plugins.value = data;

      } catch (e) {
        console.error("Error fetching plugins:", e);
        error.value = e.message;
        plugins.value = []; // Clear plugins on error
      } finally {
        loading.value = false;
      }
    };

    onMounted(() => {
      fetchPlugins(); // Fetch plugins when component is mounted
    });

    return {
      plugins,
      loading,
      error,
      fetchPlugins
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
}
th {
  background-color: #f2f2f2;
}
.error-message {
  color: red;
  margin-top: 15px;
}
button {
  padding: 8px 15px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-bottom:15px;
}
button:disabled {
  background-color: #aaa;
}
button:hover:not(:disabled) {
  background-color: #0056b3;
}
</style>
