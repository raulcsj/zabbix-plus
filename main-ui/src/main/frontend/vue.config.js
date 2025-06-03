// vue.config.js
// This ensures the output of `npm run build` goes to `src/main/frontend/dist`
// which our Gradle 'copyFrontendDist' task expects.
module.exports = {
  outputDir: 'dist',
  // Optional: If your Spring Boot app serves the Vue app from a subpath (e.g., /ui),
  // you might need to set publicPath. For now, assuming root serving.
  // publicPath: process.env.NODE_ENV === 'production' ? '/ui/' : '/'
};
