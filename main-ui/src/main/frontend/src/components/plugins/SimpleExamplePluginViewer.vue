<template>
  <div class="simple-example-plugin-viewer">
    <h4>{{ VUE_COMPONENT_NAME }}</h4>
    <p>This is the user interface for the '<strong>{{ pluginName || 'Unknown Plugin' }}</strong>'.</p>
    <p>Content is rendered dynamically by the framework!</p>
  </div>
</template>

<script>
export default {
  name: 'SimpleExamplePluginViewer', // This name must match what's in plugin metadata
  props: {
    pluginName: String // Passed down from PluginPlaceholderView
  },
  data() {
    return {
      VUE_COMPONENT_NAME: 'SimpleExamplePluginViewer' // For display
    }
  },
  mounted() {
    console.log(`${this.VUE_COMPONENT_NAME} has been mounted for plugin: ${this.pluginName}`);
  }
}
</script>

<style scoped>
.simple-example-plugin-viewer {
  padding: 15px;
  border: 1px solid green;
  background-color: #e6ffe6;
  border-radius: 5px;
}
.simple-example-plugin-viewer h4 {
  color: green;
}
</style>
