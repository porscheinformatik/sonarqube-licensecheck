const path = require("path");

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
  },
  module: {
    rules: [
      // Echoes bundles an unreachable React 15 fallback that imports APIs removed in React 19.
      {
        test: /@sonarsource[\\/]echoes-react[\\/]dist[\\/]index\.js$/,
        parser: {
          importExportsPresence: false,
        },
      },
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
