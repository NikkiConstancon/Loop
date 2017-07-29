var zetta = require('zetta');
var scout = require('./scout.js');
var display = require('./display.js');

var linkAddress = 'http://197.242.150.255:3009'
if (process.argv.indexOf('--test') >= 0) {
    linkAddress = 'http://127.0.0.1:3009'
    console.log('Zetalet initialized in testing mode, using link address [' + linkAddress + ']')
}

zetta()
    .name('zettalet')
	.use(scout)
	.use(display)
    .link(linkAddress)
	.listen(6000, function(){
    	console.log('Zettelet is running: 6000');
	});
