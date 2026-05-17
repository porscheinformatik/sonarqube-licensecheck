const path = require("path");

// Shim directory: each shim checks for SonarQube's host React/ReactDOM globals
// at runtime. If present (SQ 25.x with React 18 globals), the host version is
// used. Otherwise (SQ 26.x where React 19 is internal), the bundled React 19
// from node_modules is used as a fallback.
const shims = path.resolve(__dirname, "src/main/web/shims");

// Resolve paths to the real React packages for the bundled fallback.
// These bypass the shim aliases so the full React 19 code is available.
const reactDir = path.dirname(require.resolve("react/package.json"));
const reactDomDir = path.dirname(require.resolve("react-dom/package.json"));

module.exports = (env, argv) => ({
  entry: {
    configuration: "./src/main/web/configuration.js",
    dashboard: "./src/main/web/dashboard.js",
  },
  output: {
    path: path.resolve(__dirname, "target/classes/static/"),
    filename: "[name].js",
  },
  resolve: {
    extensions: [".js", ".jsx", ".json"],
    alias: {
      // Shims (intercept normal imports)
      react$: path.join(shims, "react-shim.js"),
      "react-dom$": path.join(shims, "react-dom-shim.js"),
      "react-dom/client": path.join(shims, "react-dom-client-shim.js"),
      "react/jsx-runtime": path.join(shims, "jsx-runtime-shim.js"),
      "react/jsx-dev-runtime": path.join(shims, "jsx-runtime-shim.js"),
      // Bundled fallback paths (used by shims when globals are not available)
      "react-bundled$": path.join(reactDir, "index.js"),
      "react-dom-bundled$": path.join(reactDomDir, "index.js"),
      "react-dom-client-bundled$": path.join(reactDomDir, "client.js"),
      "react-jsx-runtime-bundled$": path.join(reactDir, "jsx-runtime.js"),
    },
  },
  module: {
    rules: [
      {
        test: /\.(js|jsx)$/,
        exclude: /node_modules/,
        use: {
          loader: "babel-loader",
          options: {
            presets: ["@babel/preset-env", "@babel/preset-react"],
          },
        },
      },
      {
        test: /\.css$/,
        use: ["style-loader", "css-loader"],
      },
    ],
  },
});
