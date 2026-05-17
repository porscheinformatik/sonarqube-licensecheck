// Shim for "react/jsx-runtime".
// SQ 25.x: host React is on window.React — bridge jsx() to createElement()
//          so elements use the host's $$typeof symbol (React 18 format).
// SQ 26.x: no host globals — use the bundled React 19 jsx-runtime directly.
var useHost = require("./use-host-react");

if (useHost) {
  var React = window.React;

  function jsx(type, props, key) {
    if (key !== undefined) {
      props = Object.assign({}, props, { key: key });
    }
    return React.createElement(type, props);
  }

  exports.jsx = jsx;
  exports.jsxs = jsx;
  exports.Fragment = React.Fragment;
} else {
  var runtime = require("react-jsx-runtime-bundled");
  exports.jsx = runtime.jsx;
  exports.jsxs = runtime.jsxs;
  exports.Fragment = runtime.Fragment;
}
