<template>
  <div class="plugin-view">
    <h3>Plugin View: {{ pluginName }}</h3>
    <p>This is where the UI for the '<strong>{{ pluginName }}</strong>' plugin would be rendered.</p>
    <p><em>(Conceptual: Dynamic component loading or an iframe for this plugin's content would go here.)</em></p>
    <p>The entry point specified by this plugin is: <code>{{ uiEntryPoint || 'Not available' }}</code></p>
    <p>Actual rendering of plugin UIs (especially those using different technologies or complex fragments like Thymeleaf from a JAR) is an advanced topic requiring careful setup of template resolvers or content fetching mechanisms.</p>
  </div>
</template>

<script>
// In a real application, you might fetch the plugin's UI entry point or other metadata here.
// For now, we just display the pluginName passed via route props.
// import { ref, onMounted, watch } from 'vue';
// import { useRoute } from 'vue-router'; // If not using props:true in router

export default {
  name: 'PluginPlaceholderView',
  props: {
    pluginName: {
      type: String,
      required: true
    }
  },
  setup(props) {
    // Conceptual: Fetch UI entry point or other metadata for the plugin
    // const route = useRoute(); // If props:true is not used for pluginName
    // const currentPluginName = ref(props.pluginName || route.params.pluginName);
    const uiEntryPoint = ref(''); // Placeholder

    // onMounted(() => {
    //   console.log(`PluginPlaceholderView mounted for plugin: ${props.pluginName}`);
    //   // Example: Fetch actual UI entry point from backend if needed
    //   // fetch(`/api/plugins/${props.pluginName}/ui-entry-point`)
    //   //   .then(res => res.json())
    //   //   .then(data => uiEntryPoint.value = data.entryPoint)
    //   //   .catch(err => console.error("Failed to fetch UI entry point:", err));
    // });

    // watch(() => props.pluginName, (newName) => {
    //   currentPluginName.value = newName;
    //   // Refetch or update based on newName
    // });

    return {
      uiEntryPoint
      // If not using props for pluginName:
      // pluginName: currentPluginName
    };
  }
}
</script>

<style scoped>
.plugin-view {
  padding: 20px;
  border: 1px solid #ccc;
  border-radius: 5px;
  background-color: #f9f9f9;
}
.plugin-view code {
  background-color: #eee;
  padding: 2px 4px;
  border-radius: 3px;
}
</style>
