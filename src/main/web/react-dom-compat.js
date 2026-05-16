// React 19 compatibility shim.
// Provides stubs for APIs removed in React 19 that are still referenced by
// some bundled third-party dependencies inside @sonarsource/echoes-react.
// These stubs are no-ops: the affected code paths (tooltip portals via
// react-floater/react-joyride) are not exercised by the license-check plugin.
//
// This file is set as the webpack alias for "react-dom$". It re-exports
// everything from the real react-dom and adds the removed stub functions.
const ReactDOM = require("react-dom-cjs");

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
