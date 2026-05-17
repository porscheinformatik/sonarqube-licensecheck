// Shared detection: use host React globals ONLY when ALL of them are available.
// SQ 25.x exposes window.React + window.ReactDOM (with createRoot).
// SQ 26.x may expose window.React but NOT window.ReactDOM, leading to a
// version mismatch if we mix host react with bundled react-dom.
// To avoid this, require all globals to be present before using any of them.
module.exports =
  typeof window !== "undefined" &&
  window.React &&
  window.ReactDOM &&
  typeof window.ReactDOM.createRoot === "function";
