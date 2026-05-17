// Shim that provides ReactDOM from the host when available (SQ 25.x),
// falling back to the bundled React 19 ReactDOM (SQ 26.x).
// Adds no-op stubs for APIs removed in React 19 that some bundled
// third-party dependencies may still reference.
var useHost = require("./use-host-react");
var ReactDOM;

if (useHost) {
  ReactDOM = window.ReactDOM;
} else {
  ReactDOM = require("react-dom-bundled");
}

if (!ReactDOM.unmountComponentAtNode) {
  ReactDOM.unmountComponentAtNode = function () {
    return false;
  };
}

if (!ReactDOM.unstable_renderSubtreeIntoContainer) {
  ReactDOM.unstable_renderSubtreeIntoContainer = function () {
    return null;
  };
}

module.exports = ReactDOM;
