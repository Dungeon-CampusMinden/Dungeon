const path = require('path');
const CopyWebpackPlugin = require('copy-webpack-plugin');


module.exports = {
    entry: './src/index.js',
    output: {
        filename: '[name].js',
        path: path.resolve(__dirname, 'dist'),
    },
    module: {
        rules: [
            {
                test: /\.js$/,
                resolve: {
                    fullySpecified: false,
                },
                enforce: "pre",
                use: [require.resolve('source-map-loader')],
            },
            {
                test: /\.css$/i,
                use: ['style-loader', 'css-loader'],
            }
        ],
    },
    plugins: [
        new CopyWebpackPlugin({
            patterns: [
                {
                    from: 'assets',
                    to: ''
                },
                {
                    from: 'src/*.html',
                    to: '[name].html'
                }
            ]
        })
    ]
};
