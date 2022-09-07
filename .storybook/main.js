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
    "@storybook/addon-essentials",
    "@storybook/addon-a11y",
    'storybook-dark-mode',
    '@geometricpanda/storybook-addon-badges'
  ],
  webpackFinal: async (config, { configType }) => {
    // Resolve the root path defined in tsconfig.json
    config.resolve.plugins.push(
      new TsconfigPathsPlugin({
        configFile: path.resolve(__dirname, '../tsconfig.json'),
      })
    );

    // Support mjs modules included with Chakra-UI
    // https://github.com/storybookjs/storybook/issues/16690#issuecomment-971579785
    config.module.rules.push({
      test: /\.mjs$/,
      include: /node_modules/,
      type: "javascript/auto",
    })
    // Return the altered config.
    return config;
  }
}
