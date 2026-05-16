const path = require("path");

// Resolve the actual react-dom CJS file path (bypasses the exports map restriction).
// Select dev vs prod build based on webpack mode for proper error messages during development.
const reactDomPkgDir = path.dirname(require.resolve("react-dom/package.json"));
const reactDomCjs = (mode) =>
  path.join(
    reactDomPkgDir,
    mode === "development" ? "cjs/react-dom.development.js" : "cjs/react-dom.production.js",
  );

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
      "react-dom$": path.resolve(__dirname, "src/main/web/react-dom-compat.js"),
      "react-dom-cjs": reactDomCjs(argv.mode),
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
