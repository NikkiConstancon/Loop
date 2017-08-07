var zetta = require('zetta');
var GlucoseMeter = require('../index');
var argv = require('minimist')(process.argv.slice(2));

var increment = argv['i'];

zetta()
  .use(GlucoseMeter, {increment: increment})
  .link('http://dev.zettaapi.org')
  .listen(1337);
