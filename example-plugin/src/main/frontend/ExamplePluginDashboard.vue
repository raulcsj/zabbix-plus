<template>
  <div class="example-plugin-dashboard">
    <h3>{{ componentTitle }}</h3>
    <p>This dashboard is part of the '<strong>{{ pluginName }}</strong>' plugin.</p>
    <p>It will be used to display and interact with data managed by the plugin.</p>

    <div class="data-section">
      <h4>Data from Plugin Backend:</h4>
      <button @click="fetchData" :disabled="isLoading">
        {{ isLoading ? 'Loading...' : 'Fetch Data' }}
      </button>
      <div v-if="error" class="error-message">
        Error fetching data: {{ error }}
      </div>
      <div v-if="items.length > 0" class="items-list">
        <h5>Items:</h5>
        <ul>
          <li v-for="item in items" :key="item.id">
            ID: {{ item.id }}, Name: {{ item.name }}, Value: {{ item.value }}
          </li>
        </ul>
      </div>
      <p v-else-if="!isLoading && !error">No data fetched yet or no items found.</p>
    </div>

    <div class="action-section">
      <h4>Add New Item:</h4>
      <div>
        <label for="itemName">Name: </label>
        <input type="text" id="itemName" v-model="newItem.name" />
      </div>
      <div>
        <label for="itemValue">Value: </label>
        <input type="text" id="itemValue" v-model="newItem.value" />
      </div>
      <button @click="addItem" :disabled="isAdding">
        {{ isAdding ? 'Adding...' : 'Add Item' }}
      </button>
      <div v-if="addItemError" class="error-message">
        Error adding item: {{ addItemError }}
      </div>
      <div v-if="addItemSuccess" class="success-message">
        Successfully added item!
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ExamplePluginDashboard', // This name will be used for registration in main-ui
  props: {
    // pluginName prop is expected to be passed by PluginPlaceholderView.vue in main-ui
    pluginName: {
      type: String,
      default: 'SimpleExamplePlugin' // Default if not provided
    }
  },
  data() {
    return {
      componentTitle: 'Example Plugin Dashboard',
      items: [],
      isLoading: false,
      error: null,
      newItem: {
        name: '',
        value: '' // 'value' is part of the form, but backend currently only uses 'name'
      },
      isAdding: false,
      addItemError: null,
      addItemSuccess: false,
    };
  },
  methods: {
    getApiBasePath() {
      // Construct base API path using pluginName prop, converting to lowercase
      // as used in ExamplePluginApiController paths.
      return `/api/plugins/${this.pluginName.toLowerCase()}`;
    },
    async fetchData() {
      this.isLoading = true;
      this.error = null;
      // this.items = []; // Keep existing items while loading new ones, or clear them. Clearing them:
      this.items = [];
      try {
        const response = await fetch(`${this.getApiBasePath()}/data`);
        if (!response.ok) {
          const errorText = await response.text();
          throw new Error(`HTTP error! status: ${response.status} - ${errorText}`);
        }
        this.items = await response.json();
        console.log(`[${this.pluginName}] Fetched data:`, this.items);
      } catch (e) {
        this.error = e.message;
        console.error(`[${this.pluginName}] Error fetching data:`, e);
      } finally {
        this.isLoading = false;
      }
    },
    async addItem() {
      this.isAdding = true;
      this.addItemError = null;
      this.addItemSuccess = false;
      try {
        const response = await fetch(`${this.getApiBasePath()}/data`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          // newItem contains 'name' and 'value'. Backend currently uses 'name'.
          body: JSON.stringify(this.newItem)
        });

        if (!response.ok) {
          const errorData = await response.text();
          throw new Error(`HTTP error! status: ${response.status} - ${errorData}`);
        }

        // Response from backend POST is: Map.of("message", "Data added successfully via plugin")
        // We can log this or display it.
        const result = await response.json();
        console.log(`[${this.pluginName}] Add item result:`, result.message);

        this.addItemSuccess = true;
        // Clear form
        this.newItem.name = '';
        this.newItem.value = '';

        // Optionally, re-fetch all data to see the new item from the server
        // Or, if the POST request returned the created item, add it to 'this.items' directly.
        // Since our current POST returns just a message, re-fetching is a good way to update the list.
        await this.fetchData();

      } catch (e) {
        this.addItemError = e.message;
        console.error(`[${this.pluginName}] Error adding item:`, e);
      } finally {
        this.isAdding = false;
      }
    }
  },
  mounted() {
    console.log(`${this.componentTitle} for plugin '${this.pluginName}' has been mounted.`);
    // Fetch data when component mounts for immediate display
    this.fetchData();
  }
}
</script>

<style scoped>
.example-plugin-dashboard {
  padding: 20px;
  border: 2px solid #007bff; /* Blue border */
  border-radius: 8px;
  background-color: #f8f9fa;
}
.example-plugin-dashboard h3 {
  color: #0056b3; /* Darker blue for title */
  margin-top: 0;
}
.data-section, .action-section {
  margin-top: 20px;
  padding: 15px;
  border: 1px solid #ccc;
  border-radius: 5px;
  background-color: #fff;
}
.data-section h4, .action-section h4 {
  margin-top: 0;
}
.items-list ul {
  list-style-type: none;
  padding-left: 0;
}
.items-list li {
  padding: 5px 0;
  border-bottom: 1px solid #eee;
}
.items-list li:last-child {
  border-bottom: none;
}
button {
  padding: 8px 15px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 10px;
}
button:disabled {
  background-color: #aaa;
}
button:hover:not(:disabled) {
  background-color: #0056b3;
}
.error-message {
  color: red;
  margin-top: 10px;
}
.success-message {
  color: green;
  margin-top: 10px;
}
input[type="text"] {
  padding: 8px;
  margin-bottom: 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
  width: calc(100% - 18px); /* Account for padding */
}
label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}
</style>
