module.exports = {
  entry: './src/main/web/configuration.js',
  output: {
    filename: 'target/classes/static/configuration.js'
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
