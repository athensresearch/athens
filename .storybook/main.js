module.exports = {
  "stories": [
    "../src/js/**/*.stories.mdx",
    "../src/js/**/*.stories.tsx"
  ],
  "addons": [
    "@storybook/addon-links",
    "@storybook/addon-essentials",
    '@geometricpanda/storybook-addon-badges'
  ],
  webpackFinal: async (config, { configType }) => {
    // Always prefer .tsx files when resolving modules.
    // We output the .js files from tsc directly on the same folder, but want
    // storybook to process the original .tsx for the additional TS tooling.
    config.resolve.extensions.unshift(".tsx");
    
    // Return the altered config.
    return config;
  }
}