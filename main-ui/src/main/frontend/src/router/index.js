import { createRouter, createWebHistory } from 'vue-router';
import HomeView from '../views/HomeView.vue';
import PluginPlaceholderView from '../views/PluginPlaceholderView.vue'; // For plugin UIs
import LoadedPluginsView from '../views/LoadedPluginsView.vue'; // For listing plugins

const routes = [
  {
    path: '/',
    name: 'home',
    component: HomeView
  },
  {
    path: '/ui/plugin/:pluginName', // Route for displaying plugin UIs
    name: 'plugin-view',
    component: PluginPlaceholderView,
    props: true // Pass route params as props to the component
  },
  {
    path: '/ui/plugins/list-vue', // Route for the Vue-based plugin list
    name: 'loaded-plugins-vue',
    component: LoadedPluginsView
  }
  // More routes can be added here
];

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL || '/'), // BASE_URL from vue.config.js publicPath
  routes
});

export default router;
