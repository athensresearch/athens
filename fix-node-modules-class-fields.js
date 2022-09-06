// This script removes class field usage by transpiling via Babel.

// The Google Closure Compiled, used by Clojure, does not support class fields:
// https://github.com/google/closure-compiler/issues/2731
// The errors when using shadow-cljs look like this:
// --- node_modules/@chakra-ui/descendant/dist/index.cjs.js:122
// ES6 transpilation of 'Public class fields' is not yet implemented.

const { exec } = require("child_process");

function execAndPrint(cmd){
  exec(cmd, (error, stdout, stderr) => {
    if (error) {
      console.log(`error: ${error.message}`);
      return;
    }
    if (stderr) {
      console.log(`stderr: ${stderr}`);
      return;
    }
    console.log(`stdout: ${stdout}`);
  });
}

function compileInPlace(filename) {
  execAndPrint(`yarn babel --plugins @babel/plugin-proposal-class-properties --out-file ${filename} ${filename}`);
}

// Add any new files here.
var files = [
  "./node_modules/@chakra-ui/utils/dist/index.cjs.js",
  "./node_modules/@chakra-ui/react-use-pan-event/dist/index.cjs.js",
  "./node_modules/@chakra-ui/modal/dist/index.cjs.js",
  "./node_modules/@chakra-ui/descendant/dist/index.cjs.js",
];

files.map(compileInPlace);
