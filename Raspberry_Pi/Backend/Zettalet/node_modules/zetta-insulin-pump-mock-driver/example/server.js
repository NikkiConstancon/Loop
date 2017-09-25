var zetta = require('zetta');
var InsulinPump = require('../index');
var argv = require('minimist')(process.argv.slice(2));

var increment = argv['i'];

zetta()
  .use(InsulinPump, {increment: increment})
  .link('http://dev.zettaapi.org')
  .listen(1337);
