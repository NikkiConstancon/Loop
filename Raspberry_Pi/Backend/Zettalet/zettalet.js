var zetta = require('zetta');
var scout = require('./scout.js');
var display = require('./display.js');


var port = Math.floor(Math.random() * (65535 - 1024) + 1024)
var linkAddress = 'http://197.242.150.255:3009'
if (process.argv.indexOf('--test') >= 0) {
    linkAddress = 'http://127.0.0.1:3009'
    console.log('Zetalet initialized in testing mode, using link address [' + linkAddress + ']')
}

zetta()
    .name(require('./name'))
	.use(scout)
	.use(display)
    .link(linkAddress)
    .listen(port, function(){
	});
