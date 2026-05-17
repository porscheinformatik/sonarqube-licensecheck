// Shim that provides react-dom/client (createRoot, hydrateRoot).
// SQ 25.x (React 18): createRoot lives on the global window.ReactDOM.
// SQ 26.x (React 19): no globals; fall back to bundled react-dom/client.
var useHost = require("./use-host-react");

if (useHost) {
  module.exports = window.ReactDOM;
} else {
  module.exports = require("react-dom-client-bundled");
}
