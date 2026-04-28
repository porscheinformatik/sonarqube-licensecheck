// React 19 compatibility shim.
// Provides stubs for APIs removed in React 19 that are still referenced by
// some bundled third-party dependencies inside @sonarsource/echoes-react.
// These stubs are no-ops: the affected code paths (tooltip portals via
// react-floater/react-joyride) are not exercised by the license-check plugin.
//
// This file is set as the webpack alias for "react-dom$". It re-exports
// everything from the real react-dom and adds the removed stub functions.
const ReactDOM = require("react-dom-cjs-production");

export const {
  createPortal,
  flushSync,
  version,
  __DOM_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE,
  preconnect,
  prefetchDNS,
  preinit,
  preinitModule,
  preload,
  preloadModule,
  requestFormReset,
  unstable_batchedUpdates,
  useFormState,
  useFormStatus,
} = ReactDOM;

export default ReactDOM;

export function unmountComponentAtNode() {
  return false;
}

export function unstable_renderSubtreeIntoContainer() {
  return null;
}
