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
    extensions: ["*", ".js", ".jsx", ".json"],
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
