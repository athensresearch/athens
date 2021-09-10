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
    // Always prefer .tsx files when resolving modules.
    // We output the .js files from tsc directly on the same folder, but want
    // storybook to process the original .tsx for the additional TS tooling.
    config.resolve.extensions.unshift(".tsx");
    
    config.resolve.plugins.push(
      new TsconfigPathsPlugin({
        configFile: path.resolve(__dirname, '../tsconfig.json'),
      })
    );

    // Return the altered config.
    return config;
  }
}