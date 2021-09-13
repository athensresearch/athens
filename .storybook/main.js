const path = require('path');
const TsconfigPathsPlugin = require('tsconfig-paths-webpack-plugin');

module.exports = {
  features: {
    postcss: false,
  }, 
  "stories": [
    "../src/js/**/*.stories.mdx",
    "../src/js/**/*.stories.tsx"
  ],
  "addons": [
    "@storybook/addon-links",
    "@storybook/addon-essentials"
  ],
  webpackFinal: async (config, { configType }) => {
    // Resolve the root path defined in tsconfig.json
    config.resolve.plugins.push(
      new TsconfigPathsPlugin({
        configFile: path.resolve(__dirname, '../tsconfig.json'),
      })
    );

    // Return the altered config.
    return config;
  }
}