const {existsSync, lstatSync} = require("fs");
const {resolve, dirname} = require("path");

function isRelativeImport(path){
  return path.startsWith(".");
}

function isDirectory(path) {
  return existsSync(path) && lstatSync(path).isDirectory();
}

function resolveImport (from, to) {
  return resolve(dirname(from), to);
}

function replaceDirectoryImports() {
  return {
    visitor: {
      ImportDeclaration: (path, state) => {
        const importPath = path.node.source.value;
        const fileName = state.file.opts.filename;
        if (isRelativeImport(importPath) && isDirectory(resolveImport(fileName, importPath))) {
          path.node.source.value += "/index"; 
        }
      }
    }
  }
}


// This config will output files to ./src/gen/components via the `yarn components` script
// See https://shadow-cljs.github.io/docs/UsersGuide.html#_javascript_dialects
module.exports = {
  presets: [
    "@babel/env",
    // Compile tsx files.
    "@babel/preset-typescript",
    // Use the react runtime import if available.
    ["@babel/preset-react", {"runtime": "automatic"}]
  ],
  plugins: [
    // Add /index to all relative directory imports, because Shadow-CLJS does not support
    // them (https://github.com/thheller/shadow-cljs/issues/841#issuecomment-777323477)
    // NB: Putting these files in node_modules would have fixed the directory imports
    // but broken hot reload (https://github.com/thheller/shadow-cljs/issues/764#issuecomment-663064549)
    replaceDirectoryImports,
    // Allow using @/ for root relative imports in the component library.
    ["module-resolver", {alias: {"@": "./src/js/components"}}],
    // Transform material-ui imports into deep imports for faster reload.
    // material-ui is very big, and importing it all can slow down development rebuilds by a lot.
    // https://material-ui.com/guides/minimizing-bundle-size/#development-environment
    ["transform-imports", {
      "@material-ui/core": {
        transform: "@material-ui/core/esm/${member}",
        preventFullImport: true
      },
      "@material-ui/icons": {
        transform: "@material-ui/icons/esm/${member}",
        preventFullImport: true
      }
    }],
    // Our build doesn't need the {loose: true} option, but if not included it wil
    // show a lot of warnings on the storybook build.
    ["@babel/proposal-class-properties", {loose: true}],
    ["@babel/proposal-object-rest-spread", {loose: true}],
    // Used only by storybook, but must be included to avoid build warnings/errors.
    ["@babel/plugin-proposal-private-methods", {loose: true}],
    ["@babel/plugin-proposal-private-property-in-object", {loose: true}],
    // Import helpers from @babel/runtime instead of duplicating them everywhere.
    "@babel/plugin-transform-runtime",
    // Better debug information for styled components.
    // https://styled-components.com/docs/tooling#babel-plugin
    "babel-plugin-styled-components"
  ],
  // Do not apply this babel config to node_modules.
  // Shadow-CLJS also runs babel over node_modules and we don't want this
  // configuration to apply to it. 
  // We still want it to be picked up by storybook though.
  exclude: ["node_modules"]
}
