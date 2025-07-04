const path = require('path');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const TerserPlugin = require('terser-webpack-plugin');
const CopyPlugin = require('copy-webpack-plugin');

const SRC_DIR = path.resolve(__dirname, 'src/main/resources/easya11y/webresources-src');
const DIST_DIR = path.resolve(__dirname, 'src/main/resources/easya11y/webresources');

module.exports = (env, argv) => {
  const isProduction = argv.mode === 'production';
  
  return {
    entry: {
      'js/easya11y': path.join(SRC_DIR, 'js/accessibility-scanner.js'),
      'css/style': path.join(SRC_DIR, 'css/style.css')
    },
    output: {
      path: DIST_DIR,
      filename: '[name].js',
      clean: false // Don't clean the output directory to preserve other files
    },
    devtool: isProduction ? false : 'source-map',
    module: {
      rules: [
        // JavaScript and JSX
        {
          test: /\.(js|jsx)$/,
          exclude: /node_modules\/(?!@magnolia-services)/,
          use: {
            loader: 'babel-loader',
            options: {
              presets: ['@babel/preset-env']
            }
          }
        },
        // CSS
        {
          test: /\.css$/,
          use: [
            isProduction ? MiniCssExtractPlugin.loader : 'style-loader',
            'css-loader'
          ]
        }
      ]
    },
    plugins: [
      new MiniCssExtractPlugin({
        filename: '[name].css'
      }),
      new CopyPlugin({
        patterns: [
          {
            from: '*.html',
            context: SRC_DIR,
            to: DIST_DIR,
            globOptions: {
              ignore: ['**/test-*.html', '**/*-react.html', '**/*-fixed.html', '**/*-complete.html', '**/*-original.html']
            }
          }
        ]
      })
    ],
    optimization: {
      minimize: isProduction,
      minimizer: [
        new TerserPlugin({
          terserOptions: {
            format: {
              comments: false,
            },
            compress: {
              drop_console: true
            }
          },
          extractComments: false
        })
      ]
    },
    resolve: {
      extensions: ['.js', '.jsx'],
      fallback: {
        // Polyfills for Node.js core modules if needed
        path: false,
        fs: false
      }
    }
  };
};
