var zetta = require('zetta');
var Thermometer = require('../index');
var style = require('./apps/style');
var argv = require('minimist')(process.argv.slice(2));

zetta()
  .use(Thermometer, {increment: argv['i']})
  .use(style)
  .listen(1337);
