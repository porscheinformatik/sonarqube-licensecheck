const path = require("path");

// Resolve the actual react-dom cjs file path (bypasses the exports map restriction)
const reactDomCjs = path.join(
  path.dirname(require.resolve("react-dom/package.json")),
  "cjs/react-dom.production.js",
);

module.exports = {
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
      "react-dom-cjs-production": reactDomCjs,
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
};
