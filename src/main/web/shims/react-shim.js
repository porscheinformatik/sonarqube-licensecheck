// Shim that provides React from the host when available (SQ 25.x exposes
// window.React + window.ReactDOM), falling back to the bundled React 19
// (SQ 26.x keeps React internal to its own ES module system).
var useHost = require("./use-host-react");

if (useHost) {
  module.exports = window.React;
} else {
  module.exports = require("react-bundled");
}
