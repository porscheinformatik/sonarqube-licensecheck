const VueLoaderPlugin = require('vue-loader/lib/plugin')

module.exports = {
  entry: {
    configuration: './src/main/web/configuration.js',
    dashboard: './src/main/web/dashboard.js'
  },
  output: {
    filename: 'target/classes/static/[name].js'
  },
  resolve: {
    alias: {
      'vue$': 'vue/dist/vue.esm.js'
    },
    extensions: ['*', '.js', '.vue', '.json']
  },
  module: {
    rules: [
      {
        test: /\.vue$/,
        loader: 'vue-loader'
      },
      {
        test: /.js$/,
        loader: 'buble-loader'
      },
      {
        test: /\.css$/,
        use: [
          'vue-style-loader',
          'css-loader',
        ]
      }
    ]
  },
  plugins: [
    new VueLoaderPlugin(),
  ],
};
