const path = require("path")
const HtmlWebpackPlugin = require("html-webpack-plugin")
const config = {
  watchOptions: {
    aggregateTimeout: 200,
    poll: 1000
  },
  entry: ['react-hot-loader/patch', './index.js'],
  output: {
    path: path.resolve(__dirname, 'dist'),
    filename: '[name].[contenthash].js'
  },
  module: {
    rules: [{
      test: /\.(js|jsx)$/,
      use: [{
        loader: 'babel-loader',
        options: {
          presets: ['@babel/preset-react', '@babel/preset-env'],
          plugins: ['@babel/plugin-transform-runtime']
        }
      }],
      exclude: /node_modules/,
    }, {
      test: /\.(png|jpg|svg)$/,
      use: 'file-loader',
    }],
  },
  devServer: {
    static: {
      directory: './public'
    }
  },
  plugins: [
    new HtmlWebpackPlugin({
      template: './public/index.html',
      filename: 'index.html',
    })
  ],
  optimization: {
    runtimeChunk: 'single',
    splitChunks: {
      cacheGroups: {
        vendor: {
          test: /[\\/]node_modules[\\/]/,
          name: 'vendors',
          chunks: 'all'
        }
      }
    }
  },
  devtool: "eval-source-map"
}
module.exports = (env, argv) => {
    if (argv.hot) {
        // Cannot use 'contenthash' when hot reloading is enabled.
        config.output.filename = '[name].[hash].js'
    }
    return config
}
