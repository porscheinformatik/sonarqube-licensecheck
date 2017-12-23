module.exports = {
  entry: {
    configuration: './src/main/web/configuration.js',
    licenses: './src/main/web/licenses.js'
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
      }
    ]
  }
};
