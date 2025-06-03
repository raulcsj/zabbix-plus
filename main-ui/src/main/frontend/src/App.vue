<template>
  <div id="app-vue-main-container">
    <nav class="main-navigation">
      <router-link to="/">Home</router-link> |

      <!-- Display loading message -->
      <span v-if="isLoadingNavItems">Loading plugin menu... | </span>

      <!-- Display error message -->
      <span v-if="navError" class="error-text">Error loading plugin menu | </span>

      <!-- Dynamic plugin navigation links -->
      <span v-for="item in pluginNavItems" :key="item.path">
        <router-link :to="item.path">
          <i v-if="item.icon" :class="item.icon"></i> {{ item.name }}
        </router-link> |
      </span>

      <router-link to="/ui/plugins/list-vue">Loaded Plugins (Vue)</router-link>
    </nav>
    <main class="content-area">
      <router-view/> <!-- This is where routed components will be displayed -->
    </main>
    <footer>
      <p>&copy; Zabbix Plus Framework - Vue UI</p>
    </footer>
  </div>
</template>

<script>
import { onMounted, computed } from 'vue';
import { usePluginRegistry } from '@/services/PluginRegistryService'; // Assuming @ alias for src

export default {
  name: 'App',
  setup() {
    const {
      initialize: initializePluginRegistry,
      navItems,
      isLoading: isLoadingPlugins, // This reflects loading of all plugin data
      error: pluginRegistryError  // This reflects error from fetching all plugin data
    } = usePluginRegistry();

    onMounted(async () => {
      // Initialize the plugin registry to fetch data from the backend.
      // This will populate navItems and other reactive properties.
      // If initialize is guaranteed to be called globally (e.g. in main.js),
      // this specific call might be redundant but ensures it if not.
      await initializePluginRegistry();
    });

    // Use computed properties for clarity if direct mapping is not preferred
    const pluginNavItems = computed(() => navItems.value);
    const isLoadingNavItems = computed(() => isLoadingPlugins.value);
    const navError = computed(() => pluginRegistryError.value);

    return {
      pluginNavItems,
      isLoadingNavItems,
      navError
    };
  }
}
</script>

<style>
#app-vue-main-container {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.main-navigation {
  padding: 15px;
  background-color: #f8f9fa;
  border-bottom: 1px solid #dee2e6;
  text-align: center;
  flex-shrink: 0;
}

.main-navigation a {
  font-weight: bold;
  color: #2c3e50;
  margin: 0 10px;
  text-decoration: none;
}

.main-navigation a.router-link-exact-active {
  color: #42b983;
}

.main-navigation .error-text {
  color: red;
  font-weight: bold;
}

.content-area {
  padding: 20px;
  flex-grow: 1;
}

footer {
  text-align: center;
  padding: 10px;
  font-size: 0.9em;
  color: #6c757d;
  border-top: 1px solid #eee;
  background-color: #f8f9fa;
  flex-shrink: 0;
}

/* Example for icon styling if using Font Awesome or similar */
.main-navigation i {
  margin-right: 4px;
}
</style>
